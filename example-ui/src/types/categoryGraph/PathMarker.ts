import type { DatabaseConfiguration } from "../database";
import type { Signature } from "../identifiers";
import { Cardinality, type Max, type Min } from "../schema";
import { type Node, AvailabilityStatus } from "./Node";

export type MorphismData = {
    signature: Signature,
    min: Min,
    max: Max
}

function combineMorphismData(first: MorphismData, second: MorphismData): MorphismData {
    return {
        signature: first.signature.concatenate(second.signature),
        min: first.min === Cardinality.One && second.min === Cardinality.One ? Cardinality.One : Cardinality.Zero,
        max: first.max === Cardinality.One && second.max === Cardinality.One ? Cardinality.One : Cardinality.Star
    };
}

function joinMorphisms(parent: MorphismData | undefined, next: MorphismData) {
    return parent ? combineMorphismData(parent, next) : next;
}

function joinDuals(parent: MorphismData | undefined, next: MorphismData) {
    return parent ? combineMorphismData(next, parent) : next;
}

export type PathSegment = {
    targetNode: Node,
    sourceNode: Node,
    morphism: MorphismData,
    fullMorphism: MorphismData,
    dual: MorphismData,
    fullDual: MorphismData,
    previousSegment: PathSegment | undefined,
    dependentSegments: PathSegment[]
}

export type FilterFunction = (segment: PathSegment) => boolean;

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

    readonly stack = [] as PathSegment[];

    constructor(rootNode: Node, filters: Filter | Filter[]) {
        this.rootNode = rootNode;
        const filtersArray = Array.isArray(filters) ? filters : [ filters ];

        this.baseFilters = filtersArray.filter(filter => filter.type === FilterType.Base).map(filter => filter.function);
        this.compositeFilters = filtersArray.filter(filter => filter.type === FilterType.Composite).map(filter => filter.function);
    }

    getTraversableNeighbours(sourceNode: Node, parentSegment?: PathSegment): PathSegment[] {
        let neighbours = sourceNode.adjacentEdges
            .map(edge => ({
                targetNode: edge.codomainNode,
                sourceNode,
                morphism: joinMorphisms(parentSegment?.morphism, edge.schemaMorphism),
                fullMorphism: joinMorphisms(parentSegment?.fullMorphism, edge.schemaMorphism),
                dual: joinDuals(parentSegment?.dual, edge.schemaMorphism.dual),
                fullDual: joinDuals(parentSegment?.fullDual, edge.schemaMorphism.dual),
                previousSegment: parentSegment,
                dependentSegments: [] as PathSegment[]
            }))
            .filter(neighbour => !neighbour.targetNode.equals(this.rootNode));

        if (parentSegment)
            neighbours = filterBackwardPaths(neighbours, parentSegment.morphism);

        for (const filterFunction of this.baseFilters)
            neighbours = neighbours.filter(filterFunction);

        // We have to check that we are not going back to the same node.
        // It's permitted, but the user has to specifically require it.
        // To do so, the user has to choose the node as CertainlyAvailable, i.e. as a direct neighbour.
        // However, this is not required for the leaves because for them there is no other option than to go back.
        if (parentSegment && neighbours.length > 1)
            neighbours = neighbours.filter(entry => !entry.targetNode.equals(sourceNode));

        return neighbours;
    }

    processNeighbour(neighbour: PathSegment, isDirect = false): void {
        const continueAdding = markNeighbour(neighbour, isDirect);
        if (!continueAdding)
            return;

        const addition = this.getTraversableNeighbours(neighbour.targetNode, neighbour);
        this.stack.push(...addition);
    }

    checkCompositeFilters(neighbour: PathSegment): boolean {
        for (const filterFunction of this.compositeFilters) {
            if (!filterFunction(neighbour))
                return false;
        }

        return true;
    }

    /**
     * # A comment to the previous system:
     *
     * In general, it isn't possible mark all nodes perfectly. The rules are:
     * - a) A node is ambiguos if there are multiple valid paths to it.
     * - b) The validity of the path can be found only after we know the whole path.
     * - c) We don't want to consider paths with loops (or, in general, paths that contain a node more than once).
     * The problem is that:
     * - a) and b) means we might need to evaluate all possible paths to check if multiple of them are valid.
     * - One possible outcome is that there are multiple paths but none of them are valid.
     * - However, it may happen that if we consider loops, we will find paths that will be valid.
     * - So the nodes should be accessible but still ambiguous.
     *
     * # Nevertheless, the process of selecting signature is continuous. It should be possible to select a node because even if path to it isn't valid the node might actually be in the middle of the path, not at the end of it, because the process isn't over yet.
     *
     * # So yes, the Composite filters are indeed an unsufficient solution.
     */
    markPathsFromRootNode(): void {
        this.rootNode.setAvailabilityStatus(AvailabilityStatus.Removable);

        /*
        if (isCenteredOnRootNode)
            this.rootNode.select({ type: SelectionType.Root, level: 0 });
        */

        // For the direct neighbours of the root the queue is needed.
        const queue = this.getTraversableNeighbours(this.rootNode);
        const allProcessedNeighbours = [] as PathSegment[];

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

        // Filter out paths that are not available.
        allProcessedNeighbours.forEach(neighbour => {
            if (!this.checkCompositeFilters(neighbour))
                neighbour.targetNode.setAvailabilityStatus(AvailabilityStatus.NotAvailable);
        });
    }
}

