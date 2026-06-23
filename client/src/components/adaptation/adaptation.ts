import { DatasourceType, type Datasource } from '@/types/Datasource';
import { type Id } from '@/types/id';
import { Key, Signature, type SignatureResponse, type KeyResponse } from '@/types/identifiers';
import { type JobState } from '@/types/job';
import { type Query } from '@/types/query';
import { type Category } from '@/types/schema';
import { ComparableMap } from '@/types/utils/ComparableMap';

export type AdaptationResponse = {
    id: Id;
    categoryId: Id;
    systemVersion: string;
    settings: AdaptationSettingsResponse;
    runId: Id | null;
};

export class Adaptation {
    private constructor(
        readonly id: Id,
        readonly categoryId: Id,
        readonly systemVersion: string,
        readonly settings: AdaptationSettings,
        readonly runId: Id | undefined,
    ) {}

    static fromResponse(input: AdaptationResponse, datasources: Datasource[]): Adaptation {
        return new Adaptation(
            input.id,
            input.categoryId,
            input.systemVersion,
            adaptationSettingsFromResponse(input.settings, datasources),
            input.runId ?? undefined,
        );
    }
}

type AdaptationSettingsResponse = {
    explorationWeight: number;
    objexes: AdaptationObjexResponse[];
    morphisms: AdaptationMorphismResponse[];
    datasourceIds: Id[];
};

export type AdaptationSettings = {
    explorationWeight: number;
    objexes: ComparableMap<Key, number, AdaptationObjex>;
    morphisms: ComparableMap<Signature, string, AdaptationMorphism>;
    datasources: Datasource[];
};

function adaptationSettingsFromResponse(input: AdaptationSettingsResponse, datasources: Datasource[]): AdaptationSettings {
    const objexes = new ComparableMap<Key, number, AdaptationObjex>(key => key.value);
    for (const objexResponse of input.objexes) {
        const objex = adaptationObjexFromResponse(objexResponse, datasources);
        objexes.set(objex.key, objex);
    }

    const morphisms = new ComparableMap<Signature, string, AdaptationMorphism>(signature => signature.value);
    for (const morphismResponse of input.morphisms) {
        const { signature: signatureResponse, ...rest } = morphismResponse;
        const signature = Signature.fromResponse(signatureResponse);
        morphisms.set(signature, { signature, ...rest });
    }

    return {
        explorationWeight: input.explorationWeight,
        objexes,
        morphisms,
        datasources: datasources.filter(d => input.datasourceIds.includes(d.id)),
    };
}

export type DatasourceId = 'Postgres' | 'Mongo' | 'Neo4j';

const datasourceIdToType = {
    'Postgres': DatasourceType.postgresql,
    'Mongo': DatasourceType.mongodb,
    'Neo4j': DatasourceType.neo4j,
};

const dataSizeInBytes = {
    [DatasourceType.postgresql]: [] as number[],
    [DatasourceType.mongodb]: [] as number[],
    [DatasourceType.neo4j]: [] as number[],
} as Record<DatasourceType, number[]>;

const recordCount = {
    [DatasourceType.postgresql]: [] as number[],
    [DatasourceType.mongodb]: [] as number[],
    [DatasourceType.neo4j]: [] as number[],
} as Record<DatasourceType, number[]>;

type AdaptationObjexResponse = {
    key: KeyResponse;
    /** The first one is from BE, the second one from the script. */
    mappings: (ObjexMapping | DatasourceId)[];
};

type ObjexMapping = {
    datasourceId: Id;
    dataSizeInBytes: number | null;
    recordCount: number | null;
};

export type AdaptationObjex = {
    key: Key;
    mappings: {
        datasource: Datasource;
        // Some of these properties might be undefined if the DB doesn't support it (or if it would be too much pain to implement).
        dataSizeInBytes: number | undefined;
        recordCount: number | undefined;
    }[];
};

