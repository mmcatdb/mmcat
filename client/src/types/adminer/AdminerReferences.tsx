import { type Id } from '../id';

export type AdminerReferences = AdminerReference[];

export type AdminerReference = {
    datasourceId: Id;
    referencedKindName: string;
    referencedProperty: string;
    referencingKindName: string;
    referencingProperty: string;
};
