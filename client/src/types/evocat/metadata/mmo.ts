import type { Category } from '../../schema/Category';

export enum MMOType {
    Objex = 'objectMetadata',
    Morphism = 'morphismMetadata',
}

export type MMOFromServer<T extends MMOType = MMOType> = {
    type: T;
};

export type MMO<T extends MMOType = MMOType> = {
    readonly type: T;
    toServer(): MMOFromServer<T>;
    up(category: Category): void;
    down(category: Category): void;
}
