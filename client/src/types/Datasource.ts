import type { Entity, Id } from './id';

export type DatasourceResponse = {
    id: Id;
    type: DatasourceType;
    label: string;
    settings: DatasourceSettings;
};

export class Datasource implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly type: DatasourceType,
        public readonly label: string,
        public readonly settings: DatasourceSettings,
    ) {}

    static fromResponse(input: DatasourceResponse): Datasource {
        return new Datasource(
            input.id,
            input.type,
            input.label,
            input.settings,
        );
    }

    get specs(): DatasourceSpecs {
        return DATASOURCE_TYPES[this.type].specs;
    }
}

export type DatasourceSettings = {
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
    settings: DatasourceSettings;
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

export function validateSettings(settings: DatasourceSettings, type: DatasourceType): boolean {
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

export type DatasourceSpecs = {
    readonly isPropertyToOneAllowed: boolean;
    readonly isPropertyToManyAllowed: boolean;
    readonly isInliningToOneAllowed: boolean;
    readonly isInliningToManyAllowed: boolean;
    readonly isGroupingAllowed: boolean;
    readonly isReferenceAllowed: boolean; // TODO The IC reference algorithm.
    readonly isComplexPropertyAllowed: boolean;
    readonly isSchemaless: boolean;
};

type DatasourceTypeDefinition = {
    type: DatasourceType;
    label: string;
    specs: DatasourceSpecs;
};

export const DATASOURCE_TYPES: Record<DatasourceType, DatasourceTypeDefinition> = {
    [DatasourceType.mongodb]: {
        type: DatasourceType.mongodb,
        label: 'MongoDB',
        specs: {
            isPropertyToOneAllowed: true,
            isPropertyToManyAllowed: true,
            isInliningToOneAllowed: true,
            isInliningToManyAllowed: true,
            isGroupingAllowed: true,
            isReferenceAllowed: true,
            isComplexPropertyAllowed: true,
            isSchemaless: true,
        },
    },
    [DatasourceType.postgresql]: {
        type: DatasourceType.postgresql,
        label: 'PostgreSQL',
        specs: {
            isPropertyToOneAllowed: true,
            isPropertyToManyAllowed: false,
            isInliningToOneAllowed: true,
            isInliningToManyAllowed: false,
            isGroupingAllowed: false,
            isReferenceAllowed: true,
            isComplexPropertyAllowed: false,
            isSchemaless: false,
        },
    },
    [DatasourceType.neo4j]: {
        type: DatasourceType.neo4j,
        label: 'Neo4j',
        specs: {
            isPropertyToOneAllowed: true,
            isPropertyToManyAllowed: false,
            isInliningToOneAllowed: true,
            isInliningToManyAllowed: false,
            isGroupingAllowed: false,
            isReferenceAllowed: false,
            isComplexPropertyAllowed: true, // Just for the _from and _to nodes, false otherwise.
            isSchemaless: true,
        },
    },
    [DatasourceType.csv]: {
        type: DatasourceType.csv,
        label: 'CSV',
        specs: {
            isPropertyToOneAllowed: true,
            isPropertyToManyAllowed: true,
            isInliningToOneAllowed: true,
            isInliningToManyAllowed: true,
            isGroupingAllowed: true,
            isReferenceAllowed: true,
            isComplexPropertyAllowed: true,
            isSchemaless: true,
        },
    },
    [DatasourceType.json]: {
        type: DatasourceType.json,
        label: 'JSON',
        specs: {
            isPropertyToOneAllowed: true,
            isPropertyToManyAllowed: true,
            isInliningToOneAllowed: true,
            isInliningToManyAllowed: true,
            isGroupingAllowed: true,
            isReferenceAllowed: true,
            isComplexPropertyAllowed: true,
            isSchemaless: true,
        },
    },
    [DatasourceType.jsonld]: {
        type: DatasourceType.jsonld,
        label: 'JSON-LD',
        specs: {
            isPropertyToOneAllowed: true,
            isPropertyToManyAllowed: true,
            isInliningToOneAllowed: true,
            isInliningToManyAllowed: true,
            isGroupingAllowed: true,
            isReferenceAllowed: true,
            isComplexPropertyAllowed: true,
            isSchemaless: true,
        },
    },
};
