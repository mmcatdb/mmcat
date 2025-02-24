import { type Edge, type Node } from '../graph/graphUtils';
import { SchemaObjex, type Category, type MetadataMorphism, type MetadataObjex, type SchemaMorphism } from '@/types/schema';

/**
 * A plain object that can be displayed by the graph library.
 * All its objects are immutable so they are safe to use in React.
 */
export type CategoryGraph = {
    nodes: CategoryNode[];
    edges: CategoryEdge[];
};

export type CategoryNode = Node & {
    schema: SchemaObjex;
    metadata: MetadataObjex;
};

export type CategoryEdge = Edge & {
    schema: SchemaMorphism;
    metadata: MetadataMorphism;
};

/** Transforms the category to a reactive state that can be rendered by React. */
export function categoryToGraph(category: Category): CategoryGraph {
    const nodes = category.getObjexes().map(objex => {
        const schema = objex.schema;
        const metadata = objex.metadata;

        return {
            id: '' + schema.key.value,
            position: metadata.position,
            schema,
            metadata,
        } satisfies CategoryNode;
    });

    const edges = category.getMorphisms().map(morphism => {
        const schema = morphism.schema;
        const metadata = morphism.metadata;

        return {
            id: '' + schema.signature.baseValue,
            from: '' + schema.domKey.value,
            to: '' + schema.codKey.value,
            schema,
            metadata,
        } satisfies CategoryEdge;
    });

    return { nodes, edges };
}

/**
 * Represents a sequence of nodes selected by the user.
 * There are morphisms between them (because there might be multiple paths, so we want to prevent any ambiguity).
 */
export class CategoryPath {
    private constructor(
        readonly nodes: CategoryNode[],
        /** There are always n-1 edges for n nodes. */
        readonly edges: CategoryEdge[],
    ) {}

    /**
     * Insert nodes (with edges between) into the path.
     * No two nodes (and edges) can be next to each other. Both first and last items must be nodes.
     */
    static create(...items: (CategoryNode | CategoryEdge)[]): CategoryPath {
        const nodes: CategoryNode[] = [];
        const edges: CategoryEdge[] = [];

        let wasNode = false;

        for (const item of items) {
            const isNode = item.schema instanceof SchemaObjex;

            if (isNode) {
                if (wasNode)
                    throw new Error('Nodes must be separated by edges.');

                nodes.push(item as CategoryNode);
            }
            else {
                if (!wasNode)
                    throw new Error('Edges must be between nodes.');

                edges.push(item as CategoryEdge);
            }

            wasNode = isNode;
        }

        // Check if the last item is a node. However, empty path is allowed.
        if (!wasNode && edges.length > 0)
            throw new Error('Edges might be only between nodes.');

        return new CategoryPath(nodes, edges);
    }

    /** Returns a new path with the added node and edge. */
    add(node: CategoryNode, edge: CategoryEdge): CategoryPath {
        return new CategoryPath([ ...this.nodes, node ], [ ...this.edges, edge ]);
    }

    /** Updates the path with the added node and edge. */
    addMutable(node: CategoryNode, edge: CategoryEdge) {
        this.nodes.push(node);
        this.edges.push(edge);
    }

    get isEmpty(): boolean {
        return this.nodes.length === 0;
    }

    get firstNode(): CategoryNode {
        return this.nodes[0];
    }

    get lastNode(): CategoryNode {
        return this.nodes[this.nodes.length - 1];
    }

}
