import type { Entity, Id, VersionId } from './id';
import {  Datasource, type DatasourceFromServer } from './datasource';
import { MappingInfo, type MappingInfoFromServer } from './mapping';

export type ActionFromServer = {
    id: Id;
    categoryId: Id;
    label: string;
    payloads: JobPayloadFromServer[];
};

export class Action implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly categoryId: Id,
        public readonly label: string,
        public readonly payloads: JobPayload[],
    ) {}

    static fromServer(input: ActionFromServer): Action {
        return new Action(
            input.id,
            input.categoryId,
            input.label,
            input.payloads.map(jobPayloadFromServer),
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

export const ACTION_TYPES = [
    {
        label: 'Model to Category',
        value: ActionType.ModelToCategory,
    },
    {
        label: 'Category to Model',
        value: ActionType.CategoryToModel,
    },
    {
        label: 'RSD to Category',
        value: ActionType.RSDToCategory,
    },
];

type JobPayloadType<TType extends ActionType = ActionType> = {
    readonly type: TType;
}

export type JobPayload =
    | ModelToCategoryPayload
    | CategoryToModelPayload
    | UpdateSchemaPayload
    | RSDToCategoryPayload
    ;

export type JobPayloadFromServer<T extends ActionType = ActionType> = {
    type: T;
};

export function jobPayloadFromServer(input: JobPayloadFromServer): JobPayload {
    switch (input.type) {
    case ActionType.ModelToCategory:
        return ModelToCategoryPayload.fromServer(input as ModelToCategoryPayloadFromServer);
    case ActionType.CategoryToModel:
        return CategoryToModelPayload.fromServer(input as CategoryToModelPayloadFromServer);
    case ActionType.UpdateSchema:
        return UpdateSchemaPayload.fromServer(input as UpdateSchemaPayloadFromServer);
    case ActionType.RSDToCategory:
        return RSDToCategoryPayload.fromServer(input as RSDToCategoryPayloadFromServer);
    }
}

export type JobPayloadInit = {
    type: ActionType.ModelToCategory | ActionType.CategoryToModel;
    datasourceId: Id;
    /** If not empty, only the selected mappings from this datasource will be used. */
    mappingIds: Id[];
} | {
    type: ActionType.RSDToCategory;
    datasourceIds: Id[];
};

type ModelToCategoryPayloadFromServer = JobPayloadFromServer<ActionType.ModelToCategory> & {
    datasource: DatasourceFromServer;
    mappings: MappingInfoFromServer[];
};

class ModelToCategoryPayload implements JobPayloadType<ActionType.ModelToCategory> {
    readonly type = ActionType.ModelToCategory;

    private constructor(
        readonly datasource: Datasource,
        readonly mappings: MappingInfo[],
    ) {}

    static fromServer(input: ModelToCategoryPayloadFromServer): ModelToCategoryPayload {
        return new ModelToCategoryPayload(
            Datasource.fromServer(input.datasource),
            input.mappings.map(MappingInfo.fromServer),
        );
    }
}

type CategoryToModelPayloadFromServer = JobPayloadFromServer<ActionType.CategoryToModel> & {
    datasource: DatasourceFromServer;
    mappings: MappingInfoFromServer[];
};

class CategoryToModelPayload implements JobPayloadType<ActionType.CategoryToModel> {
    readonly type = ActionType.CategoryToModel;

    private constructor(
        readonly datasource: Datasource,
        readonly mappings: MappingInfo[],
    ) {}

    static fromServer(input: CategoryToModelPayloadFromServer): CategoryToModelPayload {
        return new CategoryToModelPayload(
            Datasource.fromServer(input.datasource),
            input.mappings.map(MappingInfo.fromServer),
        );
    }
}

type UpdateSchemaPayloadFromServer = JobPayloadFromServer<ActionType.UpdateSchema> & {
    prevVersion: VersionId;
    nextVersion: VersionId;
};

class UpdateSchemaPayload implements JobPayloadType<ActionType.UpdateSchema> {
    readonly type = ActionType.UpdateSchema;

    private constructor(
        readonly prevVersion: VersionId,
        readonly nextVersion: VersionId,
    ) {}

    static fromServer(input: UpdateSchemaPayloadFromServer): UpdateSchemaPayload {
        return new UpdateSchemaPayload(
            input.prevVersion,
            input.nextVersion,
        );
    }
}

type RSDToCategoryPayloadFromServer = JobPayloadFromServer<ActionType.RSDToCategory> & {
    datasources: DatasourceFromServer[];
};

class RSDToCategoryPayload implements JobPayloadType<ActionType.RSDToCategory> {
    readonly type = ActionType.RSDToCategory;

    private constructor(
        readonly datasources: Datasource[],
    ) {

    }

    static fromServer(input: RSDToCategoryPayloadFromServer): RSDToCategoryPayload {
        return new RSDToCategoryPayload(input.datasources.map(Datasource.fromServer));
    }
}

