import type { DatasourceConfiguration } from '../datasource';
import type { Signature } from '../identifiers';
import { Cardinality, type Objex, type Max, type Min, type Morphism } from '.';

/**
 * Nodes and edges correspond to the objexes and morphisms from the category.
 * Contains information about paths from a selected objex.
 */
export type PathGraph = {
    source: Objex;
    nodes: Map<string, PathNode>;
    edges: Map<string, PathEdge>;
};

export type PathNode = {
    id: string;
    objex: Objex;
    pathCount: PathCount;
    /** If defined, there is a path from the source objex to this one. It might be ambiguous (in that case, only one of them is listed here). */
    pathSegmentTo?: PathSegment;
    /** If this node will ever be found ambiguous, these ones will also be marked as such. */
    dependentNodes: PathNode[];
};

function createPathNode(objex: Objex): PathNode {
    return {
        id: objex.key.toString(),
        objex,
        pathCount: PathCount.None,
        dependentNodes: [],
    };
}

/** How many paths from the source there are to this one. The default options is None. */
export enum PathCount {
    None = 'none',
    One = 'one',
    Many = 'many',
}

type Path = {
    /** The full signature from the source node to here. */
    signature: Signature;
    min: Min;
    max: Max;
};

function extendPath(path: Path, second: Path): Path {
    return {
        signature: path.signature.concatenate(second.signature),
        min: path.min === Cardinality.One && second.min === Cardinality.One ? Cardinality.One : Cardinality.Zero,
        max: path.max === Cardinality.One && second.max === Cardinality.One ? Cardinality.One : Cardinality.Star,
    };
}

export type PathEdge = {
    id: string;
    morphism: Morphism;

    /**
     * If defined, this is a direction of at least one traversable path that uses this edge. However, there might be multiple paths.
     * The edge is traversable if there is only one such path. I.e., one of its nodes is {@link PathCount.One} and the other is not {@link PathCount.None}.
     */
    traversableDirection?: boolean;
};

function createPathEdge(morphism: Morphism): PathEdge {
    return {
        id: morphism.signature.toString(),
        morphism,
    };
}

/** Corresponds to a specific edge in the graph. There might be multiple segments for the same edge. */
type PathSegment = {
    id: string;
    edge: PathEdge;
    from: Objex;
    to: Objex;

    markTraversable(): void;

    fullPath: Path;
    prevSegment: PathSegment | undefined;
    dependentSegments: PathSegment[];
};

export function computePathsFromObjex(source: Objex, filterFunction?: FilterFunction): PathGraph {
    const marker = new PathMarker(source, filterFunction);

    marker.markPathsFromSourceObjex();

    return {
        source,
        nodes: marker.nodes,
        edges: marker.edges,
    };
}

class PathMarker {
    // It's actually important this is a stack and not a queue, because the paths has to be traversed in one go.
    private readonly stack: PathSegment[] = [];

    constructor(
        private readonly sourceObjex: Objex,
        private readonly filterFunction?: FilterFunction,
    ) {}

    readonly nodes = new Map<string, PathNode>();

    private getNode(objex: Objex): PathNode {
        const id = objex.key.toString();

        let node = this.nodes.get(id);
        if (!node) {
            node = createPathNode(objex);
            this.nodes.set(id, node);
        }

        return node;
    }

    readonly edges = new Map<string, PathEdge>();

    private getEdge(morphism: Morphism): PathEdge {
        const id = morphism.signature.toString();

        let edge = this.edges.get(id);
        if (!edge) {
            edge = createPathEdge(morphism);
            this.edges.set(id, edge);
        }

        return edge;
    }

    private createPathSegment(from: Objex, morphism: Morphism, prevSegment?: PathSegment): PathSegment {
        const isSameDirection = morphism.from.equals(from);

        const segmentPath: Path = {
            signature: morphism.signature,
            min: isSameDirection ? morphism.schema.min : Cardinality.Zero,
            max: isSameDirection ? Cardinality.One : Cardinality.Star,
        };

        return {
            id: morphism.signature.toString(),
            from,
            to: isSameDirection ? morphism.to : morphism.from,
            edge: this.getEdge(morphism),

            markTraversable() {
                this.edge.traversableDirection = isSameDirection;
            },

            fullPath: prevSegment ? extendPath(prevSegment.fullPath, segmentPath) : segmentPath,
            prevSegment,
            dependentSegments: [],
        };
    }

    // A comment to the previous system:
    //
    // In general, it isn't possible mark all nodes perfectly. The rules are:
    // - a) A node is ambiguous if there are multiple valid paths to it.
    // - b) The validity of the path can be found only after we know the whole path.
    // - c) We don't want to consider paths with loops (or, in general, paths that contain the node more than once).
    // The problem is that:
    // - a) and b) means that we might need to evaluate all possible paths to check if multiple of them are valid.
    // - One possible outcome is that there are multiple paths but none of them are valid.
    // - However, it may happen that if we consider loops, we will find paths that will be valid.
    // - So the nodes should be accessible but still ambiguous.
    //
    // Nevertheless, the process of selecting signature is continuous. It should be possible to select a node because even if path to it isn't valid the node might actually be in the middle of the path, not at the end of it, because the process isn't over yet.
    //
    // So yes, the Composite filters are indeed an unsufficient solution.

