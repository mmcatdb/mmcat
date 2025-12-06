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
};

export type AdaptationSettings = {
    explorationWeight: number;
    objexes: ComparableMap<Key, number, AdaptationObjex>;
    morphisms: ComparableMap<Signature, string, AdaptationMorphism>;
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
    };
}

type AdaptationObjexResponse = {
    key: KeyResponse;
    datasourceId: Id | null;
};

export type AdaptationObjex = {
    key: Key;
    datasource: Datasource | undefined;
};

function adaptationObjexFromResponse(input: AdaptationObjexResponse, datasources: Datasource[]): AdaptationObjex {
    return {
        key: Key.fromResponse(input.key),
        datasource: input.datasourceId ? datasources.find(d => d.id === input.datasourceId) : undefined,
    };
}

type AdaptationMorphismResponse = {
    signature: SignatureResponse;
    isReferenceAllowed: boolean;
    isEmbeddingAllowed: boolean;
    isInliningAllowed: boolean;
};

export type AdaptationMorphism = {
    signature: Signature;
    isReferenceAllowed: boolean;
    isEmbeddingAllowed: boolean;
    isInliningAllowed: boolean;
};

type AdaptationResultResponse = {
    solutions: AdaptationSolutionResponse[];
};

export type AdaptationResult = {
    solutions: AdaptationSolution[];
};

export function adaptationResultFromResponse(input: AdaptationResultResponse, datasources: Datasource[], queries: Query[]): AdaptationResult {
    return {
        solutions: input.solutions.map(solutionResponse => {
            const objexes = new ComparableMap<Key, number, AdaptationObjex>(key => key.value);
            for (const objexResponse of solutionResponse.objexes) {
                const objex = adaptationObjexFromResponse(objexResponse, datasources);
                objexes.set(objex.key, objex);
            }

            const adaptationQueries = new Map<Id, AdaptationQuery>();
            for (const query of solutionResponse.queries) {
                const inputQuery = queries.find(q => q.id === query.id);
                if (inputQuery)
                    adaptationQueries.set(query.id, { query: inputQuery, speedup: query.speedup });
            }

            return {
                id: solutionResponse.id,
                speedup: solutionResponse.speedup,
                price: solutionResponse.price,
                objexes,
                queries: adaptationQueries,
            } satisfies AdaptationSolution;
        }),
    };
}

type AdaptationSolutionResponse = {
    id: number;
    /** Relative speedup (more is better). */
    speedup: number;
    /** In DB hits (less is better). */
    price: number;
    objexes: AdaptationObjexResponse[];
    queries: AdaptationQueryResponse[];
};

// Some properties of the solution aren't technically needed. E.g., prices and speedups can be measured by just running the queries with the new configuration.
// However, they might be expensive to compute. Or, these might be just estimates (and our backend can't do that). So, it makes sense to include them in the solution.
export type AdaptationSolution = {
    id: number;
    speedup: number;
    price: number;
    objexes: ComparableMap<Key, number, AdaptationObjex>;
    queries: Map<Id, AdaptationQuery>;
};

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
    const { postgres, mongo, neo4j } = getBasicDatasources(datasources);

    return {
        solutions: [
            mockAdaptationSolutionResponse(adaptation, 42.37, {
                [postgres.id]: [ 21, 1 ],
                [mongo.id]: [ 52 ],
                [neo4j.id]: [ 51 ],
            }, queries),
            mockAdaptationSolutionResponse(adaptation, 55.91, {
                [mongo.id]: [ 52, 21, 1 ],
                [neo4j.id]: [ 51 ],
            }, queries),
            mockAdaptationSolutionResponse(adaptation, 63.31, {
                [postgres.id]: [ 1, 3, 41, 51, 61, 71 ],
            }, queries),
        ]
            .sort((a, b) => b.speedup - a.speedup)
            .map((solution, index) => ({
                ...solution,
                id: index + 1,
                // Remap to last best solutions for consistency.
                speedup: lastBestSolutions[index]?.speedup ?? solution.speedup,
                price: lastBestSolutions[index]?.price ?? solution.price,
            })),
    };
}

function getBasicDatasources(datasources: Datasource[]) {
    return {
        postgres: datasources.find(d => d.type === DatasourceType.postgresql)!,
        mongo: datasources.find(d => d.type === DatasourceType.mongodb)!,
        neo4j: datasources.find(d => d.type === DatasourceType.neo4j)!,
    };
}

function mockAdaptationSolutionResponse(adaptation: Adaptation, price: number, datasourceToObjexes: Record<Id, number[]>, queries: Query[]): Omit<AdaptationSolutionResponse, 'id'> {
    const objexes: AdaptationObjexResponse[] = [];

    for (const datasourceId in datasourceToObjexes) {
        for (const key of datasourceToObjexes[datasourceId])
            objexes.push({ key, datasourceId });
    }

    // Fill in the remaining objexes.
    adaptation.settings.objexes.forEach(objex => {
        if (objex.datasource && !objexes.some(o => o.key === objex.key.value))
            objexes.push({ key: objex.key.toServer(), datasourceId: objex.datasource.id });
    });

    const adaptationQueries = queries.map(query => ({
        id: query.id,
        speedup: getRandomQuerySpeedup(),
    }));


    let totalWeight = 0;
    let absoluteSpeedup = 0;

    for (let i = 0; i < queries.length; i++) {
        totalWeight += queries[i].finalWeight;
        absoluteSpeedup += adaptationQueries[i].speedup * queries[i].finalWeight;
    }

    const speedup = absoluteSpeedup / totalWeight;

    return {
        price,
        speedup,
        objexes,
        queries: adaptationQueries,
    };
}

function getRandomQuerySpeedup(): number {
    return 0.2 + Math.random() * 3.9;
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

type MockAdaptationJobSolution = {
    // We probably don't need more (however we might in the real implementation).
    speedup: number;
    price: number;
};

/** @deprecated */
export function mockAdaptationJob(prev: MockAdaptationJob | undefined): MockAdaptationJob {
    if (!prev) {
        return {
            id: v4(),
            state: JobState.Running,
            createdAt: new Date(Date.now()),
            processedStates: 0,
            solutions: [],
        };
    }

    const deltaTime = Date.now() - prev.createdAt.getTime();
    const processedStates = prev.processedStates + Math.round(30 + Math.random() * 96_000 / deltaTime);

    const generatedSolutionsCount = Math.ceil(processedStates / 30);
    const solutions = [
        ...prev.solutions,
        ... [ ...new Array(generatedSolutionsCount) ].map(() => mockAdaptationJobSolution(processedStates)),
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

let lastBestSolutions: MockAdaptationJobSolution[] = [];

function mockAdaptationJobSolution(poolSize: number): MockAdaptationJobSolution {
    const bonus = Math.max(0, Math.log(poolSize)) / 10;
    return {
        speedup: 0.2 + Math.random() * (3.1 + bonus),
        price: 19 + Math.random() * 69,
    };
}
