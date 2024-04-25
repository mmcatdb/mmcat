import type { Entity, Id } from '../id';
import type { DeepPartial } from '../utils';
import { DatasourceConfiguration, type DatasourceConfigurationFromServer } from './Configuration';

export class DatasourceInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly type: DatasourceType,
        public readonly label: string,
    ) {}

    static fromServer(input: DatasourceInfoFromServer): DatasourceInfo {
        return new DatasourceInfo(
            input.id,
            input.type,
            input.label,
        );
    }
}

export type DatasourceInfoFromServer = {
    id: Id;
    type: DatasourceType;
    label: string; // User-defined name
};

export class DatasourceWithConfiguration implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly type: DatasourceType,
        public readonly label: string,
        public readonly configuration: DatasourceConfiguration,
    ) {}

    static fromServer(input: DatasourceWithConfigurationFromServer): DatasourceWithConfiguration {
        return new DatasourceWithConfiguration(
            input.id,
            input.type,
            input.label,
            new DatasourceConfiguration(input.configuration),
        );
    }
}

export type DatasourceWithConfigurationFromServer = {
    id: Id;
    type: DatasourceType;
    label: string; // User-defined name
    configuration: DatasourceConfigurationFromServer;
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

export type Datasource = {
    id: Id;
    type: DatasourceType;
    label: string;
    settings: Settings;
};

export type DatasourceInit = Omit<Datasource, 'id'>;

export type DatasourceUpdate = DeepPartial<DatasourceInit> & { settings: Partial<Settings> };

export enum DatasourceType {
    mongodb = 'mongodb',
    postgresql = 'postgresql',
    neo4j = 'neo4j',
    csv = 'csv',
    json = 'json',
    jsonLd = 'jsonLd',
}

export function isDatabase(type: DatasourceType): boolean {
    return [ DatasourceType.mongodb, DatasourceType.postgresql, DatasourceType.neo4j ].includes(type);
}

export function isFile(type: DatasourceType): boolean {
    return [ DatasourceType.csv, DatasourceType.json, DatasourceType.jsonLd ].includes(type);
}

export const DATASOURCE_TYPES: { type: DatasourceType, label: string }[] = [
    {
        type: DatasourceType.mongodb,
        label: 'MongoDB',
    },
    {
        type: DatasourceType.postgresql,
        label: 'PostgreSQL',
    },
    {
        type: DatasourceType.neo4j,
        label: 'Neo4j',
    },
    {
        type: DatasourceType.csv,
        label: 'CSV',
    },
    {
        type: DatasourceType.json,
        label: 'JSON',
    },
    {
        type: DatasourceType.jsonLd,
        label: 'JSON-LD',
    },
];

export function copyDatasourceUpdate(datasource: DatasourceUpdate | Datasource): DatasourceUpdate {
    return { ...datasource, settings: { ...datasource.settings } };
}

export function getNewDatasourceUpdate(): DatasourceUpdate {
    return { settings: {} };
}

export function createInitFromUpdate(update: DatasourceUpdate): DatasourceInit | null {
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
