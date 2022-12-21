import type { Entity, Id } from "../id";
import type { DeepPartial } from "../utils";
import { DatabaseConfiguration, type DatabaseConfigurationFromServer } from "./Configuration";

export class DatabaseInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly type: Type,
        public readonly label: string,
    ) {}

    static fromServer(input: DatabaseInfoFromServer): DatabaseInfo {
        return new DatabaseInfo(
            input.id,
            input.type,
            input.label
        );
    }
}

export type DatabaseInfoFromServer = {
    id: Id;
    type: Type; // Full type (i.e. mongodb)
    label: string; // User-defined name
}

export class DatabaseWithConfiguration implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly type: Type,
        public readonly label: string,
        public readonly configuration: DatabaseConfiguration
    ) {}

    static fromServer(input: DatabaseWithConfigurationFromServer): DatabaseWithConfiguration {
        return new DatabaseWithConfiguration(
            input.id,
            input.type,
            input.label,
            new DatabaseConfiguration(input.configuration)
        );
    }
}

export type DatabaseWithConfigurationFromServer = {
    id: Id;
    type: Type; // Full type (i.e. mongodb)
    label: string; // User-defined name
    configuration: DatabaseConfigurationFromServer;
}

export type Settings = {
    host: string;
    port: number;
    database: string;
    authenticationDatabase: string;
    username: string;
    password?: string;
}

export type Database = {
    id: Id;
    type: Type;
    label: string;
    settings: Settings;
}

export type DatabaseUpdate = DeepPartial<Omit<Database, 'id'>> & { settings: Partial<Settings> };

export type DatabaseInit = Omit<Database, 'id'>;

export enum Type {
    mongodb = 'mongodb',
    postgresql = 'postgresql'
}

export const DB_TYPES: { type: Type, label: string }[] = [
    {
        type: Type.mongodb,
        label: 'MongoDB'
    },
    {
        type: Type.postgresql,
        label: 'PostgreSQL'
    }
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
            authenticationDatabase: update.settings.authenticationDatabase ?? '',
            username: update.settings.username,
            password: update.settings.password
        }
    };
}
