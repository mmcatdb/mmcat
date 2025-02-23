import type { Entity, Id } from '../id';
import { DatasourceConfiguration, type DatasourceConfigurationFromServer } from './Configuration';

export type DatasourceFromServer = {
    id: Id;
    type: DatasourceType;
    label: string;
    settings: Settings;
    configuration: DatasourceConfigurationFromServer;
};

export class Datasource implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly type: DatasourceType,
        public readonly label: string,
        public readonly settings: Settings,
        public readonly configuration: DatasourceConfiguration,
    ) {}

    static fromServer(input: DatasourceFromServer): Datasource {
        return new Datasource(
            input.id,
            input.type,
            input.label,
            input.settings,
            new DatasourceConfiguration(input.configuration),
        );
    }
}

export type Settings = {
    url?: string;
    host?: string;
    port?: number;
    database?: string;
    authenticationDatabase?: string;
    username?: string;
    password?: string;
    /** For csv. Needs to be one character. */
    separator?: string;
    /** For csv. */
    hasHeader?: boolean;
    isWritable?: boolean;
    isQueryable?: boolean;
};

export type DatasourceInit = {
    type: DatasourceType;
    label: string;
    settings: Settings;
};

export type DatasourceUpdate = Omit<DatasourceInit, 'type'>;

export enum DatasourceType {
    mongodb = 'mongodb',
    postgresql = 'postgresql',
    neo4j = 'neo4j',
    csv = 'csv',
    json = 'json',
    jsonld = 'jsonld',
}

export function isDatabase(type: DatasourceType): boolean {
    return [ DatasourceType.mongodb, DatasourceType.postgresql, DatasourceType.neo4j ].includes(type);
}

export function isFile(type: DatasourceType): boolean {
    return [ DatasourceType.csv, DatasourceType.json, DatasourceType.jsonld ].includes(type);
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
        type: DatasourceType.jsonld,
        label: 'JSON-LD',
    },
];

export function validateSettings(settings: Settings, type: DatasourceType): boolean {
    if (type === DatasourceType.csv && settings.separator?.length !== 1)
        return false;

    if (isFile(type))
        return !!settings.url;

    if (!settings.host || !settings.port || !settings.database || !settings.username || !settings.password)
        return false;

    if (type === DatasourceType.mongodb && !settings.authenticationDatabase)
        return false;

    return true;
}
