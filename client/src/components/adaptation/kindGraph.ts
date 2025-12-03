import { type Datasource } from '@/types/Datasource';
import { EdgeMap, type Edge, type Node } from '../graph/graphUtils';
import { type Objex, type Category, type Morphism } from '@/types/schema';
import { ComparableMap } from '@/types/utils/ComparableMap';
import { type Signature } from '@/types/identifiers';
import { getEdgeId, getNodeId } from '../category/graph/categoryGraph';

export type KindGraph = {
    nodes: Map<string, KindNode>;
    edges: EdgeMap<KindEdge>;
};

export type KindNode = Node & {
    objex: Objex;
    properties: ComparableMap<Signature, string, Objex>;
    datasource: Datasource | undefined;
};

export type KindEdge = Edge;

type DatasourceGetter = (objex: Objex) => Datasource | undefined;

export function categoryToKindGraph(category: Category, datasourceGetter: DatasourceGetter): KindGraph {
    const nodes = new Map<string, KindNode>();

    category.getObjexes().forEach(objex => {
        const node = createNode(category, objex, datasourceGetter);
        if (node)
            nodes.set(node.id, node);
    });

    const edges: KindEdge[] = [];

    category.getMorphisms().forEach(morphism => {
        const edge = createEdge(morphism, nodes);
        if (edge)
            edges.push(edge);
    });

    return {
        nodes,
        edges: new EdgeMap(edges),
    };
}

function createNode(category: Category, objex: Objex, datasourceGetter: DatasourceGetter): KindNode | undefined {
    if (!objex.isEntity)
        return;

    const properties = new ComparableMap<Signature, string, Objex>(signature => signature.value);
    objex.morphismsFrom.values()
        .forEach(morphism => {
            const toObjex = category.getObjex(morphism.schema.codKey);
            if (!toObjex.isEntity)
                properties.set(morphism.signature, toObjex);
        });

    const id = getNodeId(objex);

    return {
        id,
        objex,
        ...objex.metadata.position,
        properties,
        datasource: datasourceGetter(objex),
    };
}

function createEdge(morphism: Morphism, nodes: Map<string, KindNode>): KindEdge | undefined {
    const from = getNodeId(morphism.schema.domKey);
    const to = getNodeId(morphism.schema.codKey);

    if (!nodes.has(from) || !nodes.has(to))
        return;

    return {
        id: getEdgeId(morphism),
        from,
        to,
        label: '', // TODO
    };
}
