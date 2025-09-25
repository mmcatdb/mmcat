import { type Signature } from '@/types/identifiers';
import { EdgeMap, type Edge, type Node } from '../graph/graphUtils';
import { type SchemaObjex, type Category, type MetadataMorphism, type MetadataObjex, type SchemaMorphism } from '@/types/schema';

/**
 * Represents a graph structure for a category, compatible with the graph library.
 * Nodes and edges are immutable to ensure safe usage in React's rendering lifecycle.
 */
export type CategoryGraph = {
    nodes: Map<string, CategoryNode>;
    edges: EdgeMap<CategoryEdge>;
};

/**
 * A node in the category graph, extending the base graph Node with schema and metadata.
 */
export type CategoryNode = Node & {
    schema: SchemaObjex;
    metadata: MetadataObjex;
};

/**
 * An edge in the category graph, extending the base graph Edge with schema and metadata.
 */
export type CategoryEdge = Edge & {
    schema: SchemaMorphism;
    metadata: MetadataMorphism;
};

/**
 * Converts a category into a graph structure suitable for rendering in React.
 * Creates immutable node and edge maps from the category's objexes and morphisms.
 *
 * @param category - The category to transform into a graph.
 * @returns A CategoryGraph containing nodes and edges.
 */
export function categoryToGraph(category: Category): CategoryGraph {
    const nodes = mapCategoryToNodes(category);
    const edges = mapCategoryToEdges(category);

    return {
        nodes: new Map(nodes.map(node => [ node.id, node ])),
        edges: new EdgeMap(edges),
    };
}

/**
 * Maps the objex components of a category into graph nodes.
 */
function mapCategoryToNodes(category: Category): CategoryNode[] {
    return category.getObjexes().map(objex => {
        const { schema, metadata } = objex;
        return {
            id: schema.key.toString(),
            ...metadata.position,
            schema,
            metadata,
        } satisfies CategoryNode;
    });
}

/** Maps the morphism components of a category into graph edges. */
function mapCategoryToEdges(category: Category): CategoryEdge[] {
    return category.getMorphisms().map(morphism => {
        const { schema, metadata } = morphism;
        return {
            id: schema.signature.toString(),
            from: schema.domKey.toString(),
            to: schema.codKey.toString(),
            schema,
            metadata,
            get label() {
                return this.schema.signature + (this.metadata.label ? ` - ${this.metadata.label}` : '');
            },
        } satisfies CategoryEdge;
    });
}

export function traverseCategoryGraph(graph: CategoryGraph, from: CategoryNode, path: Signature): CategoryNode {
    let currentId = from.id;

    for (const base of path.toBases()) {
        const nonDual = base.isBaseDual ? base.dual() : base;
        const edge = graph.edges.get(nonDual.toString())!;
        currentId = edge.from === currentId ? edge.to : edge.from;
    }

    return graph.nodes.get(currentId)!;
}
