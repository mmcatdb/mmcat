import type { Id, VersionId } from '../id';
import { Datasource } from '../Datasource';
import { MappingInfo, type MappingInfoResponse } from '../mapping';

export enum JobPayloadType {
    ModelToCategory = 'ModelToCategory',
    CategoryToModel = 'CategoryToModel',
    SchemaEvolution = 'SchemaEvolution',
    Inference = 'Inference',
}

export const JOB_PAYLOAD_TYPES: Record<JobPayloadType, { type: JobPayloadType, label: string }> = {
    [JobPayloadType.ModelToCategory]: {
        type: JobPayloadType.ModelToCategory,
        label: 'Model to Category',
    },
    [JobPayloadType.CategoryToModel]: {
        type: JobPayloadType.CategoryToModel,
        label: 'Category to Model',
    },
    [JobPayloadType.SchemaEvolution]: {
        type: JobPayloadType.SchemaEvolution,
        label: 'Update Schema',
    },
    [JobPayloadType.Inference]: {
        type: JobPayloadType.Inference,
        label: 'Inference',
    },
};

type JobPayloadBase<TType extends JobPayloadType = JobPayloadType> = {
    readonly type: TType;
};

export type JobPayload =
    | ModelToCategoryPayload
    | CategoryToModelPayload
    | SchemaEvolutionPayload
    | InferencePayload;

export type JobPayloadResponse<T extends JobPayloadType = JobPayloadType> = {
    type: T;
};

export function jobPayloadFromResponse(input: JobPayloadResponse): JobPayload {
    switch (input.type) {
    case JobPayloadType.ModelToCategory:
        return ModelToCategoryPayload.fromResponse(input as ModelToCategoryPayloadResponse);
    case JobPayloadType.CategoryToModel:
        return CategoryToModelPayload.fromResponse(input as CategoryToModelPayloadResponse);
    case JobPayloadType.SchemaEvolution:
        return SchemaEvolutionPayload.fromResponse(input as SchemaEvolutionPayloadResponse);
    case JobPayloadType.Inference:
        return InferencePayload.fromResponse(input as InferencePayloadResponse);
    }
}

export type JobPayloadInit =
    | ModelToCategoryPayloadInit
    | CategoryToModelPayloadInit
    | InferencePayloadInit;

type ModelToCategoryPayloadResponse = JobPayloadResponse<JobPayloadType.ModelToCategory> & {
    datasource: Datasource;
    mappings: MappingInfoResponse[];
};

class ModelToCategoryPayload implements JobPayloadBase<JobPayloadType.ModelToCategory> {
    readonly type = JobPayloadType.ModelToCategory;

    private constructor(
        readonly datasource: Datasource,
        readonly mappings: MappingInfo[],
    ) {}

    static fromResponse(input: ModelToCategoryPayloadResponse): ModelToCategoryPayload {
        return new ModelToCategoryPayload(
            Datasource.fromResponse(input.datasource),
            input.mappings.map(MappingInfo.fromResponse),
        );
    }
}

export type ModelToCategoryPayloadInit = {
    type: JobPayloadType.ModelToCategory;
    datasourceId: Id;
    /** If not empty, only the selected mappings from this datasource will be used. */
    mappingIds: Id[];
};

type CategoryToModelPayloadResponse = JobPayloadResponse<JobPayloadType.CategoryToModel> & {
    datasource: Datasource;
    mappings: MappingInfoResponse[];
};

class CategoryToModelPayload implements JobPayloadBase<JobPayloadType.CategoryToModel> {
    readonly type = JobPayloadType.CategoryToModel;

    private constructor(
        readonly datasource: Datasource,
        readonly mappings: MappingInfo[],
    ) {}

    static fromResponse(input: CategoryToModelPayloadResponse): CategoryToModelPayload {
        return new CategoryToModelPayload(
            Datasource.fromResponse(input.datasource),
            input.mappings.map(MappingInfo.fromResponse),
        );
    }
}

export type CategoryToModelPayloadInit = {
    type: JobPayloadType.CategoryToModel;
    datasourceId: Id;
    /** If not empty, only the selected mappings from this datasource will be used. */
    mappingIds: Id[];
};

type SchemaEvolutionPayloadResponse = JobPayloadResponse<JobPayloadType.SchemaEvolution> & {
    prevVersion: VersionId;
    nextVersion: VersionId;
};

class SchemaEvolutionPayload implements JobPayloadBase<JobPayloadType.SchemaEvolution> {
    readonly type = JobPayloadType.SchemaEvolution;

    private constructor(
        readonly prevVersion: VersionId,
        readonly nextVersion: VersionId,
    ) {}

    static fromResponse(input: SchemaEvolutionPayloadResponse): SchemaEvolutionPayload {
        return new SchemaEvolutionPayload(
            input.prevVersion,
            input.nextVersion,
        );
    }
}

type InferencePayloadResponse = JobPayloadResponse<JobPayloadType.Inference> & {
    datasources: Datasource[];
};

class InferencePayload implements JobPayloadBase<JobPayloadType.Inference> {
    readonly type = JobPayloadType.Inference;

    private constructor(
        readonly datasources: Datasource[],
    ) {

    }

    static fromResponse(input: InferencePayloadResponse): InferencePayload {
        return new InferencePayload(input.datasources.map(Datasource.fromResponse));
    }
}

export type InferencePayloadInit = {
    type: JobPayloadType.Inference;
    datasourceIds: Id[];
};
