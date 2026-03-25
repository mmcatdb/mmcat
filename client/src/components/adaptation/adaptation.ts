import { DatasourceType, type Datasource } from '@/types/Datasource';
import { type Id } from '@/types/id';
import { Key, Signature, type SignatureResponse, type KeyResponse } from '@/types/identifiers';
import { type JobState } from '@/types/job';
import { type Query } from '@/types/query';
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

export type DatasourceId = 'postgres' | 'mongo' | 'neo4j';

const datasourceIdToType = {
    postgres: DatasourceType.postgresql,
    mongo: DatasourceType.mongodb,
    neo4j: DatasourceType.neo4j,
};

const dataSizeInBytes = {
    [DatasourceType.postgresql]: [ 10, 20 ],
    [DatasourceType.mongodb]: [ 20, 40 ],
    [DatasourceType.neo4j]: [ 5, 15 ],
} as Record<DatasourceType, number[]>;

const recordCount = {
    [DatasourceType.postgresql]: [ 1000, 2000 ],
    [DatasourceType.mongodb]: [ 2000, 4000 ],
    [DatasourceType.neo4j]: [ 500, 1500 ],
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

type AdaptationResultResponse = {
    processedStates: number;
    solutions: AdaptationSolutionResponse[];
};

export type AdaptationResult = {
    processedStates: number;
    solutions: AdaptationSolution[];
};

function adaptationResultFromResponse(input: AdaptationResultResponse, initial: AdaptationSolutionResponse, datasources: Datasource[], queries: Query[]): AdaptationResult {
    return {
        processedStates: input.processedStates,
        solutions: input.solutions.map(solutionResponse => adaptationSolutionFromResponse(solutionResponse, initial, datasources, queries)),
    };
}

type AdaptationSolutionResponse = {
    id: number;
    /** Relative speed-up (more is better). 0 means no speed-up, 1 means "two times as fast". */
    // speedup: number;
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

function adaptationSolutionFromResponse(input: AdaptationSolutionResponse, initial: AdaptationSolutionResponse, datasources: Datasource[], queries: Query[]): AdaptationSolution {
    const objexes = new ComparableMap<Key, number, AdaptationObjex>(key => key.value);
    for (const objexResponse of input.objexes) {
        const objex = adaptationObjexFromResponse(objexResponse, datasources);
        objexes.set(objex.key, objex);
    }

    const adaptationQueries = new Map<Id, AdaptationQuery>();
    for (const query of input.queries) {
        const inputQuery = queries.find(q => q.id === query.id);
        if (inputQuery) {
            const initialQuery = initial.queries.find(q => q.id === query.id)!;
            const speedup = (initialQuery.cost - query.cost) / initialQuery.cost;
            adaptationQueries.set(query.id, { query: inputQuery, speedup });
        }
    }

    const totalWeight = queries.reduce((ans, q) => ans + q.finalWeight, 0);
    const totalSpeedup = adaptationQueries.values().reduce((ans, q) => ans + q.query.finalWeight * q.speedup, 0) / totalWeight;

    return {
        id: input.id,
        speedup: totalSpeedup,
        price: input.price,
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

export type AdaptationJobResponse = {
    initialJson: string;
    lastJson: string | null;
    createdAt: string;
    state: JobState;
};

export type AdaptationJob = {
    // id: Id;
    state: JobState;
    createdAt: Date;
    processedStates: number;
    /** Few best solutions so far. */
    solutions: AdaptationSolution[];
};

export function adaptationJobFromResponse(input: AdaptationJobResponse, datasources: Datasource[], queries: Query[]): AdaptationJob {
    const initialParsed = JSON.parse(input.initialJson) as AdaptationResultResponse;
    const initial = initialParsed.solutions[0];

    const lastParsed = input.lastJson ? JSON.parse(input.lastJson) as AdaptationResultResponse : undefined;
    const last = lastParsed ? adaptationResultFromResponse(lastParsed, initial, datasources, queries) : undefined;

    return {
        state: input.state,
        createdAt: new Date(input.createdAt),
        processedStates: last ? last.processedStates : 0,
        solutions: last ? last.solutions : [],
    };
}
