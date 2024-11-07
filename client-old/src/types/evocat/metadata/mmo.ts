import type { SchemaCategory } from '../../schema/SchemaCategory';

export enum MMOType {
    Object = 'objectMetadata',
    Morphism = 'morphismMetadata',
}

export type MMOFromServer<T extends MMOType = MMOType> = {
    type: T;
};

export interface MMO<T extends MMOType = MMOType> {
    readonly type: T;
    toServer(): MMOFromServer<T>;
    up(category: SchemaCategory): void;
    down(category: SchemaCategory): void;
}
