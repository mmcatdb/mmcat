import { DatasourceType, type Datasource } from '@/types/Datasource';
import { type Id } from '@/types/id';
import { type KeyResponse } from '@/types/identifiers';

// TODO This is just temporary structure for adaptation results. Refactor later.

export type AdaptationKind = {
    key: KeyResponse;
    datasource: Datasource;
    adaptation: {
        kind: Datasource;
        improvement: number;
    } | undefined;
};

export type AdaptationInput = {
    datasources: Datasource[];
    kinds: AdaptationKind[];
};

export function mockAdaptationInput(datasources: Datasource[]): AdaptationInput {
    const { prevKinds } = getPrevKinds(datasources);

    const kinds: AdaptationKind[] = [];

    for (const stringKey in prevKinds) {
        const key = Number(stringKey);
        const datasource = datasources.find(d => d.id === prevKinds[key])!;
        kinds.push({
            key,
            datasource,
            adaptation: undefined,
        });
    }

    return {
        datasources,
        kinds,
    };
}

function getPrevKinds(datasources: Datasource[]) {
    const postgres = datasources.find(d => d.type === DatasourceType.postgresql)!;
    const mongo = datasources.find(d => d.type === DatasourceType.mongodb)!;
    const neo4j = datasources.find(d => d.type === DatasourceType.neo4j)!;

    const prevKinds: Record<number, Id> = {
        1: postgres.id,
        3: postgres.id,
        21: neo4j.id,
        23: neo4j.id,
        41: postgres.id,
        51: postgres.id,
        61: postgres.id,
        52: mongo.id,
        71: mongo.id,
        73: mongo.id,
    };

    return { postgres, mongo, neo4j, prevKinds };
}

export type AdaptationResult = {
    datasources: Datasource[];
    kinds: AdaptationKind[];
    price: number;
};

export function mockAdaptationResults(datasources: Datasource[]): AdaptationResult[] {
    const { postgres, mongo, neo4j, prevKinds } = getPrevKinds(datasources);

    return [
        mockAdaptationResult(42.37, datasources, prevKinds, {
            [postgres.id]: [ 21, 1 ],
            [mongo.id]: [ 52 ],
            [neo4j.id]: [ 51 ],
        }),
        mockAdaptationResult(55.91, datasources, prevKinds, {
            [mongo.id]: [ 52, 21, 1 ],
            [neo4j.id]: [ 51 ],
        }),
    ];
}

function mockAdaptationResult(price: number, datasources: Datasource[], prevKinds: Record<number, Id>, nextKinds: Record<Id, number[]>): AdaptationResult {
    const kinds: AdaptationKind[] = [];

    for (const datasourceId in nextKinds) {
        const datasource = datasources.find(d => d.id === datasourceId)!;
        const kindKeys = nextKinds[datasourceId];

        for (const key of kindKeys) {
            const prevDatasource = datasources.find(d => d.id === prevKinds[key])!;
            kinds.push({
                key,
                datasource,
                adaptation: prevDatasource ? {
                    kind: prevDatasource,
                    improvement: Math.random(),
                } : undefined,
            });
        }
    }

    return {
        datasources,
        kinds: kinds.toSorted((a, b) => a.key - b.key),
        price,
    };
}
