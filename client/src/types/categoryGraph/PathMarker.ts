import type { DatasourceConfiguration } from '../datasource';
import type { Signature } from '../identifiers';
import { Cardinality, type Max, type Min } from '../schema';
import type { Edge } from './Edge';
import { type Node, AvailabilityStatus, type Neighbor } from './Node';

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

/** @deprecated */
export class PathMarker {
    // It's actually important this is a stack and not a queue, because the paths has to be traversed in one go.
    rootNode: Node;

    filterFunctions: FilterFunction[];

    readonly stack: PathSegment[] = [];

    constructor(rootNode: Node, filter: Filter) {
        this.rootNode = rootNode;
        this.filterFunctions = Array.isArray(filter.function) ? filter.function : [ filter.function ];
    }

    getTraversableNeighbors(sourceNode: Node, previousSegment?: PathSegment): PathSegment[] {
        let neighbors = sourceNode.neighbors
            .map(neighbor => createPathSegment(sourceNode, neighbor, previousSegment))
            .filter(segment => !segment.targetNode.equals(this.rootNode));

        // The edges around the root node are clickable so we have to check if they are valid.
        // First we mark all as invalid.
        if (!previousSegment)
            neighbors.forEach(neighbor => neighbor.edge.setTraversible(neighbor.direction, false));

        if (previousSegment)
            neighbors = filterBackwardPaths(neighbors, previousSegment.fullMorphism);

        for (const filterFunction of this.filterFunctions)
            neighbors = neighbors.filter(filterFunction);

        // Then we validate those that survive the filters.
        if (!previousSegment)
            neighbors.forEach(neighbor => neighbor.edge.setTraversible(neighbor.direction, true));

        // We have to check that we are not going back to the same node.
        // It's permitted, but the user has to specifically require it.
        // To do so, the user has to choose the node as CertainlyAvailable, i.e. as a direct neighbor.
        // However, this is not required for the leaves because for them there is no other option than to go back.
        if (previousSegment && neighbors.length > 1)
            neighbors = neighbors.filter(entry => !entry.targetNode.equals(sourceNode));

        return neighbors;
    }

    processNeighbor(neighbor: PathSegment): void {
        const continueAdding = markNeighbor(neighbor);
        if (!continueAdding)
            return;

        const addition = this.getTraversableNeighbors(neighbor.targetNode, neighbor);
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

        // For the direct neighbors of the root the queue is needed.
        const queue = this.getTraversableNeighbors(this.rootNode);
        const allProcessedNeighbors: PathSegment[] = [];

        let directNeighbor = queue.shift();
        while (directNeighbor) {
            allProcessedNeighbors.push(directNeighbor);
            this.processNeighbor(directNeighbor);
            directNeighbor = queue.shift();
        }

        let indirectNeighbor = this.stack.pop();
        while (indirectNeighbor) {
            allProcessedNeighbors.push(indirectNeighbor);
            this.processNeighbor(indirectNeighbor);
            indirectNeighbor = this.stack.pop();
        }
    }
}

function createPathSegment(sourceNode: Node, neighbor: Neighbor, previousSegment?: PathSegment): PathSegment {
    const direction = neighbor.direction;
    const edge = neighbor.edge;

    const morphismData: MorphismData = {
        signature: neighbor.signature,
        min: direction ? edge.schemaMorphism.min : Cardinality.Zero,
        max: direction ? Cardinality.One : Cardinality.Star,
    };

    return {
        targetNode: neighbor.node,
        sourceNode,
        edge,
        direction,
        fullMorphism: joinMorphisms(previousSegment?.fullMorphism, morphismData),
        previousSegment,
        dependentSegments: [] as PathSegment[],
    };
}

export function createDefaultFilter(configuration: DatasourceConfiguration): Filter {
    return {
        function: (segment: PathSegment) => segment.previousSegment
            ? (segment.fullMorphism.max === Cardinality.One ? configuration.isPropertyToOneAllowed : configuration.isPropertyToManyAllowed)
            : (segment.fullMorphism.max === Cardinality.One ? configuration.isInliningToOneAllowed : configuration.isInliningToManyAllowed),
    };
}

// The path backwards is not allowed unless this node is the current root (i.e. entry morphism is empty)
function filterBackwardPaths(neighbors: PathSegment[], entryMorphism: MorphismData): PathSegment[] {
    const entryBase = entryMorphism.signature.getLastBase();

    return neighbors.filter(neighbor => {
        const base = neighbor.fullMorphism.signature.getLastBase();
        return !(base && entryBase && base.last.isBaseAndDualOf(entryBase.last));
    });
}

function markNeighbor(neighbor: PathSegment): boolean {
    // If the previous node was the root node, this node is definitely available so we mark it this way.
    // Unless there are multiple morphisms between the root node and this one. If that's the case, we have to process it normally.
    if (!neighbor.previousSegment) {
        const status = neighbor.targetNode.availabilityStatus === AvailabilityStatus.Default ? AvailabilityStatus.CertainlyAvailable : AvailabilityStatus.Ambiguous;
        neighbor.targetNode.setAvailabilityStatus(status);
        neighbor.targetNode.availablePathData = neighbor.fullMorphism;

        return true;
    }

    // If the node hasn't been traversed yet, it becomes available.
    // Unless it's previous node has been found ambigous - then this node is also ambiguous.
    if (neighbor.targetNode.availabilityStatus === AvailabilityStatus.Default) {
        if (neighbor.sourceNode?.availabilityStatus === AvailabilityStatus.Ambiguous) {
            neighbor.targetNode.setAvailabilityStatus(AvailabilityStatus.Ambiguous);
        }
        else {
            neighbor.targetNode.setAvailabilityStatus(AvailabilityStatus.Available);
            neighbor.targetNode.availablePathData = neighbor.fullMorphism;

            // If the previous neighbor is found to be ambiguous, this one is also ambiguous.
            if (neighbor.previousSegment)
                neighbor.previousSegment.dependentSegments.push(neighbor);
        }

        return true;
    }

    // Already traversed path detected. This means we have marked all potentially ambiguous nodes.
    // If the node is in the Ambiguous state, it means we have already processed all its ambiguous paths
    if (neighbor.targetNode.availabilityStatus !== AvailabilityStatus.Ambiguous)
        processAmbiguousPath(neighbor);

    return false;
}

function processAmbiguousPath(lastNeighbor: PathSegment): void {
    let currentNeighbor = lastNeighbor.previousSegment;

    while (
        currentNeighbor &&
        !currentNeighbor.targetNode.equals(lastNeighbor.targetNode) &&
        currentNeighbor.targetNode.availabilityStatus !== AvailabilityStatus.CertainlyAvailable
    ) {
        currentNeighbor.targetNode.setAvailabilityStatus(AvailabilityStatus.Ambiguous);
        processAmbiguousDependentSegments(currentNeighbor);
        currentNeighbor = currentNeighbor.previousSegment;
    }
}

function processAmbiguousDependentSegments(neigbour: PathSegment): void {
    neigbour.dependentSegments.forEach(dependentNeighbor => {
        if (dependentNeighbor.targetNode.availabilityStatus !== AvailabilityStatus.Ambiguous) {
            dependentNeighbor.targetNode.setAvailabilityStatus(AvailabilityStatus.Ambiguous);
            processAmbiguousDependentSegments(dependentNeighbor);
        }
    });
}
