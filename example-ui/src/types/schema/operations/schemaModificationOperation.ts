import type { SchemaCategory } from "../SchemaCategory";

export enum SMOType {
    CreateObject = 'createObject',
    DeleteObject = 'deleteObject',
    EditObject = 'editObject',
    CreateMorphism = 'createMorphism',
    DeleteMorphism = 'deleteMorphism',
    EditMorphism = 'editMorphism',
    Composite = 'composite',
}

export type SMOFromServer<T extends SMOType = SMOType> = {
    type: T;
};

export interface SMO<T extends SMOType = SMOType> {
    readonly type: T;
    toServer(): SMOFromServer<T>;
    up(category: SchemaCategory): void;
    down(category: SchemaCategory): void;
}
