import { type Id } from '../id';

// FIXME To samé co na BE.
// Dále, proč má tento (a další typové soubory) příponu .tsx? To by mělo být .ts.

export type AdminerReferences = AdminerReference[];

export type AdminerReference = {
    referencedDatasourceId: Id;
    referencedKindName: string;
    referencedProperty: string;
    referencingDatasourceId: Id;
    referencingKindName: string;
    referencingProperty: string;
};

export type KindReference = {
    referencingProperty: string;
    datasourceId: Id;
    kindName: string;
    property: string;
};
