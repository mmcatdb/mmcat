import type { Entity, Id, VersionId } from './id';
import { Datasource } from './Datasource';
import { MappingInfo, type MappingInfoResponse } from './mapping';

export type ActionResponse = {
    id: Id;
    categoryId: Id;
    label: string;
    payloads: JobPayloadResponse[];
};

export class Action implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly categoryId: Id,
        public readonly label: string,
        public readonly payloads: JobPayload[],
    ) {}

    static fromResponse(input: ActionResponse): Action {
        return new Action(
            input.id,
            input.categoryId,
            input.label,
            input.payloads.map(jobPayloadFromResponse),
        );
    }
}

export type ActionInit = {
    categoryId: Id;
    label: string;
    payloads: JobPayloadInit[];
};

export enum ActionType {
    ModelToCategory = 'ModelToCategory',
    CategoryToModel = 'CategoryToModel',
    UpdateSchema = 'UpdateSchema',
    RSDToCategory = 'RSDToCategory'
}

export const ACTION_TYPES = [ {
    type: ActionType.ModelToCategory,
    label: 'Model to Category',
}, {
    type: ActionType.CategoryToModel,
    label: 'Category to Model',
}, {
    type: ActionType.RSDToCategory,
    label: 'RSD to Category',
} ];

type JobPayloadType<TType extends ActionType = ActionType> = {
    readonly type: TType;
};

export type JobPayload =
    | ModelToCategoryPayload
    | CategoryToModelPayload
    | UpdateSchemaPayload
    | RSDToCategoryPayload;

export type JobPayloadResponse<T extends ActionType = ActionType> = {
    type: T;
};

export function jobPayloadFromResponse(input: JobPayloadResponse): JobPayload {
    switch (input.type) {
    case ActionType.ModelToCategory:
        return ModelToCategoryPayload.fromResponse(input as ModelToCategoryPayloadResponse);
    case ActionType.CategoryToModel:
        return CategoryToModelPayload.fromResponse(input as CategoryToModelPayloadResponse);
    case ActionType.UpdateSchema:
        return UpdateSchemaPayload.fromResponse(input as UpdateSchemaPayloadResponse);
    case ActionType.RSDToCategory:
        return RSDToCategoryPayload.fromResponse(input as RSDToCategoryPayloadResponse);
    }
}

export type JobPayloadInit =
    | ModelToCategoryPayloadInit
    | CategoryToModelPayloadInit
    | RSDToCategoryPayloadInit;

type ModelToCategoryPayloadResponse = JobPayloadResponse<ActionType.ModelToCategory> & {
    datasource: Datasource;
    mappings: MappingInfoResponse[];
};

class ModelToCategoryPayload implements JobPayloadType<ActionType.ModelToCategory> {
    readonly type = ActionType.ModelToCategory;

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
    type: ActionType.ModelToCategory;
    datasourceId: Id;
    /** If not empty, only the selected mappings from this datasource will be used. */
    mappingIds: Id[];
};

type CategoryToModelPayloadResponse = JobPayloadResponse<ActionType.CategoryToModel> & {
    datasource: Datasource;
    mappings: MappingInfoResponse[];
};

class CategoryToModelPayload implements JobPayloadType<ActionType.CategoryToModel> {
    readonly type = ActionType.CategoryToModel;

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
    type: ActionType.CategoryToModel;
    datasourceId: Id;
    /** If not empty, only the selected mappings from this datasource will be used. */
    mappingIds: Id[];
};

type UpdateSchemaPayloadResponse = JobPayloadResponse<ActionType.UpdateSchema> & {
    prevVersion: VersionId;
    nextVersion: VersionId;
};

class UpdateSchemaPayload implements JobPayloadType<ActionType.UpdateSchema> {
    readonly type = ActionType.UpdateSchema;

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

type RSDToCategoryPayloadResponse = JobPayloadResponse<ActionType.RSDToCategory> & {
    datasources: Datasource[];
};

class RSDToCategoryPayload implements JobPayloadType<ActionType.RSDToCategory> {
    readonly type = ActionType.RSDToCategory;

    private constructor(
        readonly datasources: Datasource[],
    ) {

    }

    static fromResponse(input: RSDToCategoryPayloadResponse): RSDToCategoryPayload {
        return new RSDToCategoryPayload(input.datasources.map(Datasource.fromResponse));
    }
}

export type RSDToCategoryPayloadInit = {
    type: ActionType.RSDToCategory;
    datasourceIds: Id[];
};