export function createDefaultFilter(configuration: DatabaseConfiguration): Filter {
    return { type: FilterType.Base, function: createDefaultFilterFunction(configuration) };
}

function createDefaultFilterFunction(configuration: DatabaseConfiguration): FilterFunction {
    return (neighbour: PathSegment) => neighbour.previousSegment ?
        (neighbour.morphism.max === Cardinality.One ? configuration.isPropertyToOneAllowed : configuration.isPropertyToManyAllowed) :
        (neighbour.morphism.max === Cardinality.One ? configuration.isInliningToOneAllowed : configuration.isInliningToManyAllowed);
}

// The path backwards is not allowed unless this node is the current root (i.e. entry morphism is empty)
function filterBackwardPaths(neighbours: PathSegment[], entryMorphism: MorphismData): PathSegment[] {
    const entryBase = entryMorphism.signature.getLastBase();

    return neighbours.filter(neighbour => {
        const base = neighbour.morphism.signature.getLastBase();
        return !(base && entryBase && base.last.isBaseAndDualOf(entryBase.last));
    });
}

function markNeighbour(neighbour: PathSegment, isDirect: boolean): boolean {
    // If the previous node was the root node, this node is definitely available so we mark it this way.
    // Unless there are multiple morphisms between the root node and this one. If that's the case, we have to process it normally.
    if (isDirect) {
        const status = neighbour.targetNode.availabilityStatus === AvailabilityStatus.Default ? AvailabilityStatus.CertainlyAvailable : AvailabilityStatus.Ambiguous;
        neighbour.targetNode.setAvailabilityStatus(status);
        neighbour.targetNode.availablePathData = neighbour.morphism;

        return true;
    }

    // If the node hasn't been traversed yet, it becomes available.
    // Unless it's previous node has been found ambigous - then this node is also ambiguous.
    if (neighbour.targetNode.availabilityStatus === AvailabilityStatus.Default) {
        if (neighbour.sourceNode?.availabilityStatus === AvailabilityStatus.Ambiguous) {
            neighbour.targetNode.setAvailabilityStatus(AvailabilityStatus.Ambiguous);
        }
        else {
            neighbour.targetNode.setAvailabilityStatus(AvailabilityStatus.Available);
            neighbour.targetNode.availablePathData = neighbour.morphism;

            // If the previous neighbour is found to be ambiguous, this one is also ambiguous.
            if (neighbour.previousSegment)
                neighbour.previousSegment.dependentSegments.push(neighbour);
        }

        return true;
    }

    // Already traversed path detected. This means we have marked all potentially ambiguous nodes.
    // If the node is in the Ambiguous state, it means we have already processed all its ambiguous paths
    if (neighbour.targetNode.availabilityStatus !== AvailabilityStatus.Ambiguous)
        processAmbiguousPath(neighbour);

    return false;
}

function processAmbiguousPath(lastNeighbour: PathSegment): void {
    let currentNeighbour = lastNeighbour.previousSegment;

    while (
        currentNeighbour &&
        !currentNeighbour.targetNode.equals(lastNeighbour.targetNode) &&
        currentNeighbour.targetNode.availabilityStatus !== AvailabilityStatus.CertainlyAvailable
    ) {
        currentNeighbour.targetNode.setAvailabilityStatus(AvailabilityStatus.Ambiguous);
        processAmbiguousDependentSegments(currentNeighbour);
        currentNeighbour = currentNeighbour.previousSegment;
    }
}

function processAmbiguousDependentSegments(neigbour: PathSegment): void {
    neigbour.dependentSegments.forEach(dependentNeighbour => {
        if (dependentNeighbour.targetNode.availabilityStatus !== AvailabilityStatus.Ambiguous) {
            dependentNeighbour.targetNode.setAvailabilityStatus(AvailabilityStatus.Ambiguous);
            processAmbiguousDependentSegments(dependentNeighbour);
        }
    });
}
