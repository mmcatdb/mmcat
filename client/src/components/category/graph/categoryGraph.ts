import { Key, Signature } from '@/types/identifiers';
import { EdgeMap, type Edge, type Node } from '../../graph/graphUtils';
import { type Morphism, type Objex, type SchemaMorphism, type SchemaObjex, type Category, type MetadataMorphism, type MetadataObjex } from '@/types/schema';

/**
 * Represents a graph structure for a category, compatible with the graph library.
 * Nodes and edges are immutable to ensure safe usage in React's rendering lifecycle.
 */
export type CategoryGraph = {
    nodes: Map<string, CategoryNode>;
    edges: EdgeMap<CategoryEdge>;
};

export type CategoryNode = Node & {
    schema: SchemaObjex;
    metadata: MetadataObjex;
};

export type CategoryEdge = Edge & {
    schema: SchemaMorphism;
    metadata: MetadataMorphism;
};

/**
 * Converts a category into a graph structure suitable for rendering in React.
 * Creates immutable node and edge maps from the category's objexes and morphisms.
 */
export function categoryToGraph(category: Category): CategoryGraph {
    const nodes = mapCategoryToNodes(category);
    const edges = mapCategoryToEdges(category);

    return {
        nodes: new Map(nodes.map(node => [ node.id, node ])),
        edges: new EdgeMap(edges),
    };
}

/** Maps the objex components of a category into graph nodes. */
function mapCategoryToNodes(category: Category): CategoryNode[] {
    return category.getObjexes().map(objex => {
        const { schema, metadata } = objex;
        return {
            id: getNodeId(schema),
            ...metadata.position,
            schema,
            metadata,
        } satisfies CategoryNode;
    });
}

export function getNodeId(objexOrKey: Objex | SchemaObjex | Key): string {
    const key = objexOrKey instanceof Key ? objexOrKey : objexOrKey.key;
    return key.toString();
}

export function getNodeKey(nodeId: string): Key {
    return Key.fromNumber(Number(nodeId));
}

/** Maps the morphism components of a category into graph edges. */
function mapCategoryToEdges(category: Category): CategoryEdge[] {
    return category.getMorphisms().map(morphism => {
        const { schema, metadata } = morphism;
        return {
            id: getEdgeId(schema),
            from: getNodeId(schema.domKey),
            to: getNodeId(schema.codKey),
            schema,
            metadata,
            get label() {
                return this.schema.signature + (this.metadata.label ? ` - ${this.metadata.label}` : '');
            },
        } satisfies CategoryEdge;
    });
}

export function getEdgeId(morphismOrSignature: Morphism | SchemaMorphism | Signature): string {
    if (!(morphismOrSignature instanceof Signature)) 
        return morphismOrSignature.signature.toString();
    
    if (!morphismOrSignature.isBase || morphismOrSignature.isBaseDual)
        throw new Error('Edge ID can only be obtained from base non-dual signatures.');

    return morphismOrSignature.toString();
}

export function getEdgeSignature(edgeId: string): Signature {
    return Signature.base(Number(edgeId));
}

/**
 * Goes from the `from` node along the given `path`.
 * @returns The destination node.
 */
export function traverseCategoryGraph(graph: CategoryGraph, from: CategoryNode, path: Signature): CategoryNode {
    let currentId = from.id;

    for (const base of path.toBases()) {
        const nonDual = base.isBaseDual ? base.dual() : base;
        const edge = graph.edges.get(nonDual.toString())!;
        currentId = edge.from === currentId ? edge.to : edge.from;
    }

    return graph.nodes.get(currentId)!;
}
