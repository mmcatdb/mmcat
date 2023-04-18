import type { DatabaseConfiguration } from "../database";
import type { Signature } from "../identifiers";
import { Cardinality, type Max, type Min } from "../schema";
import type { Edge } from "./Edge";
import { type Node, AvailabilityStatus, Neighbour } from "./Node";

export type MorphismData = {
    signature: Signature;
    min: Min;
    max: Max;
};

function combineMorphismData(first: MorphismData, second: MorphismData): MorphismData {
    return {
        signature: first.signature.concatenate(second.signature),
        min: first.min === Cardinality.One && second.min === Cardinality.One ? Cardinality.One : Cardinality.Zero,
        max: first.max === Cardinality.One && second.max === Cardinality.One ? Cardinality.One : Cardinality.Star,
    };
}

function joinMorphisms(parent: MorphismData | undefined, next: MorphismData): MorphismData {
    return parent ? combineMorphismData(parent, next) : next;
}

export type PathSegment = {
    sourceNode: Node;
    targetNode: Node;
    edge: Edge;
    direction: boolean;
    fullMorphism: MorphismData;
    previousSegment: PathSegment | undefined;
    dependentSegments: PathSegment[];
};

export type FilterFunction = (segment: PathSegment) => boolean;

export type Filter = {
    function: FilterFunction | FilterFunction[];
};

export class PathMarker {
    // It's actually important this is a stack and not a queue, because the paths has to be traversed in one go.
    rootNode: Node;

    filterFunctions: FilterFunction[];

    readonly stack = [] as PathSegment[];

    constructor(rootNode: Node, filter: Filter) {
        this.rootNode = rootNode;
        this.filterFunctions = Array.isArray(filter.function) ? filter.function : [ filter.function ];
    }

    getTraversableNeighbours(sourceNode: Node, previousSegment?: PathSegment): PathSegment[] {
        let neighbours = sourceNode.neighbours
            .map(neighbour => createPathSegment(sourceNode, neighbour, previousSegment))
            .filter(segment => !segment.targetNode.equals(this.rootNode));

        // The edges around the root node are clickable so we have to check if they are valid.
        // First we mark all as invalid.
        if (!previousSegment)
            neighbours.forEach(neighbour => neighbour.edge.setTraversible(neighbour.direction, false));

        if (previousSegment)
            neighbours = filterBackwardPaths(neighbours, previousSegment.fullMorphism);

        for (const filterFunction of this.filterFunctions)
            neighbours = neighbours.filter(filterFunction);

        // Then we validate those that survive the filters.
        if (!previousSegment)
            neighbours.forEach(neighbour => neighbour.edge.setTraversible(neighbour.direction, true));

        // We have to check that we are not going back to the same node.
        // It's permitted, but the user has to specifically require it.
        // To do so, the user has to choose the node as CertainlyAvailable, i.e. as a direct neighbour.
        // However, this is not required for the leaves because for them there is no other option than to go back.
        if (previousSegment && neighbours.length > 1)
            neighbours = neighbours.filter(entry => !entry.targetNode.equals(sourceNode));

        return neighbours;
    }

    processNeighbour(neighbour: PathSegment): void {
        const continueAdding = markNeighbour(neighbour);
        if (!continueAdding)
            return;

        const addition = this.getTraversableNeighbours(neighbour.targetNode, neighbour);
        this.stack.push(...addition);
    }

    /**
     * # A comment to the previous system:
     *
     * In general, it isn't possible mark all nodes perfectly. The rules are:
     * - a) A node is ambiguous if there are multiple valid paths to it.
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
            this.processNeighbour(directNeighbour);
            directNeighbour = queue.shift();
        }

        let indirectNeighbour = this.stack.pop();
        while (indirectNeighbour) {
            allProcessedNeighbours.push(indirectNeighbour);
            this.processNeighbour(indirectNeighbour);
            indirectNeighbour = this.stack.pop();
        }
    }
}

function createPathSegment(sourceNode: Node, neighbour: Neighbour, previousSegment?: PathSegment): PathSegment {
    const direction = neighbour.direction;
    const edge = neighbour.edge;

    const morphismData: MorphismData = {
        signature: neighbour.signature,
        min: direction ? edge.schemaMorphism.min : Cardinality.Zero,
        max: direction ? Cardinality.One : Cardinality.Star,
    };

    return {
        targetNode: neighbour.node,
        sourceNode,
        edge,
        direction,
        fullMorphism: joinMorphisms(previousSegment?.fullMorphism, morphismData),
        previousSegment,
        dependentSegments: [] as PathSegment[],
    };
}

export function createDefaultFilter(configuration: DatabaseConfiguration): Filter {
    return {
        function: (segment: PathSegment) => segment.previousSegment
            ? (segment.fullMorphism.max === Cardinality.One ? configuration.isPropertyToOneAllowed : configuration.isPropertyToManyAllowed)
            : (segment.fullMorphism.max === Cardinality.One ? configuration.isInliningToOneAllowed : configuration.isInliningToManyAllowed),
    };
}

// The path backwards is not allowed unless this node is the current root (i.e. entry morphism is empty)
function filterBackwardPaths(neighbours: PathSegment[], entryMorphism: MorphismData): PathSegment[] {
    const entryBase = entryMorphism.signature.getLastBase();

    return neighbours.filter(neighbour => {
        const base = neighbour.fullMorphism.signature.getLastBase();
        return !(base && entryBase && base.last.isBaseAndDualOf(entryBase.last));
    });
}

function markNeighbour(neighbour: PathSegment): boolean {
    // If the previous node was the root node, this node is definitely available so we mark it this way.
    // Unless there are multiple morphisms between the root node and this one. If that's the case, we have to process it normally.
    if (!neighbour.previousSegment) {
        const status = neighbour.targetNode.availabilityStatus === AvailabilityStatus.Default ? AvailabilityStatus.CertainlyAvailable : AvailabilityStatus.Ambiguous;
        neighbour.targetNode.setAvailabilityStatus(status);
        neighbour.targetNode.availablePathData = neighbour.fullMorphism;

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
            neighbour.targetNode.availablePathData = neighbour.fullMorphism;

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
