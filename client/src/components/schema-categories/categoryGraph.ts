import { type Edge, type Node } from '../graph/graphUtils';
import { type Category, type MetadataMorphism, type MetadataObjex, type SchemaMorphism, type SchemaObjex } from '@/types/schema';

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
