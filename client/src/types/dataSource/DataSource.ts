import type { Entity, Id } from '../id';
import type { DeepPartial } from '../utils';
import { DataSourceConfiguration, type DataSourceConfigurationFromServer } from './Configuration';

export class DataSourceInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly type: DataSourceType,
        public readonly label: string,
    ) {}

    static fromServer(input: DataSourceInfoFromServer): DataSourceInfo {
        return new DataSourceInfo(
            input.id,
            input.type,
            input.label,
        );
    }
}

export type DataSourceInfoFromServer = {
    id: Id;
    type: DataSourceType;
    label: string; // User-defined name
};

export class DataSourceWithConfiguration implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly type: DataSourceType,
        public readonly label: string,
        public readonly configuration: DataSourceConfiguration,
    ) {}

    static fromServer(input: DataSourceWithConfigurationFromServer): DataSourceWithConfiguration {
        return new DataSourceWithConfiguration(
            input.id,
            input.type,
            input.label,
            new DataSourceConfiguration(input.configuration),
        );
    }
}

export type DataSourceWithConfigurationFromServer = {
    id: Id;
    type: DataSourceType;
    label: string; // User-defined name
    configuration: DataSourceConfigurationFromServer;
};

export type Settings = {
    url?: string;
    host?: string;
    port?: number;
    database?: string;
    authenticationDatabase?: string;
    username?: string;
    password?: string;
};

export type DataSource = {
    id: Id;
    type: DataSourceType;
    label: string;
    settings: Settings;
};

export type DataSourceInit = Omit<DataSource, 'id'>;

export type DataSourceUpdate = DeepPartial<DataSourceInit> & { settings: Partial<Settings> };

export enum DataSourceType {
    mongodb = 'mongodb',
    postgresql = 'postgresql',
    neo4j = 'neo4j',
    csv = 'csv',
    json = 'json',
    jsonLd = 'jsonLd',
}

export function isDatabase(type: DataSourceType): boolean {
    return [ DataSourceType.mongodb, DataSourceType.postgresql, DataSourceType.neo4j ].includes(type);
}

export function isFile(type: DataSourceType): boolean {
    return [ DataSourceType.csv, DataSourceType.json, DataSourceType.jsonLd ].includes(type);
}

export const DATA_SOURCE_TYPES: { type: DataSourceType, label: string }[] = [
    {
        type: DataSourceType.mongodb,
        label: 'MongoDB',
    },
    {
        type: DataSourceType.postgresql,
        label: 'PostgreSQL',
    },
    {
        type: DataSourceType.neo4j,
        label: 'Neo4j',
    },
    {
        type: DataSourceType.csv,
        label: 'CSV',
    },
    {
        type: DataSourceType.json,
        label: 'JSON',
    },
    {
        type: DataSourceType.jsonLd,
        label: 'JSON-LD',
    },
];

export function copyDataSourceUpdate(dataSource: DataSourceUpdate | DataSource): DataSourceUpdate {
    return { ...dataSource, settings: { ...dataSource.settings } };
}

export function getNewDataSourceUpdate(): DataSourceUpdate {
    return { settings: {} };
}

export function createInitFromUpdate(update: DataSourceUpdate): DataSourceInit | null {
    if (!update.type || !update.label) 
        return null;

    return {
        type: update.type,
        label: update.label,
        settings: {
            url: update.settings.url,
            host: update.settings.host,
            port: update.settings.port,
            database: update.settings.database,
            authenticationDatabase: update.settings.authenticationDatabase,
            username: update.settings.username,
            password: update.settings.password,
        },
    };
}
