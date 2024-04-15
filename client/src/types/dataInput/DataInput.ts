import type { Entity, Id } from '../id';
import type { DeepPartial } from '../utils';
import { DataInputConfiguration, type DataInputConfigurationFromServer } from './Configuration';

export class DataInputInfo implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly type: Type,
        public readonly label: string,
    ) {}

    static fromServer(input: DataInputInfoFromServer): DataInputInfo {
        return new DataInputInfo(
            input.id,
            input.type,
            input.label,
        );
    }
}

export type DataInputInfoFromServer = {
    id: Id;
    type: Type;
    label: string; // User-defined name
};

export class DataInputWithConfiguration implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly type: Type,
        public readonly label: string,
        public readonly configuration: DataInputConfiguration,
    ) {}

    static fromServer(input: DataInputWithConfigurationFromServer): DataInputWithConfiguration {
        return new DataInputWithConfiguration(
            input.id,
            input.type,
            input.label,
            new DataInputConfiguration(input.configuration),
        );
    }
}

export type DataInputWithConfigurationFromServer = {
    id: Id;
    type: Type;
    label: string; // User-defined name
    configuration: DataInputConfigurationFromServer;
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

export type DataInput = {
    id: Id;
    type: Type;
    label: string;
    settings: Settings;
};

export type DataInputInit = Omit<DataInput, 'id'>;

export type DataInputUpdate = DeepPartial<DataInputInit> & { settings: Partial<Settings> };

export enum Type {
    Csv = 'Csv',
    Json = 'Json',
    JsonLdStore = 'JsonLdStore',
    mongodb = 'mongodb',
    postgresql = 'postgresql',
    neo4j = 'neo4j'
}

export const DI_TYPES: { type: Type, label: string }[] = [
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
    {
        type: Type.Csv,
        label: 'Csv'
    },
    {
        type: Type.Json,
        label: 'Json'
    },
    {
        type: Type.JsonLdStore,
        label: 'JsonLdStore'
    },
];

export function copyDataInputUpdate(dataInput: DataInputUpdate | DataInput): DataInputUpdate {
    return { ...dataInput, settings: { ...dataInput.settings } };
}

export function getNewDataInputUpdate(): DataInputUpdate {
    return { settings: {} };
}

export function createInitFromUpdate(update: DataInputUpdate): DataInputInit | null {
    if (!update.type || !update.label) {
        return null;
    }
    /*
    if (
        !update.type ||
        !update.label ||
        !update.settings.url ||
        !update.settings.host ||
        !update.settings.port ||
        !update.settings.database ||
        (
            update.type === Type.mongodb && !update.settings.authenticationDatabase
        ) ||
        !update.settings.username ||
        !update.settings.password
    )
        return null;*/

    return {
        type: update.type,
        label: update.label,
        settings: {
            url: update.settings.url ?? undefined,
            host: update.settings.host ?? undefined,
            port: update.settings.port ?? undefined,
            database: update.settings.database ?? undefined,
            authenticationDatabase: update.settings.authenticationDatabase ?? undefined, // what about here??
            username: update.settings.username ?? undefined,
            password: update.settings.password ?? undefined,
        },
    };
}