    markPathsFromSourceObjex(): void {
        this.getNode(this.sourceObjex).pathCount = PathCount.One;

        // A queue is needed for the direct neighbors of the source.
        const queue = this.getTraversableNeighbors(this.sourceObjex);

        let directNeighbor = queue.shift();
        while (directNeighbor) {
            this.processNeighbor(directNeighbor);
            directNeighbor = queue.shift();
        }

        let indirectNeighbor = this.stack.pop();
        while (indirectNeighbor) {
            this.processNeighbor(indirectNeighbor);
            indirectNeighbor = this.stack.pop();
        }
    }

    private processNeighbor(neighbor: PathSegment): void {
        const continueAdding = this.markNeighbor(neighbor);
        if (!continueAdding)
            return;

        const addition = this.getTraversableNeighbors(neighbor.to, neighbor);
        this.stack.push(...addition);
    }

    private getTraversableNeighbors(from: Objex, prevSegment?: PathSegment): PathSegment[] {
        let neighbors = from.findNeighborMorphisms()
            .map(morphism => this.createPathSegment(from, morphism, prevSegment))
            // No need to go back to the source.
            .filter(neighbor => !neighbor.to.equals(this.sourceObjex));

        if (prevSegment)
            // We march to victory, or we march to defeat. But we go forward, only forward.
            neighbors = neighbors.filter(neighbor => !neighbor.to.equals(prevSegment.from));

        if (this.filterFunction)
            neighbors = neighbors.filter(this.filterFunction);

        return neighbors;
    }

    /**
     * Continues the path to the To node of the neighbor.
     * @returns Whether we should continue traversing the neighbors of this node.
     */
    private markNeighbor(neighbor: PathSegment): boolean {
        neighbor.markTraversable();

        const fromNode = this.getNode(neighbor.from);
        const toNode = this.getNode(neighbor.to);

        // If the previous node was the source node, this node is definitely available so we mark it this way.
        // However, it still might be ambiguous.
        if (!neighbor.prevSegment) {
            toNode.pathCount = toNode.pathCount === PathCount.None ? PathCount.One : PathCount.Many;
            toNode.pathSegmentTo = neighbor;

            return true;
        }

        // The To node hasn't been traversed yet, it becomes reachable.
        if (toNode.pathCount === PathCount.None) {
            if (fromNode.pathCount === PathCount.Many) {
                // The previous node is ambiguous, so this one is also ambiguous.
                toNode.pathCount = PathCount.Many;
            }
            else {
                toNode.pathCount = PathCount.One;
                toNode.pathSegmentTo = neighbor;

                // If the previous node will ever be found ambiguous, this one will also be marked as such.
                if (neighbor.prevSegment)
                    fromNode.dependentNodes.push(toNode);
                    // neighbor.prevSegment.dependentSegments.push(neighbor);

            }

            return true;
        }

        // We are not in this node for the first time! If it's already marked as ambiguous, we don't need to do anything.
        if (toNode.pathCount !== PathCount.Many)
            this.processAmbiguousPath(neighbor);

        // No need to continue traversing the neighbors of this node - we have already been here before.
        return false;
    }

    private processAmbiguousPath(newSegment: PathSegment): void {
        // Let's travel back to the last common node. Both paths are ambiguous from this point.
        // We met here!
        const contactNode = this.getNode(newSegment.to);

        // The first nodes after the common node on the first and second paths.
        let firstPathStart: PathNode | undefined;
        let secondPathStart: PathNode | undefined;

        // We are going back to the source. We collect all nodes except the source node.
        const visitedNodes = new Set<string>();
        let current = contactNode;
        while (current.pathSegmentTo) {
            visitedNodes.add(current.id);
            current = this.getNode(current.pathSegmentTo.from);
        }

        // The source node is the common node until proven otherwise.
        let commonNode = this.getNode(this.sourceObjex);

        // We go back along the other path and we look for the first common node. We also need to find the first node after the common one.
        current = this.getNode(newSegment.from);
        while (current.pathSegmentTo) {
            if (visitedNodes.has(current.id)) {
                commonNode = current;
                break;
            }

            secondPathStart = current;
            current = this.getNode(current.pathSegmentTo.from);
        }

        current = contactNode;
        while (current !== commonNode) {
            firstPathStart = current;
            current = this.getNode(current.pathSegmentTo!.from);
        }

        // Now we go back along the first path and find the first node after the common one.

        // Finally, we mark all nodes on both paths except the common node as ambiguous.
        if (firstPathStart)
            this.markNodeAmbiguous(firstPathStart);
        if (secondPathStart)
            this.markNodeAmbiguous(secondPathStart);
    }

    private markNodeAmbiguous(node: PathNode): void {
        node.pathCount = PathCount.Many;
        node.dependentNodes.forEach(toNode => {
            if (toNode.pathCount !== PathCount.Many)
                this.markNodeAmbiguous(toNode);
        });
    }
}

export type FilterFunction = (segment: PathSegment) => boolean;

export function createDefaultFilter(configuration: DatasourceConfiguration): FilterFunction {
    return (segment: PathSegment) => segment.prevSegment
        ? (segment.fullPath.max === Cardinality.One ? configuration.isPropertyToOneAllowed : configuration.isPropertyToManyAllowed)
        : (segment.fullPath.max === Cardinality.One ? configuration.isInliningToOneAllowed : configuration.isInliningToManyAllowed);
}
