import { DatabaseConfiguration } from "../database";
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
    previousNeighbour: NodeNeighbour | undefined,
    dependentNeighbours: NodeNeighbour[]
}

export type FilterFunction = (neighbour: NodeNeighbour) => boolean;

export class PathMarker {
    // It's actually important this is a stack and not a queue, because the paths has to be traversed from in one go.
    rootNode: Node;
    filterFunction: FilterFunction;

    readonly stack = [] as NodeNeighbour[];

    constructor(rootNode: Node, filterOptions: DatabaseConfiguration | FilterFunction) {
        this.rootNode = rootNode;
        this.filterFunction = filterOptions instanceof DatabaseConfiguration ?
            createDefaultFilterFunction(filterOptions) :
            filterOptions;
    }

    getTraversableNeighbours(parentNode: Node, parentNeighbour?: NodeNeighbour): NodeNeighbour[] {
        const combineFunction = parentNeighbour ?
            (morphism: SchemaMorphism) => combineData(parentNeighbour.morphism, morphism) :
            (morphism: SchemaMorphism) => morphismToData(morphism);

        let neighbours = [ ...parentNode.neighbours.entries() ]
            .map(([ childNode, morphism ]) => ({
                node: childNode,
                previousNode: parentNode,
                morphism: combineFunction(morphism),
                previousNeighbour: parentNeighbour,
                dependentNeighbours: [] as NodeNeighbour[]
            }))
            .filter(neighbour => !neighbour.node.equals(this.rootNode));

        if (parentNeighbour)
            neighbours = filterBackwardPaths(neighbours, parentNeighbour.morphism);

        neighbours = neighbours.filter(this.filterFunction);

        // We have to check that we are not going back to the same node.
        // It's permitted, but the user has to specifically require it.
        // To do so, the user has to choose the node as CertainlyAvailable, i.e. as a direct neighbour.
        // However, this is not required for the leaves because for them there is no other option than to go back.
        if (parentNeighbour && neighbours.length > 1)
            neighbours = neighbours.filter(entry => !entry.node.equals(parentNode));

        return neighbours;
    }

    processNeighbour(neighbour: NodeNeighbour, isDirect = false): void {
        // If the previous node was the root node, this node is definitely available so we mark it this way.
        if (isDirect) {
            neighbour.node.setAvailabilityStatus(AvailabilityStatus.CertainlyAvailable);
            neighbour.node.availablePathData = neighbour.morphism;
        }
        // If the node hasn't been traversed yet, it becomes available.
        // Unless it's previous node has been found ambigous - then this node is also ambiguous.
        else if (neighbour.node.availabilityStatus === AvailabilityStatus.Default) {
            if (neighbour.previousNode?.availabilityStatus === AvailabilityStatus.Maybe) {
                neighbour.node.setAvailabilityStatus(AvailabilityStatus.Maybe);
            }
            else {
                neighbour.node.setAvailabilityStatus(AvailabilityStatus.Available);
                neighbour.node.availablePathData = neighbour.morphism;

                // If the previous neighbour is found to be ambiguous, this one is also ambiguous.
                if (neighbour.previousNeighbour)
                    neighbour.previousNeighbour.dependentNeighbours.push(neighbour);
            }
        }
        // Already traversed path detected. This means we have mark all potentially ambiguous nodes.
        else {
            // If the node is already in the maybe state, it means we have already processed all its ambiguous paths
            if (neighbour.node.availabilityStatus !== AvailabilityStatus.Maybe)
                this.processAmbiguousPath(neighbour);

            return;
        }

        const addition = this.getTraversableNeighbours(neighbour.node, neighbour);
        this.stack.push(...addition);
    }

    processAmbiguousPath(lastNeighbour: NodeNeighbour): void {
        let currentNeigbour = lastNeighbour.previousNeighbour;

        while (
            currentNeigbour &&
            !currentNeigbour.node.equals(lastNeighbour.node) &&
            currentNeigbour.node.availabilityStatus !== AvailabilityStatus.CertainlyAvailable
        ) {
            currentNeigbour.node.setAvailabilityStatus(AvailabilityStatus.Maybe);
            this.processAmbiguousDependentNeighbours(currentNeigbour);
            currentNeigbour = currentNeigbour.previousNeighbour;
        }
    }

    processAmbiguousDependentNeighbours(neigbour: NodeNeighbour): void {
        neigbour.dependentNeighbours.forEach(dependentNeighbour => {
            if (dependentNeighbour.node.availabilityStatus !== AvailabilityStatus.Maybe) {
                dependentNeighbour.node.setAvailabilityStatus(AvailabilityStatus.Maybe);
                this.processAmbiguousDependentNeighbours(dependentNeighbour);
            }
        });
    }

    markPathsFromRootNode(): void {
        this.rootNode.setAvailabilityStatus(AvailabilityStatus.Removable);

        /*
        if (isCenteredOnRootNode)
            this.rootNode.select({ type: SelectionType.Root, level: 0 });
        */

        // For the direct neighbours of the root the queue is needed.
        const queue = this.getTraversableNeighbours(this.rootNode);

        let directNeighbour = queue.shift();
        while (directNeighbour) {
            this.processNeighbour(directNeighbour, true);
            directNeighbour = queue.shift();
        }

        let neighbour = this.stack.pop();
        while (neighbour) {
            this.processNeighbour(neighbour);
            neighbour = this.stack.pop();
        }
    }
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
