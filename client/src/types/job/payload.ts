import type { Id, VersionId } from '../id';
import { Datasource } from '../Datasource';
import { MappingInfo, type MappingInfoResponse } from '../mapping';

export enum JobPayloadType {
    ModelToCategory = 'ModelToCategory',
    CategoryToModel = 'CategoryToModel',
    UpdateSchema = 'UpdateSchema',
    RSDToCategory = 'RSDToCategory',
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
    [JobPayloadType.UpdateSchema]: {
        type: JobPayloadType.UpdateSchema,
        label: 'Update Schema',
    },
    [JobPayloadType.RSDToCategory]: {
        type: JobPayloadType.RSDToCategory,
        label: 'RSD to Category',
    },
};

type JobPayloadBase<TType extends JobPayloadType = JobPayloadType> = {
    readonly type: TType;
};

export type JobPayload =
    | ModelToCategoryPayload
    | CategoryToModelPayload
    | UpdateSchemaPayload
    | RSDToCategoryPayload;

export type JobPayloadResponse<T extends JobPayloadType = JobPayloadType> = {
    type: T;
};

export function jobPayloadFromResponse(input: JobPayloadResponse): JobPayload {
    switch (input.type) {
    case JobPayloadType.ModelToCategory:
        return ModelToCategoryPayload.fromResponse(input as ModelToCategoryPayloadResponse);
    case JobPayloadType.CategoryToModel:
        return CategoryToModelPayload.fromResponse(input as CategoryToModelPayloadResponse);
    case JobPayloadType.UpdateSchema:
        return UpdateSchemaPayload.fromResponse(input as UpdateSchemaPayloadResponse);
    case JobPayloadType.RSDToCategory:
        return RSDToCategoryPayload.fromResponse(input as RSDToCategoryPayloadResponse);
    }
}

export type JobPayloadInit =
    | ModelToCategoryPayloadInit
    | CategoryToModelPayloadInit
    | RSDToCategoryPayloadInit;

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

type UpdateSchemaPayloadResponse = JobPayloadResponse<JobPayloadType.UpdateSchema> & {
    prevVersion: VersionId;
    nextVersion: VersionId;
};

class UpdateSchemaPayload implements JobPayloadBase<JobPayloadType.UpdateSchema> {
    readonly type = JobPayloadType.UpdateSchema;

    private constructor(
        readonly prevVersion: VersionId,
        readonly nextVersion: VersionId,
    ) {}

    static fromResponse(input: UpdateSchemaPayloadResponse): UpdateSchemaPayload {
        return new UpdateSchemaPayload(
            input.prevVersion,
            input.nextVersion,
        );
    }
}

type RSDToCategoryPayloadResponse = JobPayloadResponse<JobPayloadType.RSDToCategory> & {
    datasources: Datasource[];
};

class RSDToCategoryPayload implements JobPayloadBase<JobPayloadType.RSDToCategory> {
    readonly type = JobPayloadType.RSDToCategory;

    private constructor(
        readonly datasources: Datasource[],
    ) {

    }

    static fromResponse(input: RSDToCategoryPayloadResponse): RSDToCategoryPayload {
        return new RSDToCategoryPayload(input.datasources.map(Datasource.fromResponse));
    }
}

export type RSDToCategoryPayloadInit = {
    type: JobPayloadType.RSDToCategory;
    datasourceIds: Id[];
};
