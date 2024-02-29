import { DataSource, type DataSourceFromServer } from './dataSource';
import type { Entity, Id, VersionId } from './id';
import { LogicalModelInfo, type LogicalModelInfoFromServer } from './logicalModel';

export type ActionFromServer = {
    id: Id;
    categoryId: Id;
    label: string;
    payload: ActionPayloadFromServer;
};

export class Action implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly categoryId: Id,
        public readonly label: string,
        public readonly payload: ActionPayload,
    ) {}

    static fromServer(input: ActionFromServer): Action {
        return new Action(
            input.id,
            input.categoryId,
            input.label,
            actionPayloadFromServer(input.payload),
        );
    }

}

export type ActionInit = {
    categoryId: Id;
    label: string;
    payload: ActionPayloadInit;
};

export enum ActionType {
    ModelToCategory = 'ModelToCategory',
    CategoryToModel = 'CategoryToModel',
    JsonLdToCategory = 'JsonLdToCategory',
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
        label: 'Import data', // TODO
        value: ActionType.JsonLdToCategory,
    },
    {
        label: 'RSD to Category',
        value: ActionType.RSDToCategory,
    },
];

interface ActionPayloadType<TType extends ActionType = ActionType> {
    readonly type: TType;
}

export type ActionPayload =
    | ModelToCategoryPayload
    | CategoryToModelPayload
    | JsonLdToCategoryPayload
    | UpdateSchemaPayload
    | RSDToCategoryPayload
    ;

export type ActionPayloadFromServer<T extends ActionType = ActionType> = {
    type: T;
};

export function actionPayloadFromServer(input: ActionPayloadFromServer): ActionPayload {
    switch (input.type) {
    case ActionType.ModelToCategory:
        return ModelToCategoryPayload.fromServer(input as ModelToCategoryPayloadFromServer);
    case ActionType.CategoryToModel:
        return CategoryToModelPayload.fromServer(input as CategoryToModelPayloadFromServer);
    case ActionType.JsonLdToCategory:
        return JsonLdToCategoryPayload.fromServer(input as JsonLdToCategoryPayloadFromServer);
    case ActionType.UpdateSchema:
        return UpdateSchemaPayload.fromServer(input as UpdateSchemaPayloadFromServer);
    case ActionType.RSDToCategory:
        return RSDToCategoryPayload.fromServer(input as RSDToCategoryPayloadFromServer);
    }
}

export type ActionPayloadInit = {
    type: ActionType.ModelToCategory | ActionType.CategoryToModel;
    logicalModelId: Id;
} | {
    type: ActionType.JsonLdToCategory | ActionType.RSDToCategory;
    dataSourceId: Id;
};

type ModelToCategoryPayloadFromServer = ActionPayloadFromServer<ActionType.ModelToCategory> & {
    logicalModel: LogicalModelInfoFromServer;
};

class ModelToCategoryPayload implements ActionPayloadType<ActionType.ModelToCategory> {
    readonly type = ActionType.ModelToCategory;

    private constructor(
        readonly logicalModel: LogicalModelInfo,
    ) {}

    static fromServer(input: ModelToCategoryPayloadFromServer): ModelToCategoryPayload {
        return new ModelToCategoryPayload(
            LogicalModelInfo.fromServer(input.logicalModel),
        );
    }
}

type CategoryToModelPayloadFromServer = ActionPayloadFromServer<ActionType.CategoryToModel> & {
    logicalModel: LogicalModelInfoFromServer;
};

class CategoryToModelPayload implements ActionPayloadType<ActionType.CategoryToModel> {
    readonly type = ActionType.CategoryToModel;

    private constructor(
        readonly logicalModel: LogicalModelInfo,
    ) {}

    static fromServer(input: CategoryToModelPayloadFromServer): CategoryToModelPayload {
        return new CategoryToModelPayload(
            LogicalModelInfo.fromServer(input.logicalModel),
        );
    }
}

type JsonLdToCategoryPayloadFromServer = ActionPayloadFromServer<ActionType.JsonLdToCategory> & {
    dataSource: DataSourceFromServer;
};

class JsonLdToCategoryPayload implements ActionPayloadType<ActionType.JsonLdToCategory> {
    readonly type = ActionType.JsonLdToCategory;

    private constructor(
        readonly dataSource: DataSource,
    ) {}

    static fromServer(input: JsonLdToCategoryPayloadFromServer): JsonLdToCategoryPayload {
        return new JsonLdToCategoryPayload(
            DataSource.fromServer(input.dataSource),
        );
    }
}

type UpdateSchemaPayloadFromServer = ActionPayloadFromServer<ActionType.UpdateSchema> & {
    prevVersion: VersionId;
    nextVersion: VersionId;
};

class UpdateSchemaPayload implements ActionPayloadType<ActionType.UpdateSchema> {
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

type RSDToCategoryPayloadFromServer = ActionPayloadFromServer<ActionType.RSDToCategory> & {
    dataSource: DataSourceFromServer;
};

class RSDToCategoryPayload implements ActionPayloadType<ActionType.RSDToCategory> {
    readonly type = ActionType.RSDToCategory;

    private constructor(
        readonly dataSource: DataSource,
    ) {}

    static fromServer(input: RSDToCategoryPayloadFromServer): RSDToCategoryPayload {
        return new RSDToCategoryPayload(
            DataSource.fromServer(input.dataSource),
        );
    }
}

