import { type Id } from '../id';

export type AdminerReferences = AdminerReference[];

export type AdminerReference = {
    from: AdminerReferenceKind;
    to: AdminerReferenceKind;
};

export type AdminerReferenceKind = {
    datasourceId: Id;
    kindName: string;
    property: string;
};

export type KindReference = {
    fromProperty: string;
} & AdminerReferenceKind;
