import type { Signature } from '@/types/identifiers';
import { Cardinality, type Objex, type Max, type Min, type Morphism, type Category } from '@/types/schema';
import { type DatasourceSpecs } from '@/types/Datasource';
import { getEdgeId, getNodeId, getNodeKey } from '@/components/category/graph/categoryGraph';

/**
 * Nodes and edges correspond to the objexes and morphisms from the category.
 * Contains information about paths from a selected objex.
 */
export type PathGraph = {
    sourceNodeId: string;
    nodes: Map<string, PathNode>;
    edges: Map<string, PathEdge>;
};

type PathNode = {
    id: string;
    pathCount: PathCount;
    /** If this node will ever be found ambiguous, these ones will also be marked as such. */
    dependentNodes: PathNode[];
    /** If defined, there is a path from the source objex to this one. It might be ambiguous (in that case, only one of them is listed here). */
    pathSegmentTo?: PathSegment;
};

/** How many paths from the source there are to this one. The default options is None. */
export enum PathCount {
    None = 'none',
    One = 'one',
    Many = 'many',
}

function createPathNode(objex: Objex): PathNode {
    return {
        id: getNodeId(objex),
        pathCount: PathCount.None,
        dependentNodes: [],
    };
}

type CategoryPath = {
    /** The full signature from the source node to here. */
    signature: Signature;
    min: Min;
    max: Max;
};

function extendPath(path: CategoryPath, second: CategoryPath): CategoryPath {
    return {
        signature: path.signature.concatenate(second.signature),
        min: path.min === Cardinality.One && second.min === Cardinality.One ? Cardinality.One : Cardinality.Zero,
        max: path.max === Cardinality.One && second.max === Cardinality.One ? Cardinality.One : Cardinality.Star,
    };
}

type PathEdge = {
    id: string;
    from: string;
    to: string;
    /**
     * If defined, this is a direction of at least one traversable path that uses this edge. However, there might be multiple paths.
     * The edge is traversable if there is only one such path. I.e., one of its nodes is {@link PathCount.One} and the other is not {@link PathCount.None}.
     */
    traversableDirection?: boolean;
};

function createPathEdge(morphism: Morphism): PathEdge {
    return {
        id: getEdgeId(morphism),
        from: getNodeId(morphism.schema.domKey),
        to: getNodeId(morphism.schema.codKey),
    };
}

/** Corresponds to a specific edge in the graph. There might be multiple segments for the same edge. */
type PathSegment = {
    edge: PathEdge;
    from: PathNode;
    to: PathNode;
    prevSegment: PathSegment | undefined;
    fullPath: CategoryPath;

    markTraversable(): void;
};

export type PathGraphProvider = {
    computePathGraph(sourceNodeId: string): PathGraph;
};

export class DefaultPathGraphProvider implements PathGraphProvider {
    constructor(
        private readonly category: Category,
    ) {}

    computePathGraph(sourceNodeId: string): PathGraph {
        const sourceObjex = this.category.getObjex(getNodeKey(sourceNodeId));
        return computePathsFromObjex(sourceObjex);
    }
}

export function computePathsFromObjex(source: Objex, filterFunction?: FilterFunction): PathGraph {
    const marker = new PathMarker(source, filterFunction);

    marker.markPathsFromSourceObjex();

    return {
        sourceNodeId: getNodeId(source),
        nodes: marker.nodes,
        edges: marker.edges,
    };
}

class PathMarker {
    // It's actually important this is a stack and not a queue, because the paths has to be traversed in one go.
    private readonly stack: PathSegment[] = [];
    private readonly sourceNode: PathNode;

    constructor(
        private readonly sourceObjex: Objex,
        private readonly filterFunction?: FilterFunction,
    ) {
        this.sourceNode = this.getNode(sourceObjex);
    }

    readonly nodes = new Map<string, PathNode>();
    readonly objexes = new Map<string, Objex>();

    private getNode(objex: Objex): PathNode {
        const id = getNodeId(objex);

        let node = this.nodes.get(id);
        if (!node) {
            node = createPathNode(objex);
            this.nodes.set(id, node);
            this.objexes.set(id, objex);
        }

        return node;
    }

    private getObjex(node: PathNode): Objex {
        return this.objexes.get(node.id)!;
    }

    readonly edges = new Map<string, PathEdge>();
    readonly morphisms = new Map<string, Morphism>();

