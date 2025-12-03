import { DatasourceType, type Datasource } from '@/types/Datasource';
import { type Id } from '@/types/id';
import { Key, Signature, type SignatureResponse, type KeyResponse } from '@/types/identifiers';
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

export function adaptationResultFromResponse(input: AdaptationResultResponse, datasources: Datasource[]): AdaptationResult {
    return {
        solutions: input.solutions.map(solutionResponse => {
            const objexes = new ComparableMap<Key, number, AdaptationObjex>(key => key.value);
            for (const objexResponse of solutionResponse.objexes) {
                const objex = adaptationObjexFromResponse(objexResponse, datasources);
                objexes.set(objex.key, objex);
            }

            return {
                id: solutionResponse.id,
                price: solutionResponse.price,
                speedup: solutionResponse.speedup,
                objexes,
            } satisfies AdaptationSolution;
        }),
    };
}

type AdaptationSolutionResponse = {
    id: number;
    price: number;
    /** Relative speedup (more is better). */
    speedup: number;
    objexes: AdaptationObjexResponse[];
};

export type AdaptationSolution = {
    id: number;
    price: number;
    speedup: number;
    objexes: ComparableMap<Key, number, AdaptationObjex>;
};

// TODO This is just temporary structure for adaptation results. Refactor later.

/** @deprecated */
export function mockAdaptationResultResponse(adaptation: Adaptation, datasources: Datasource[]): AdaptationResultResponse {
    const { postgres, mongo, neo4j } = getBasicDatasources(datasources);

    return {
        solutions: [
            mockAdaptationSolutionResponse(adaptation, 42.37, 3.7, {
                [postgres.id]: [ 21, 1 ],
                [mongo.id]: [ 52 ],
                [neo4j.id]: [ 51 ],
            }),
            mockAdaptationSolutionResponse(adaptation, 55.91, 4.5, {
                [mongo.id]: [ 52, 21, 1 ],
                [neo4j.id]: [ 51 ],
            }),
            mockAdaptationSolutionResponse(adaptation, 63.31, 2.9, {
                [postgres.id]: [ 1, 3, 41, 51, 61, 71 ],
            }),
        ].map((solution, index) => ({ id: index + 1, ...solution })),
    };
}

function getBasicDatasources(datasources: Datasource[]) {
    return {
        postgres: datasources.find(d => d.type === DatasourceType.postgresql)!,
        mongo: datasources.find(d => d.type === DatasourceType.mongodb)!,
        neo4j: datasources.find(d => d.type === DatasourceType.neo4j)!,
    };
}

function mockAdaptationSolutionResponse(adaptation: Adaptation, price: number, speedup: number, datasourceToObjexes: Record<Id, number[]>): Omit<AdaptationSolutionResponse, 'id'> {
    const objexes: AdaptationObjexResponse[] = [];

    for (const datasourceId in datasourceToObjexes) {
        for (const key of datasourceToObjexes[datasourceId])
            objexes.push({ key, datasourceId });
    }

    // Fill in the remaining objexes
    adaptation.settings.objexes.forEach(objex => {
        if (objex.datasource && !objexes.some(o => o.key === objex.key.value))
            objexes.push({ key: objex.key.toServer(), datasourceId: objex.datasource.id });
    });

    return {
        price,
        speedup,
        objexes,
    };
}