function adaptationObjexFromResponse(input: AdaptationObjexResponse, datasources: Datasource[]): AdaptationObjex {
    const mappings = input.mappings.map(mapping => {
        const datasource = typeof mapping === 'string'
            ? datasources.find(d => d.type === datasourceIdToType[mapping])!
            : datasources.find(d => d.id === mapping.datasourceId)!;

        return {
            datasource,
            dataSizeInBytes: dataSizeInBytes[datasource.type][input.key] ?? undefined,
            recordCount: recordCount[datasource.type][input.key] ?? undefined,
        };
    });

    return {
        key: Key.fromResponse(input.key),
        mappings,
    };
}

type AdaptationMorphismResponse = {
    signature: SignatureResponse;
    isReferenceAllowed: boolean;
    isEmbeddingAllowed: boolean;
};

export type AdaptationMorphism = {
    signature: Signature;
    isReferenceAllowed: boolean;
    isEmbeddingAllowed: boolean;
};

export type AdaptationJobResponse = {
    initialSolution: AdaptationSolutionRawResponse;
    lastResult: AdaptationResultResponse;
    createdAt: string;
    state: JobState;
};

export type AdaptationJob = {
    // id: Id;
    state: JobState;
    createdAt: Date;
    states: number;
    iterations: number;
    /** Few best solutions so far. */
    solutions: AdaptationSolution[];
    initialCost: number;
};

export function adaptationJobFromResponse(input: AdaptationJobResponse, category: Category, datasources: Datasource[], queries: Query[]): AdaptationJob {
    const initialSolution = parseAdaptationSolution(input.initialSolution);
    const result = adaptationResultFromResponse(input.lastResult, initialSolution, category, datasources, queries);

    return {
        state: input.state,
        createdAt: new Date(input.createdAt),
        states: result.states,
        iterations: result.iteration,
        solutions: result.solutions,
        initialCost: initialSolution.cost,
    };
}

type AdaptationResultResponse = {
    iteration: number;
    states: number;
    solutions: AdaptationSolutionRawResponse[];
};

export type AdaptationResult = {
    iteration: number;
    states: number;
    solutions: AdaptationSolution[];
};

function adaptationResultFromResponse(input: AdaptationResultResponse, initial: AdaptationSolutionResponse, category: Category, datasources: Datasource[], queries: Query[]): AdaptationResult {
    return {
        iteration: input.iteration,
        states: input.states,
        solutions: input.solutions.map(raw => {
            const solutionResponse = parseAdaptationSolution(raw);
            return adaptationSolutionFromResponse(solutionResponse, initial, category, datasources, queries);
        }),
    };
}

type AdaptationSolutionRawResponse = {
    id: number;
    cost: number;
    objexes: AdaptationMapping;
};

type AdaptationSolutionResponse = {
    id: number;
    /** Relative speed-up (more is better). 0 means no speed-up, 1 means "two times as fast". */
    // speedup: number;
    /** In DB hits (less is better). */
    cost: number;
    objexes: AdaptationObjexResponse[];
    queries: AdaptationQueryResponse[];
};

// Some properties of the solution aren't technically needed. E.g., costs and speed-ups can be measured by just running the queries with the new configuration.
// However, they might be expensive to compute. Or, these might be just estimates (and our backend can't do that). So, it makes sense to include them in the solution.
export type AdaptationSolution = {
    id: number;
    speedup: number;
    cost: number;
    objexes: ComparableMap<Key, number, AdaptationObjex>;
    queries: Map<Id, AdaptationQuery>;
};