    private getEdge(morphism: Morphism): PathEdge {
        const id = getEdgeId(morphism);

        let edge = this.edges.get(id);
        if (!edge) {
            edge = createPathEdge(morphism);
            this.edges.set(id, edge);
            this.morphisms.set(id, morphism);
        }

        return edge;
    }

    private getMorphism(edge: PathEdge): Morphism {
        return this.morphisms.get(edge.id)!;
    }

    private createPathSegment(from: Objex, morphism: Morphism, prevSegment?: PathSegment): PathSegment {
        const isSameDirection = morphism.dom.equals(from);

        const segmentPath: CategoryPath = {
            signature: morphism.signature,
            min: isSameDirection ? morphism.schema.min : Cardinality.Zero,
            max: isSameDirection ? Cardinality.One : Cardinality.Star,
        };

        const to = isSameDirection ? morphism.cod : morphism.dom;

        return {
            from: this.getNode(from),
            to: this.getNode(to),
            edge: this.getEdge(morphism),

            markTraversable() {
                this.edge.traversableDirection = isSameDirection;
            },

            fullPath: prevSegment ? extendPath(prevSegment.fullPath, segmentPath) : segmentPath,
            prevSegment,
        };
    }

    markPathsFromSourceObjex(): void {
        this.sourceNode.pathCount = PathCount.One;

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

        const toObjex = this.getObjex(neighbor.to);
        const addition = this.getTraversableNeighbors(toObjex, neighbor);
        this.stack.push(...addition);
    }

    private getTraversableNeighbors(from: Objex, prevSegment?: PathSegment): PathSegment[] {
        let neighbors = from.neighborMorphisms
            .map(morphism => this.createPathSegment(from, morphism, prevSegment))
            // No need to go back to the source.
            .filter(neighbor => neighbor.to.id !== this.sourceNode.id);

        if (prevSegment)
            // We march to victory, or we march to defeat. But we go forward, only forward.
            neighbors = neighbors.filter(neighbor => neighbor.to.id !== prevSegment.from.id);

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

        const fromNode = neighbor.from;
        const toNode = neighbor.to;

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
        const contactNode = newSegment.to;

        // The first nodes after the common node on the first and second paths.
        let firstPathStart: PathNode | undefined;
        let secondPathStart: PathNode | undefined;

        // We are going back to the source. We collect all nodes except the source node.
        const visitedNodes = new Set<string>();
        let current = contactNode;
        while (current.pathSegmentTo) {
            visitedNodes.add(current.id);
            current = current.pathSegmentTo.from;
        }

        // The source node is the common node until proven otherwise.
        let commonNode: PathNode = this.sourceNode;

        // We go back along the other path and we look for the first common node. We also need to find the first node after the common one.
        current = newSegment.from;
        while (current.pathSegmentTo) {
            if (visitedNodes.has(current.id)) {
                commonNode = current;
                break;
            }

            secondPathStart = current;
            current = current.pathSegmentTo.from;
        }

        current = contactNode;
        while (current !== commonNode) {
            firstPathStart = current;
            current = current.pathSegmentTo!.from;
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

export function createDefaultFilter(specs: DatasourceSpecs): FilterFunction {
    return (segment: PathSegment) => segment.prevSegment
        ? (segment.fullPath.max === Cardinality.One ? specs.isPropertyToOneAllowed : specs.isPropertyToManyAllowed)
        : (segment.fullPath.max === Cardinality.One ? specs.isInliningToOneAllowed : specs.isInliningToManyAllowed);
}

export function computePathToNode(pathNode: PathNode): { nodeIds: string[], edgeIds: string[] } {
    const nodeIdsReversed: string[] = [];
    const edgeIdsReversed: string[] = [];

    let current = pathNode;

    while (current.pathSegmentTo) {
        nodeIdsReversed.push(current.id);
        edgeIdsReversed.push(current.pathSegmentTo.edge.id);
        current = current.pathSegmentTo.from;
    }

    return { nodeIds: nodeIdsReversed.reverse(), edgeIds: edgeIdsReversed.reverse() };
}

export function computePathWithEdge(pathEdge: PathEdge, graph: PathGraph): { nodeIds: string[], edgeIds: string[] } {
    const fromNodeId = pathEdge.traversableDirection ? pathEdge.from : pathEdge.to;
    const fromNode = graph.nodes.get(fromNodeId)!;

    const { nodeIds, edgeIds } = computePathToNode(fromNode);

    const toNodeId = pathEdge.traversableDirection ? pathEdge.to : pathEdge.from;
    nodeIds.push(toNodeId);
    edgeIds.push(pathEdge.id);

    return { nodeIds, edgeIds };
}
