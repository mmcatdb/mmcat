import type { Entity, Id } from '../id';

export enum Type {
    JsonLdStore = 'JsonLdStore'
}

export const DATA_SOURCE_TYPES: { type: Type, label: string }[] = [
    {
        type: Type.JsonLdStore,
        label: 'JSON-LD store',
    },
];

export class DataSource implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly url: string,
        public readonly label: string,
        public readonly type: Type,
    ) {}

    static fromServer(input: DataSourceFromServer): DataSource {
        return new DataSource(
            input.id,
            input.url,
            input.label,
            input.type,
        );
    }
}

export type DataSourceFromServer = {
    id: Id;
    url: string;
    label: string;
    type: Type;
};

export type DataSourceInit = Omit<DataSource, 'id'>;

export type DataSourceUpdate = Partial<Omit<DataSourceInit, 'type'>>;