function adaptationSolutionFromResponse(input: AdaptationSolutionResponse, initial: AdaptationSolutionResponse, category: Category, datasources: Datasource[], queries: Query[]): AdaptationSolution {
    const objexes = new ComparableMap<Key, number, AdaptationObjex>(key => key.value);
    for (const objexResponse of input.objexes) {
        const objex = adaptationObjexFromResponse(objexResponse, datasources);
        objexes.set(objex.key, objex);
    }

    const postgres = datasources.find(d => d.type === DatasourceType.postgresql)!;

    for (const objex of category.getObjexes().filter(o => o.isEntity)) {
        if (!objexes.has(objex.key)) {
            const newObjex: AdaptationObjex = {
                key: objex.key,
                mappings: [ {
                    datasource: postgres,
                    dataSizeInBytes: undefined,
                    recordCount: undefined,
                } ],
            };
            objexes.set(objex.key, newObjex);
        }
    }

    const adaptationQueries = new Map<Id, AdaptationQuery>();
    // FIXME These are empty
    for (const query of input.queries) {
        const inputQuery = queries.find(q => q.id === query.id);
        if (inputQuery) {
            const initialQuery = initial.queries.find(q => q.id === query.id)!;
            const speedup = (initialQuery.cost - query.cost) / initialQuery.cost;
            adaptationQueries.set(query.id, { query: inputQuery, speedup });
        }
    }

    // const totalWeight = queries.reduce((ans, q) => ans + q.finalWeight, 0);
    // const totalSpeedup = adaptationQueries.values().reduce((ans, q) => ans + q.query.finalWeight * q.speedup, 0) / totalWeight;
    const speedup = initial.cost > 0 ? (initial.cost - input.cost) / initial.cost : 0;

    return {
        id: input.id,
        speedup,
        cost: input.cost,
        objexes,
        queries: adaptationQueries,
    } satisfies AdaptationSolution;
}

export type AdaptationQueryResponse = {
    id: Id;
    // speedup: number;
    cost: number;
};

export type AdaptationQuery = {
    query: Query;
    speedup: number;
};

function parseAdaptationSolution(raw: AdaptationSolutionRawResponse): AdaptationSolutionResponse {
    return {
        id: raw.id,
        cost: raw.cost,
        objexes: parseAdaptationObjexes(raw.objexes),
        queries: [],
    };
}

type AdaptationMapping = Record<DatasourceId, string[]>;

function parseAdaptationObjexes(mapping: AdaptationMapping): AdaptationObjexResponse[] {
    const objexes = new Map<number, DatasourceId[]>();

    for (const datasourceId in mapping) {
        const kinds = mapping[datasourceId as DatasourceId];
        const kindsToKeys = DATASOURCES_TO_KINDS_TO_KEYS[datasourceId as DatasourceId];

        for (const kind of kinds) {
            const keys = kindsToKeys[kind];

            for (const key of keys) {
                let objex = objexes.get(key);
                if (!objex) {
                    objex = [];
                    objexes.set(key, objex);
                }

                objex.push(datasourceId as DatasourceId);
            }
        }
    }

    const result: AdaptationObjexResponse[] = [];
    objexes.forEach((datasourceIds, key) => {
        result.push({
            key,
            mappings: [ ...new Set<DatasourceId>(datasourceIds).values() ],
        });
    });

    return result;
}

const DATASOURCES_TO_KINDS_TO_KEYS: Record<DatasourceId, Record<string, number[]>> = {
    'Postgres': {
        'person': [ 1 ],
        'customer': [ 2 ],
        'seller': [ 3 ],
        'product': [ 4 ],
        'order': [ 5 ],
        'order_item': [ 6 ],
        'review': [ 7 ],
        'category': [ 8 ],
        'has_category': [ 9 ],
        'has_interest': [ 10 ],
        'follows': [ 11 ],
    },
    'Mongo': {
        'person': [ 1, 10, 8, 11 ],
        'product': [ 4, 3, 9, 8 ],
        'order': [ 5, 2, 6, 4 ],
        'customer': [ 2 ],
        'seller': [ 3 ],
        'category': [ 8 ],
        'review': [ 7 ],
    },
    'Neo4j': {
        'Person': [ 1 ],
        'Customer': [ 2 ],
        'Seller': [ 3 ],
        'Category': [ 8 ],
        'Product': [ 4 ],
        'Order': [ 5 ],
        'HAS_ITEM': [ 6 ],
        'REVIEWED': [ 7 ],
        'HAS_CATEGORY': [ 9 ],
        'HAS_INTEREST': [ 10 ],
        'FOLLOWS': [ 11 ],
        // Just to be sure.
        'SNAPSHOT_OF': [],
        'OFFERS': [],
        'PLACED': [],
    },
};
