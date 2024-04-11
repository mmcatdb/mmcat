import type { Entity, Id } from '../id';
import type { DeepPartial } from '../utils';
import { DataInputConfiguration, type DataInputConfigurationFromServer } from './Configuration';

export class DataInputInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly databaseType: Type,
        public readonly dataSourceType: Type,
        public readonly label: string,
    ) {}

    static fromServer(input: DataInputInfoFromServer): DataInputInfo {
        return new DataInputInfo(
            input.id,
            input.databaseType,
            input.dataSourceType,
            input.label,
        );
    }
}

export type DataInputInfoFromServer = {
    id: Id;
    databaseType: Type; // Full type (i.e. mongodb)
    dataSourceType: Type;
    label: string; // User-defined name
};

export class DataInputWithConfiguration implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly databaseType: Type,
        public readonly dataSourceType: Type,
        public readonly label: string,
        public readonly configuration: DataInputConfiguration,
    ) {}

    static fromServer(input: DataInputWithConfigurationFromServer): DataInputWithConfiguration {
        return new DataInputWithConfiguration(
            input.id,
            input.databaseType,
            input.dataSourceType,
            input.label,
            new DataInputConfiguration(input.configuration),
        );
    }
}

export type DataInputWithConfigurationFromServer = {
    id: Id;
    databaseType: Type; // Full type (i.e. mongodb)
    dataSourceType: Type;
    label: string; // User-defined name
    configuration: DataInputConfigurationFromServer;
};

export type Settings = {
    host: string;
    port: number;
    database: string;
    authenticationDatabase?: string;
    username: string;
    password?: string;
};

export type Database = {
    id: Id;
    type: Type;
    label: string;
    settings: Settings;
};

export type DatabaseInit = Omit<Database, 'id'>;

export type DatabaseUpdate = DeepPartial<DatabaseInit> & { settings: Partial<Settings> };

export enum Type {
    mongodb = 'mongodb',
    postgresql = 'postgresql',
    neo4j = 'neo4j'
}

export const DB_TYPES: { type: Type, label: string }[] = [
    {
        type: Type.mongodb,
        label: 'MongoDB',
    },
    {
        type: Type.postgresql,
        label: 'PostgreSQL',
    },
    {
        type: Type.neo4j,
        label: 'Neo4j',
    },
];

export function copyDatabaseUpdate(database: DatabaseUpdate | Database): DatabaseUpdate {
    return { ...database, settings: { ...database.settings } };
}

export function getNewDatabaseUpdate(): DatabaseUpdate {
    return { settings: {} };
}

export function createInitFromUpdate(update: DatabaseUpdate): DatabaseInit | null {
    if (
        !update.type ||
        !update.label ||
        !update.settings.host ||
        !update.settings.port ||
        !update.settings.database ||
        (
            update.type === Type.mongodb && !update.settings.authenticationDatabase
        ) ||
        !update.settings.username ||
        !update.settings.password
    )
        return null;

    return {
        type: update.type,
        label: update.label,
        settings: {
            host: update.settings.host,
            port: update.settings.port,
            database: update.settings.database,
            authenticationDatabase: update.settings.authenticationDatabase ?? undefined,
            username: update.settings.username,
            password: update.settings.password,
        },
    };
}
