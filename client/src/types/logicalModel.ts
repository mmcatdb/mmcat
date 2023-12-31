import { DatabaseWithConfiguration, type DatabaseInfoFromServer, type DatabaseWithConfigurationFromServer, DatabaseInfo } from './database';
import type { Id, Entity } from './id';
import { Mapping, type MappingFromServer } from './mapping';

export type LogicalModelInit = {
    databaseId: Id;
    categoryId: Id;
    label: string;
};

export class LogicalModelInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly database: DatabaseInfo,
    ) {}

    static fromServer(input: LogicalModelInfoFromServer): LogicalModelInfo {
        return new LogicalModelInfo(
            input.id,
            input.label,
            DatabaseInfo.fromServer(input.database),
        );
    }
}

export type LogicalModelInfoFromServer = {
    id: Id;
    label: string;
    database: DatabaseInfoFromServer;
};

export class LogicalModel implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly categoryId: Id,
        public readonly database: DatabaseWithConfiguration,
        public readonly mappings: Mapping[],
    ) {}

    static fromServer(input: LogicalModelFromServer): LogicalModel {

        return new LogicalModel(
            input.id,
            input.label,
            input.categoryId,
            DatabaseWithConfiguration.fromServer(input.database),
            input.mappings.map(Mapping.fromServer),
        );
    }
}

export type LogicalModelFromServer = {
    id: Id;
    categoryId: Id;
    label: string;
    database: DatabaseWithConfigurationFromServer;
    mappings: MappingFromServer[];
};
