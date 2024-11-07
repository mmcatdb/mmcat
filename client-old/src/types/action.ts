import type { Entity, Id, VersionId } from './id';
import {  Datasource, type DatasourceFromServer } from './datasource';
import { MappingInfo, type MappingInfoFromServer } from './mapping';

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
    UpdateSchema = 'UpdateSchema',
    RSDToCategory = 'RSDToCategory'
}

export const ACTION_TYPES = [ {
    label: 'Model to Category',
    value: ActionType.ModelToCategory,
}, {
    label: 'Category to Model',
    value: ActionType.CategoryToModel,
}, {
    label: 'RSD to Category',
    value: ActionType.RSDToCategory,
} ] as const;

type ActionPayloadType<TType extends ActionType = ActionType> = {
    readonly type: TType;
};

export type ActionPayload =
    | ModelToCategoryPayload
    | CategoryToModelPayload
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
    case ActionType.UpdateSchema:
        return UpdateSchemaPayload.fromServer(input as UpdateSchemaPayloadFromServer);
    case ActionType.RSDToCategory:
        return RSDToCategoryPayload.fromServer(input as RSDToCategoryPayloadFromServer);
    }
}

export type ActionPayloadInit = {
    type: ActionType.ModelToCategory | ActionType.CategoryToModel;
    datasourceId: Id;
    /** If provided, only the selected mappings from this datasource will be used. */
    mappingIds: Id[] | undefined;
} | {
    type: ActionType.RSDToCategory;
    datasourceIds: Id[];
};

type ModelToCategoryPayloadFromServer = ActionPayloadFromServer<ActionType.ModelToCategory> & {
    datasource: DatasourceFromServer;
    mappings: MappingInfoFromServer[];
};

class ModelToCategoryPayload implements ActionPayloadType<ActionType.ModelToCategory> {
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

type CategoryToModelPayloadFromServer = ActionPayloadFromServer<ActionType.CategoryToModel> & {
    datasource: DatasourceFromServer;
    mappings: MappingInfoFromServer[];
};

class CategoryToModelPayload implements ActionPayloadType<ActionType.CategoryToModel> {
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
    datasources: DatasourceFromServer[];
};

class RSDToCategoryPayload implements ActionPayloadType<ActionType.RSDToCategory> {
    readonly type = ActionType.RSDToCategory;

    private constructor(
        readonly datasources: Datasource[],
    ) {

    }

    static fromServer(input: RSDToCategoryPayloadFromServer): RSDToCategoryPayload {
        return new RSDToCategoryPayload(input.datasources.map(Datasource.fromServer));
    }
}

