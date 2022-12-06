import { DatabaseView, DatabaseWithConfiguration, type DatabaseViewFromServer, type DatabaseWithConfigurationFromServer } from "./database";
import { Mapping, type MappingFromServer } from "./mapping";

export type LogicalModelInit = {
    databaseId: number,
    categoryId: number,
    jsonValue: string
}

export class LogicalModelInfo {
    private constructor(
        public readonly id: number,
        public readonly label: string
    ) {}

    static fromServer(input: LogicalModelInfoFromServer): LogicalModelInfo {
        const json = JSON.parse(input.jsonValue) as { label: string };

        return new LogicalModelInfo(
            input.id,
            json.label
        );
    }
}

export type LogicalModelInfoFromServer = {
    id: number;
    jsonValue: string;
}

export class LogicalModel {
    private constructor(
        public readonly id: number,
        public readonly label: string,
        public readonly categoryId: number,
        public readonly database: DatabaseWithConfiguration,
        public readonly mappings: Mapping[]
    ) {}

    static fromServer(input: LogicalModelFromServer): LogicalModel {
        const json = JSON.parse(input.jsonValue) as { label: string };

        return new LogicalModel(
            input.id,
            json.label,
            input.categoryId,
            DatabaseWithConfiguration.fromServer(input.database),
            input.mappings.map(Mapping.fromServer)
        );
    }
}

export type LogicalModelFromServer = {
    id: number;
    categoryId: number;
    jsonValue: string;
    database: DatabaseWithConfigurationFromServer;
    mappings: MappingFromServer[];
}
