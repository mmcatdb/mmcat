import { type Edge, type Node } from './graphUtils';

/**
 * Represents a sequence of nodes selected by the user.
 * There are edges between them (because there might be multiple paths, so we want to prevent any ambiguity).
 */
export class GraphPath<N extends Node, E extends Edge> {
    private constructor(
        readonly nodes: N[],
        /** There are always n-1 edges for n nodes. */
        readonly edges: E[],
    ) {}

    /**
     * Insert nodes (with edges between) into the path.
     * No two nodes (and edges) can be next to each other. Both first and last items must be nodes.
     */
    static create<N extends Node, E extends Edge>(...items: (N | E)[]): GraphPath<N, E> {
        const nodes: N[] = [];
        const edges: E[] = [];

        let wasNode = false;

        for (const item of items) {
            const isNode = 'position' in item;

            if (isNode) {
                if (wasNode)
                    throw new Error('Nodes must be separated by edges.');

                nodes.push(item);
            }
            else {
                if (!wasNode)
                    throw new Error('Edges must be between nodes.');

                edges.push(item);
            }

            wasNode = isNode;
        }

        // Check if the last item is a node. However, empty path is allowed.
        if (!wasNode && edges.length > 0)
            throw new Error('Edges might be only between nodes.');

        return new GraphPath(nodes, edges);
    }

    /** Returns a new path with the added node and edge. */
    add(node: N, edge: E): GraphPath<N, E> {
        return new GraphPath([ ...this.nodes, node ], [ ...this.edges, edge ]);
    }

    /** Updates the path with the added node and edge. */
    addMutable(node: N, edge: E) {
        this.nodes.push(node);
        this.edges.push(edge);
    }

    clone(): GraphPath<N, E> {
        return new GraphPath([ ...this.nodes ], [ ...this.edges ]);
    }

    remove(): GraphPath<N, E> {
        return new GraphPath(this.nodes.slice(0, -1), this.edges.slice(0, -1));
    }

    get isEmpty(): boolean {
        return this.nodes.length === 0;
    }

    get firstNode(): N {
        return this.nodes[0];
    }

    get lastNode(): N {
        return this.nodes[this.nodes.length - 1];
    }
}
