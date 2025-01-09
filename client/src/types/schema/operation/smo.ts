import type { Category } from '../Category';

export enum SMOType {
    CreateObjex = 'createObject',
    DeleteObjex = 'deleteObject',
    UpdateObjex = 'updateObject',
    CreateMorphism = 'createMorphism',
    DeleteMorphism = 'deleteMorphism',
    UpdateMorphism = 'updateMorphism',
    Composite = 'composite',
}

export type SMOFromServer<T extends SMOType = SMOType> = {
    type: T;
};

export type SMO<T extends SMOType = SMOType> = {
    readonly type: T;
    toServer(): SMOFromServer<T>;
    up(category: Category): void;
    down(category: Category): void;
}
