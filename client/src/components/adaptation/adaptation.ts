import { DatasourceType, type Datasource } from '@/types/Datasource';
import { type Id } from '@/types/id';
import { Key, Signature, type SignatureResponse, type KeyResponse } from '@/types/identifiers';
import { JobState } from '@/types/job';
import { type Query } from '@/types/query';
import { prettyPrintDouble } from '@/types/utils/common';
import { ComparableMap } from '@/types/utils/ComparableMap';
import { v4 } from 'uuid';

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

type AdaptationObjexResponse = {
    key: KeyResponse;
    mapping: {
        datasourceId: Id;
        dataSizeInBytes: number | null;
        recordCount: number | null;
    } | null;
};

export type AdaptationObjex = {
    key: Key;
    mapping?: {
        datasource: Datasource;
        // Some of these properties might be undefined if the DB doesn't support it (or if it would be too much pain to implement).
        dataSizeInBytes: number | undefined;
        recordCount: number | undefined;
    };
};

function adaptationObjexFromResponse(input: AdaptationObjexResponse, datasources: Datasource[]): AdaptationObjex {
    const inputMapping = input.mapping;
    const mapping = inputMapping ? {
        datasource: datasources.find(d => d.id === inputMapping.datasourceId)!,
        dataSizeInBytes: inputMapping.dataSizeInBytes ?? undefined,
        recordCount: inputMapping.recordCount ?? undefined,
    } : undefined;

    return {
        key: Key.fromResponse(input.key),
        mapping,
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

type AdaptationResultResponse = {
    solutions: AdaptationSolutionResponse[];
};

export type AdaptationResult = {
    solutions: AdaptationSolution[];
};

export function adaptationResultFromResponse(input: AdaptationResultResponse, datasources: Datasource[], queries: Query[]): AdaptationResult {
    return {
        solutions: input.solutions.map(solutionResponse => adaptationSolutionFromResponse(solutionResponse, datasources, queries)),
    };
}

type AdaptationSolutionResponse = {
    id: number;
    /** Relative speed-up (more is better). 0 means no speed-up, 1 means "two times as fast". */
    speedup: number;
    /** In DB hits (less is better). */
    price: number;
    objexes: AdaptationObjexResponse[];
    queries: AdaptationQueryResponse[];
};

// Some properties of the solution aren't technically needed. E.g., prices and speed-ups can be measured by just running the queries with the new configuration.
// However, they might be expensive to compute. Or, these might be just estimates (and our backend can't do that). So, it makes sense to include them in the solution.
export type AdaptationSolution = {
    id: number;
    speedup: number;
    price: number;
    objexes: ComparableMap<Key, number, AdaptationObjex>;
    queries: Map<Id, AdaptationQuery>;
};

function adaptationSolutionFromResponse(input: AdaptationSolutionResponse, datasources: Datasource[], queries: Query[]): AdaptationSolution {
    const objexes = new ComparableMap<Key, number, AdaptationObjex>(key => key.value);
    for (const objexResponse of input.objexes) {
        const objex = adaptationObjexFromResponse(objexResponse, datasources);
        objexes.set(objex.key, objex);
    }

    const adaptationQueries = new Map<Id, AdaptationQuery>();
    for (const query of input.queries) {
        const inputQuery = queries.find(q => q.id === query.id);
        if (inputQuery)
            adaptationQueries.set(query.id, { query: inputQuery, speedup: query.speedup });
    }

    return {
        id: input.id,
        speedup: input.speedup,
        price: input.price,
        objexes,
        queries: adaptationQueries,
    } satisfies AdaptationSolution;
}

export type AdaptationQueryResponse = {
    id: Id;
    speedup: number;
};

export type AdaptationQuery = {
    query: Query;
    speedup: number;
};

// TODO This is just temporary structure for adaptation results. Refactor later.

/** @deprecated */
export function mockAdaptationResultResponse(adaptation: Adaptation, datasources: Datasource[], queries: Query[]): AdaptationResultResponse {
    const { postgres, mongo } = getBasicDatasources(adaptation, datasources);

    let prevJob: MockAdaptationJob | undefined;
    while (lastBestSolutions.length < 3)
        prevJob = mockAdaptationJob(queries, prevJob, 2000);

    let i = 0;

    return {
        solutions: [
            mockAdaptationSolutionResponse(adaptation, {
                [postgres.id]: [ 30, 40, 50 ],
                [mongo.id]: [ 70, 80 ],
            }, queries, lastBestSolutions[i++]),
            mockAdaptationSolutionResponse(adaptation, {
                [postgres.id]: [ 70, 80 ],
                [mongo.id]: [ 30, 40, 50 ],
            }, queries, lastBestSolutions[i++]),
            mockAdaptationSolutionResponse(adaptation, {
                [postgres.id]: [ 30, 40, 50, 70, 80 ],
            }, queries, lastBestSolutions[i++]),
        ]
            .map((solution, index) => ({ ...solution, id: index + 1 })),
    };
}

function getBasicDatasources(adaptation: Adaptation, datasources: Datasource[]) {
    const usedIds = new Set<Id>();
    adaptation.settings.objexes.values().forEach(objex => {
        if (objex.mapping)
            usedIds.add(objex.mapping.datasource.id);
    });

    // We want to hit all basic types if possible. And ideally those created specifically for this adaptation. Not ideal tho.
    const searched = [
        ...datasources.filter(d => usedIds.has(d.id)),
        ...datasources.toReversed(),
    ];

    return {
        postgres: searched.find(d => d.type === DatasourceType.postgresql)!,
        mongo: searched.find(d => d.type === DatasourceType.mongodb)!,
        neo4j: searched.find(d => d.type === DatasourceType.neo4j)!,
    };
}

/** @deprecated */
function mockAdaptationSolutionResponse(adaptation: Adaptation, datasourceToObjexes: Record<Id, number[]>, queries: Query[], mockSolution: MockAdaptationJobSolution): Omit<AdaptationSolutionResponse, 'id'> {
    const objexes: AdaptationObjexResponse[] = [];

    for (const datasourceId in datasourceToObjexes) {
        for (const key of datasourceToObjexes[datasourceId]) {
            const objex = adaptation.settings.objexes.get(Key.fromResponse(key));
            objexes.push({ key, mapping: objex?.mapping ? {
                datasourceId,
                dataSizeInBytes: objex.mapping.dataSizeInBytes ?? null,
                recordCount: objex.mapping.recordCount ?? null,
            } : null });
        }
    }

    // Fill in the remaining objexes.
    adaptation.settings.objexes.forEach(objex => {
        if (objex.mapping && !objexes.some(o => o.key === objex.key.value)) {
            objexes.push({ key: objex.key.toServer(), mapping: {
                datasourceId: objex.mapping.datasource.id,
                dataSizeInBytes: objex.mapping.dataSizeInBytes ?? null,
                recordCount: objex.mapping.recordCount ?? null,
            } });
        }
    });

    const adaptationQueries = queries.map(query => ({
        id: query.id,
        speedup: mockSolution.queries.get(query.id) ?? 0,
    }));

    return {
        price: mockSolution.price,
        speedup: mockSolution.speedup,
        objexes,
        queries: adaptationQueries,
    };
}

/** @deprecated */
export type MockAdaptationJob = {
    id: Id;
    state: JobState;
    createdAt: Date;
    processedStates: number;
    /** Few best solutions so far. */
    solutions: MockAdaptationJobSolution[];
};

/** @deprecated */
type MockAdaptationJobSolution = {
    // We probably don't need more (however we might in the real implementation).
    speedup: number;
    price: number;
    queries: Map<Id, number>;
};

/** @deprecated */
export function mockAdaptationJob(queries: Query[], prev: MockAdaptationJob | undefined, deltaTimeInMs?: number): MockAdaptationJob {
    if (!prev) {
        return {
            id: v4(),
            state: JobState.Running,
            createdAt: new Date(Date.now()),
            processedStates: 0,
            solutions: [],
        };
    }

    const deltaTime = deltaTimeInMs ?? Date.now() - prev.createdAt.getTime();
    const processedStates = prev.processedStates + Math.round(30 + Math.random() * 96_000 / deltaTime);

    const generatedSolutionsCount = Math.ceil(processedStates / 30);
    const solutions = [
        { speedup: 0, price: 0, queries: new Map(queries.map(q => [ q.id, 0 ])) }, // The initial solution.
        ...prev.solutions,
        ... [ ...new Array(generatedSolutionsCount) ].map(() => mockAdaptationJobSolution(queries, processedStates)),
    ]
        .sort((a, b) => {
            return prettyPrintDouble(a.speedup) === prettyPrintDouble(b.speedup)
                ? a.price - b.price
                : b.speedup - a.speedup;
        })
        .slice(0, 3);

    lastBestSolutions = solutions;

    return {
        ...prev,
        solutions,
        processedStates,
    };
}

/** @deprecated */
let lastBestSolutions: MockAdaptationJobSolution[] = [];

/** @deprecated */
function mockAdaptationJobSolution(queries: Query[], poolSize: number): MockAdaptationJobSolution {
    const bonus = Math.max(0, Math.log(poolSize)) / 10;
    // Speed-up has to be in (-1, ∞). Let's also choose a reasonable upper bound.
    let speedup = -1.4 + Math.random() * (1.9 + bonus);
    if (speedup < -1)
        speedup = 1 / speedup;

    const totalWeight = queries.reduce((ans, q) => ans + q.finalWeight, 0);

    const querySpeedups = [ ...new Array(queries.length) ].map(getRandomQuerySpeedup);
    // Now we have to make the query speedups match the overall speedup.
    // Let's say each query Q_i takes time t_i originally and T_i after adaptation. This corresponds to velocities v_i = 1 / t_i and V_i = 1 / T_i.
    // Speed-up of the query is s_i = (V_i - v_i) / v_i = V_i / v_i - 1 = t_i / T_i - 1
    // The total speed-up is S + 1 = t / T = (sum t_i * w_i) / (sum T_i * w_i) = (sum w_i * t_i) / (sum w_i * t_i / (s_i + 1))
    // So, we can't know the total speed-up without the original times t_i. And the original times are independent on the weights.
    // However, it should still be consistent accross queries. So, let's use the query average execution times.
    const times = queries.map(q => q.stats ? q.stats.evaluationTimeInMs.sum / q.stats.executionCount : 0);
    const weightedTimes = times.map((t, index) => t * queries[index].finalWeight / totalWeight);

    const currentSpeedup = weightedTimes.reduce((ans, t) => ans + t, 0) / weightedTimes.reduce((ans, t, index) => ans + t / (querySpeedups[index] + 1), 0) - 1;
    // We want this value to be equal to the previously chosen speedup.
    const adjustmentFactor = (speedup + 1) / (currentSpeedup + 1);

    for (let i = 0; i < querySpeedups.length; i++)
        querySpeedups[i] = (querySpeedups[i] + 1) * adjustmentFactor - 1;

    const queriesMap = new Map<Id, number>();
    for (let i = 0; i < queries.length; i++)
        queriesMap.set(queries[i].id, querySpeedups[i]);

    return {
        speedup,
        price: 19 + Math.random() * 69,
        queries: queriesMap,
    };
}

/** @deprecated */
function getRandomQuerySpeedup(): number {
    const speedup = -1.2 + Math.random() * 3.9;
    return speedup > -1 ? speedup : 1 / speedup;
}

