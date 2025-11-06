import { type Id } from '@/types/id';
import { type DatasourceType } from '@/types/Datasource';
import { EdgeMap, type Position, type Edge, type Node } from '../graph/graphUtils';

// FIXME Move this. And use proper types.
export type AdaptationResult = {
    datasources: ResultDatasource[];
    kinds: ResultKind[];
    price: number;
    positions: Map<Id, Position>;
};

export type ResultDatasource = {
    id: Id;
    type: DatasourceType;
    label: string;
};

export type ResultKind = {
    id: Id;
    datasource: ResultDatasource;
    label: string;
    toRelationships: Id[];
    improvement: number;
};

export type KindGraph = {
    nodes: Map<string, KindNode>;
    edges: EdgeMap<KindEdge>;
};

export type KindNode = Node & {
    label: string;
    datasource: {
        type: DatasourceType;
        label: string;
    };
};

export type KindEdge = Edge;

export function adaptationResultToGraph(result: AdaptationResult): KindGraph {
    const nodes = mapCategoryToNodes(result);
    const edges = mapCategoryToEdges(result);

    return {
        nodes: new Map(nodes.map(node => [ node.id, node ])),
        edges: new EdgeMap(edges),
    };
}

function mapCategoryToNodes(result: AdaptationResult): KindNode[] {
    return result.kinds.map(kind => {
        const { id, datasource, label } = kind;
        return {
            id,
            ...result.positions.get(id)!,
            label,
            datasource,
        } satisfies KindNode;
    });
}

function mapCategoryToEdges(result: AdaptationResult): KindEdge[] {
    return result.kinds.flatMap(kind => kind.toRelationships.map(toKind => ({
        id: `${kind.id}-to-${toKind}`,
        from: kind.id,
        to: toKind,
        label: '',
    })));
}
