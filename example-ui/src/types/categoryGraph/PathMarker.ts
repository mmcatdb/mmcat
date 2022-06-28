import type { DatabaseConfiguration } from "../database";
import type { Signature } from "../identifiers";
import { Cardinality, SchemaMorphism, type Max, type Min } from "../schema";
import { type Node, AvailabilityStatus } from "./Node";

export type MorphismData = {
    signature: Signature,
    min: Min,
    max: Max
}

function morphismToData(morphism: SchemaMorphism): MorphismData {
    return {
        signature: morphism.signature,
        min: morphism.min,
        max: morphism.max
    };
}

function combineData(first: MorphismData, second: MorphismData): MorphismData {
    return {
        signature: first.signature.concatenate(second.signature),
        min: first.min === Cardinality.One && second.min === Cardinality.One ? Cardinality.One : Cardinality.Zero,
        max: first.max === Cardinality.One && second.max === Cardinality.One ? Cardinality.One : Cardinality.Star
    };
}

export type NodeNeighbour = {
    node: Node,
    previousNode: Node,
    morphism: MorphismData,
    dualMorphism: MorphismData,
    previousNeighbour: NodeNeighbour | undefined,
    dependentNeighbours: NodeNeighbour[]
}

export type FilterFunction = (neighbour: NodeNeighbour) => boolean;

export enum FilterType {
    Base, // A rule must be satisfied for each base morphism along the path.
    Composite // A rule must be satisfied only for the composite morphism of the full path.
}
export type Filter = { function: FilterFunction, type: FilterType };

export class PathMarker {
    // It's actually important this is a stack and not a queue, because the paths has to be traversed in one go.
    rootNode: Node;

    baseFilters: FilterFunction[];
    compositeFilters: FilterFunction[];

    readonly stack = [] as NodeNeighbour[];

    constructor(rootNode: Node, filters: Filter | Filter[]) {
        this.rootNode = rootNode;
        const filtersArray = Array.isArray(filters) ? filters : [ filters ];

        this.baseFilters = filtersArray.filter(filter => filter.type === FilterType.Base).map(filter => filter.function);
        this.compositeFilters = filtersArray.filter(filter => filter.type === FilterType.Composite).map(filter => filter.function);
    }

    getTraversableNeighbours(parentNode: Node, parentNeighbour?: NodeNeighbour): NodeNeighbour[] {
        const combineFunction = parentNeighbour ?
            (morphism: SchemaMorphism) => combineData(parentNeighbour.morphism, morphism) :
            (morphism: SchemaMorphism) => morphismToData(morphism);

        const dualCombineFunction = parentNeighbour ?
            (dualMorphism: SchemaMorphism) => combineData(dualMorphism, parentNeighbour.dualMorphism) :
            (dualMorphism: SchemaMorphism) => morphismToData(dualMorphism);

        let neighbours = parentNode.adjacentEdges
            .map(edge => ({
                node: edge.codomainNode,
                previousNode: parentNode,
                morphism: combineFunction(edge.schemaMorphism),
                dualMorphism: dualCombineFunction(edge.schemaMorphism.dual),
                previousNeighbour: parentNeighbour,
                dependentNeighbours: [] as NodeNeighbour[]
            }))
            .filter(neighbour => !neighbour.node.equals(this.rootNode));

        if (parentNeighbour)
            neighbours = filterBackwardPaths(neighbours, parentNeighbour.morphism);

        for (const filterFunction of this.baseFilters)
            neighbours = neighbours.filter(filterFunction);

        // We have to check that we are not going back to the same node.
        // It's permitted, but the user has to specifically require it.
        // To do so, the user has to choose the node as CertainlyAvailable, i.e. as a direct neighbour.
        // However, this is not required for the leaves because for them there is no other option than to go back.
        if (parentNeighbour && neighbours.length > 1)
            neighbours = neighbours.filter(entry => !entry.node.equals(parentNode));

        return neighbours;
    }

    processNeighbour(neighbour: NodeNeighbour, isDirect = false): void {
        const continueAdding = markNeighbour(neighbour, isDirect);
        if (!continueAdding)
            return;

        const addition = this.getTraversableNeighbours(neighbour.node, neighbour);
        this.stack.push(...addition);
    }

    checkCompositeFilters(neighbour: NodeNeighbour): boolean {
        for (const filterFunction of this.compositeFilters) {
            if (!filterFunction(neighbour))
                return false;
        }

        return true;
    }

