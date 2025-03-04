import { type Id } from '../id';

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
