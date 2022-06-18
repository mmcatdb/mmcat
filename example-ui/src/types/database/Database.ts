import type { DeepPartial } from "../utils";
import { DatabaseConfiguration, type DatabaseConfigurationFromServer } from "./Configuration";

export class DatabaseView {
    public readonly id: number;
    public readonly type: Type;
    public readonly label: string;
    public configuration: DatabaseConfiguration;

    public constructor(fromServer: DatabaseViewFromServer) {
        this.id = fromServer.id;
        this.type = fromServer.type;
        this.label = fromServer.label;
        this.configuration = new DatabaseConfiguration(fromServer.configuration);
    }
}

export type DatabaseViewFromServer = {
    id: number;
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
    id: number | null;
    type: Type;
    label: string;
    settings: Settings;
}

export type DatabaseUpdate = DeepPartial<Omit<Database, 'id'>> & { settings: Partial<Settings> };

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