    markPathsFromRootNode(): void {
        this.rootNode.setAvailabilityStatus(AvailabilityStatus.Removable);

        /*
        if (isCenteredOnRootNode)
            this.rootNode.select({ type: SelectionType.Root, level: 0 });
        */

        // For the direct neighbours of the root the queue is needed.
        const queue = this.getTraversableNeighbours(this.rootNode);
        const allProcessedNeighbours = [] as NodeNeighbour[];

        let directNeighbour = queue.shift();
        while (directNeighbour) {
            allProcessedNeighbours.push(directNeighbour);
            this.processNeighbour(directNeighbour, true);
            directNeighbour = queue.shift();
        }

        let indirectNeighbour = this.stack.pop();
        while (indirectNeighbour) {
            allProcessedNeighbours.push(indirectNeighbour);
            this.processNeighbour(indirectNeighbour);
            indirectNeighbour = this.stack.pop();
        }

        // Filter out the not available ones.
        allProcessedNeighbours.forEach(neighbour => {
            if (!this.checkCompositeFilters(neighbour))
                neighbour.node.setAvailabilityStatus(AvailabilityStatus.NotAvailable);
        });
    }
}

export function createDefaultFilter(configuration: DatabaseConfiguration): Filter {
    return { type: FilterType.Base, function: createDefaultFilterFunction(configuration) };
}

function createDefaultFilterFunction(configuration: DatabaseConfiguration): FilterFunction {
    return (neighbour: NodeNeighbour) => neighbour.previousNeighbour ?
        (neighbour.morphism.max === Cardinality.One ? configuration.isPropertyToOneAllowed : configuration.isPropertyToManyAllowed) :
        (neighbour.morphism.max === Cardinality.One ? configuration.isInliningToOneAllowed : configuration.isInliningToManyAllowed);
}

// The path backwards is not allowed unless this node is the current root (i.e. entry morphism is empty)
function filterBackwardPaths(neighbours: NodeNeighbour[], entryMorphism: MorphismData): NodeNeighbour[] {
    const entryBase = entryMorphism.signature.getLastBase();

    return neighbours.filter(neighbour => {
        const base = neighbour.morphism.signature.getLastBase();
        return !(base && entryBase && base.last.isBaseAndDualOf(entryBase.last));
    });
}

function markNeighbour(neighbour: NodeNeighbour, isDirect: boolean): boolean {
    // If the previous node was the root node, this node is definitely available so we mark it this way.
    // Unless there are multiple morphisms between the root node and this one. If that's the case, we have to process it normally.
    if (isDirect) {
        const status = neighbour.node.availabilityStatus === AvailabilityStatus.Default ? AvailabilityStatus.CertainlyAvailable : AvailabilityStatus.Ambiguous;
        neighbour.node.setAvailabilityStatus(status);
        neighbour.node.availablePathData = neighbour.morphism;

        return true;
    }

    // If the node hasn't been traversed yet, it becomes available.
    // Unless it's previous node has been found ambigous - then this node is also ambiguous.
    if (neighbour.node.availabilityStatus === AvailabilityStatus.Default) {
        if (neighbour.previousNode?.availabilityStatus === AvailabilityStatus.Ambiguous) {
            neighbour.node.setAvailabilityStatus(AvailabilityStatus.Ambiguous);
        }
        else {
            neighbour.node.setAvailabilityStatus(AvailabilityStatus.Available);
            neighbour.node.availablePathData = neighbour.morphism;

            // If the previous neighbour is found to be ambiguous, this one is also ambiguous.
            if (neighbour.previousNeighbour)
                neighbour.previousNeighbour.dependentNeighbours.push(neighbour);
        }

        return true;
    }

    // Already traversed path detected. This means we have marked all potentially ambiguous nodes.
    // If the node is in the Ambiguous state, it means we have already processed all its ambiguous paths
    if (neighbour.node.availabilityStatus !== AvailabilityStatus.Ambiguous)
        processAmbiguousPath(neighbour);

    return false;
}

function processAmbiguousPath(lastNeighbour: NodeNeighbour): void {
    let currentNeighbour = lastNeighbour.previousNeighbour;

    while (
        currentNeighbour &&
        !currentNeighbour.node.equals(lastNeighbour.node) &&
        currentNeighbour.node.availabilityStatus !== AvailabilityStatus.CertainlyAvailable
    ) {
        currentNeighbour.node.setAvailabilityStatus(AvailabilityStatus.Ambiguous);
        processAmbiguousDependentNeighbours(currentNeighbour);
        currentNeighbour = currentNeighbour.previousNeighbour;
    }
}

function processAmbiguousDependentNeighbours(neigbour: NodeNeighbour): void {
    neigbour.dependentNeighbours.forEach(dependentNeighbour => {
        if (dependentNeighbour.node.availabilityStatus !== AvailabilityStatus.Ambiguous) {
            dependentNeighbour.node.setAvailabilityStatus(AvailabilityStatus.Ambiguous);
            processAmbiguousDependentNeighbours(dependentNeighbour);
        }
    });
}
