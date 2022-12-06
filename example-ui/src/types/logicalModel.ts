import { DatabaseView, DatabaseWithConfiguration, type DatabaseViewFromServer, type DatabaseWithConfigurationFromServer } from "./database";
import { Mapping, type MappingFromServer } from "./mapping";

export class LogicalModel {
    private constructor(
        public readonly id: number,
        public readonly label: string,
        public readonly databaseView: DatabaseWithConfiguration,
        public readonly categoryId: number
    ) {}

    static fromServer(input: LogicalModelFromServer): LogicalModel {
        const databaseView = DatabaseWithConfiguration.fromServer(input.databaseView);
        const json = JSON.parse(input.jsonValue) as { label: string };

        return new LogicalModel(input.id, json.label, databaseView, input.categoryId);
    }
}

export type LogicalModelFromServer = {
    id: number;
    databaseView: DatabaseWithConfigurationFromServer;
    categoryId: number;
    jsonValue: string;
}

export type LogicalModelInit = {
    databaseId: number,
    categoryId: number,
    jsonValue: string
}

export class LogicalModelView {
    private constructor(
        public readonly id: number,
        public readonly label: string,
        public readonly databaseView: DatabaseView,
        public readonly categoryId: number
    ) {}

    static fromServer(input: LogicalModelViewFromServer): LogicalModelView {
        const databaseView = DatabaseView.fromServer(input.databaseView);
        const json = JSON.parse(input.jsonValue) as { label: string };

        return new LogicalModelView(input.id, json.label, databaseView, input.categoryId);
    }
}

export type LogicalModelViewFromServer = {
    id: number;
    databaseView: DatabaseViewFromServer;
    categoryId: number;
    jsonValue: string;
}

export class LogicalModelInfo {
    private constructor(
        public readonly id: number,
        public readonly label: string,
        public readonly categoryId: number
    ) {}

    static fromServer(input: LogicalModelInfoFromServer): LogicalModelInfo {
        const json = JSON.parse(input.jsonValue) as { label: string };

        return new LogicalModelInfo(
            input.id,
            json.label,
            input.categoryId
        );
    }
}

export type LogicalModelInfoFromServer = {
    id: number;
    categoryId: number;
    jsonValue: string;
}

export class LogicalModelFull {
    private constructor(
        public readonly id: number,
        public readonly label: string,
        public readonly categoryId: number,
        public readonly database: DatabaseWithConfiguration,
        public readonly mappings: Mapping[]
    ) {}

    static fromServer(input: LogicalModelFullFromServer): LogicalModelFull {
        const json = JSON.parse(input.jsonValue) as { label: string };

        return new LogicalModelFull(
            input.id,
            json.label,
            input.categoryId,
            DatabaseWithConfiguration.fromServer(input.database),
            input.mappings.map(Mapping.fromServer)
        );
    }
}

export type LogicalModelFullFromServer = {
    id: number;
    categoryId: number;
    jsonValue: string;
    database: DatabaseWithConfigurationFromServer;
    mappings: MappingFromServer[];
}
