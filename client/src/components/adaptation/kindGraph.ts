import { type Datasource } from '@/types/Datasource';
import { EdgeMap, type Edge, type Node } from '../graph/graphUtils';
import { type AdaptationKind } from './adaptation';
import { type Objex, type Category, type Morphism } from '@/types/schema';
import { ComparableMap } from '@/types/utils/ComparableMap';
import { Key, type Signature } from '@/types/identifiers';
import { getEdgeId, getNodeId } from '../category/graph/categoryGraph';

export type KindGraph = {
    nodes: Map<string, KindNode>;
    edges: EdgeMap<KindEdge>;
};

export type KindNode = Node & {
    objex: Objex;
    properties: ComparableMap<Signature, string, Objex>;
    kind: Datasource | undefined;
    adaptation: Datasource | undefined;
};

export type KindEdge = Edge;

export function categoryToKindGraph(category: Category, kinds: AdaptationKind[]): KindGraph {
    const nodes = new Map<string, KindNode>();

    category.getObjexes().forEach(objex => {
        const node = createNode(category, objex, kinds);
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

function createNode(category: Category, objex: Objex, kinds: AdaptationKind[]): KindNode | undefined {
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
    const kind = kinds.find(k => objex.key.equals(Key.fromResponse(k.key)));

    return {
        id,
        objex,
        ...objex.metadata.position,
        properties,
        kind: kind?.kind,
        adaptation: kind?.adaptation,
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
