import { DatabaseWithConfiguration, type DatabaseWithConfigurationFromServer } from "./database";
import type { Id, Entity } from "./id";
import { Mapping, type MappingFromServer } from "./mapping";

export type LogicalModelInit = {
    databaseId: Id;
    categoryId: Id;
    jsonValue: string;
}

export class LogicalModelInfo implements Entity {
    private constructor(
        public readonly id: Id,
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
    id: Id;
    jsonValue: string;
}

export class LogicalModel implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly categoryId: Id,
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
    id: Id;
    categoryId: Id;
    jsonValue: string;
    database: DatabaseWithConfigurationFromServer;
    mappings: MappingFromServer[];
}
