import type { Category } from '../../schema/Category';

export enum MMOType {
    Objex = 'objexMetadata',
    Morphism = 'morphismMetadata',
}

export type MMOResponse<T extends MMOType = MMOType> = {
    type: T;
};

export type MMO<T extends MMOType = MMOType> = {
    readonly type: T;
    toServer(): MMOResponse<T>;
    up(category: Category): void;
    down(category: Category): void;
};
