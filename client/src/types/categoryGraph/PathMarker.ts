import type { DatasourceConfiguration } from '../datasource';
import type { Signature } from '../identifiers';
import { Cardinality, type Objex, type Max, type Min, type Morphism } from '../schema';

export function computePathsFromNode(objex: Objex, filterFunction?: FilterFunction) {
    const marker = new PathMarker(objex, filterFunction);
    marker.markPathsFromRootNode();
}

class PathMarker {
    // It's actually important this is a stack and not a queue, because the paths has to be traversed in one go.
    private readonly stack: PathSegment[] = [];

    constructor(
        private readonly rootObjex: Objex,
        private readonly filterFunction?: FilterFunction,
    ) {}

    private readonly nodes = new Map<string, PathNode>();

    private getNode(objex: Objex): PathNode {
        const id = objex.key.toString();

        let node = this.nodes.get(id);
        if (!node) {
            node = { id, availability: Availability.Default };
            this.nodes.set(id, node);
        }

        return node;
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
        this.getNode(this.rootObjex).availability = Availability.Removable;

        /*
        if (isCenteredOnRootNode)
            this.rootNode.select({ type: SelectionType.Root, level: 0 });
        */

        // For the direct neighbors of the root the queue is needed.
        const queue = this.getTraversableNeighbors(this.rootObjex);
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

    private getTraversableNeighbors(from: Objex, previousSegment?: PathSegment): PathSegment[] {
        let neighbors = from.findNeighbourMorphisms()
            .map(morphism => createPathSegment(from, morphism, previousSegment))
            .filter(segment => !segment.to.equals(this.rootObjex));

        // The edges around the root node are clickable so we have to check if they are valid.
        // First we mark all as invalid.
        if (!previousSegment)
            neighbors.forEach(neighbor => neighbor.setTraversible(false));

        if (previousSegment)
            neighbors = this.filterBackwardPaths(neighbors, previousSegment.fullMorphism);

        if (this.filterFunction)
            neighbors = neighbors.filter(this.filterFunction);

        // Then we validate those that survive the filters.
        if (!previousSegment)
            neighbors.forEach(neighbor => neighbor.setTraversible(true));

        // We have to check that we are not going back to the same node.
        // It's permitted, but the user has to specifically require it.
        // To do so, the user has to choose the node as CertainlyAvailable, i.e. as a direct neighbor.
        // However, this is not required for the leaves because for them there is no other option than to go back.
        if (previousSegment && neighbors.length > 1)
            neighbors = neighbors.filter(entry => !entry.to.equals(from));

        return neighbors;
    }

    private processNeighbor(neighbor: PathSegment): void {
        const continueAdding = this.markNeighbor(neighbor);
        if (!continueAdding)
            return;

        const addition = this.getTraversableNeighbors(neighbor.to, neighbor);
        this.stack.push(...addition);
    }

    // The path backwards is not allowed unless this node is the current root (i.e. entry morphism is empty)
    private filterBackwardPaths(neighbors: PathSegment[], entryMorphism: MorphismData): PathSegment[] {
        const entryBase = entryMorphism.signature.getLastBase();

        return neighbors.filter(neighbor => {
            const base = neighbor.fullMorphism.signature.getLastBase();
            return !(base && entryBase && base.last.isBaseAndDualOf(entryBase.last));
        });
    }

    private markNeighbor(neighbor: PathSegment): boolean {
        const toNode = this.getNode(neighbor.to);

        // If the previous node was the root node, this node is definitely available so we mark it this way.
        // Unless there are multiple morphisms between the root node and this one. If that's the case, we have to process it normally.
        if (!neighbor.previousSegment) {
            const status = toNode.availability === Availability.Default ? Availability.CertainlyAvailable : Availability.Ambiguous;
            toNode.availability = status;
            toNode.availablePath = neighbor.fullMorphism;

            return true;
        }

        // If the node hasn't been traversed yet, it becomes available.
        // Unless it's previous node has been found ambigous - then this node is also ambiguous.
        if (toNode.availability === Availability.Default) {
            if (this.getNode(neighbor.from).availability === Availability.Ambiguous) {
                toNode.availability = Availability.Ambiguous;
            }
            else {
                toNode.availability = Availability.Available;
                toNode.availablePath = neighbor.fullMorphism;

                // If the previous neighbor is found to be ambiguous, this one is also ambiguous.
                if (neighbor.previousSegment)
                    neighbor.previousSegment.dependentSegments.push(neighbor);
            }

            return true;
        }

        // Already traversed path detected. This means we have marked all potentially ambiguous nodes.
        // If the node is in the Ambiguous state, it means we have already processed all its ambiguous paths
        if (toNode.availability !== Availability.Ambiguous)
            this.processAmbiguousPath(neighbor);

        return false;
    }

    private processAmbiguousPath(lastNeighbor: PathSegment): void {
        let currentNeighbor = lastNeighbor.previousSegment;

        while (
            currentNeighbor &&
        !currentNeighbor.to.equals(lastNeighbor.to) &&
        this.getNode(currentNeighbor.to).availability !== Availability.CertainlyAvailable
        ) {
            this.getNode(currentNeighbor.to).availability = Availability.Ambiguous;
            this.processAmbiguousDependentSegments(currentNeighbor);
            currentNeighbor = currentNeighbor.previousSegment;
        }
    }

    private processAmbiguousDependentSegments(neigbour: PathSegment): void {
        neigbour.dependentSegments.forEach(dependentNeighbor => {
            const toNode = this.getNode(dependentNeighbor.to);
            if (toNode.availability !== Availability.Ambiguous) {
                toNode.availability = Availability.Ambiguous;
                this.processAmbiguousDependentSegments(dependentNeighbor);
            }
        });
    }
}

type PathNode = {
    id: string;
    availability: Availability;
    availablePath?: MorphismData;
};

export enum Availability {
    Default = 'default',
    Available = 'available',
    CertainlyAvailable = 'certainlyAvailable',
    Ambiguous = 'ambiguous',
    Removable = 'removable',
    NotAvailable = 'notAvailable'
}

// TODO Rename to PathEdge or something?

export type PathSegment = {
    from: Objex;
    to: Objex;
    direction: boolean;
    fullMorphism: MorphismData;

    isTraversible: boolean;
    isTraversibleDual: boolean;

    setTraversible(value: boolean): void;

    previousSegment: PathSegment | undefined;
    dependentSegments: PathSegment[];
};

function createPathSegment(from: Objex, morphism: Morphism, previousSegment?: PathSegment): PathSegment {
    const direction = morphism.from.equals(from);

    const morphismData: MorphismData = {
        signature: morphism.signature,
        min: direction ? morphism.schema.min : Cardinality.Zero,
        max: direction ? Cardinality.One : Cardinality.Star,
    };

    return {
        from,
        to: direction ? morphism.to : morphism.from,
        direction,
        fullMorphism: joinMorphisms(previousSegment?.fullMorphism, morphismData),

        isTraversible: false,
        isTraversibleDual: false,

        setTraversible(value: boolean) {
            if (this.direction)
                this.isTraversible = value;
            else
                this.isTraversibleDual = value;
        },

        previousSegment,
        dependentSegments: [],
    };
}

// TODO Maybe something like Edge here? I.e., "directed morphism"?
type MorphismData = {
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

export type FilterFunction = (segment: PathSegment) => boolean;

export function createDefaultFilter(configuration: DatasourceConfiguration): FilterFunction {
    return (segment: PathSegment) => segment.previousSegment
        ? (segment.fullMorphism.max === Cardinality.One ? configuration.isPropertyToOneAllowed : configuration.isPropertyToManyAllowed)
        : (segment.fullMorphism.max === Cardinality.One ? configuration.isInliningToOneAllowed : configuration.isInliningToManyAllowed);
}
