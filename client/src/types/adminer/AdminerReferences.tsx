import { type Id } from '../id';

export type AdminerReferences = AdminerReference[];

export type AdminerReference = {
    referencedDatasourceId: Id;
    referencedKindName: string;
    referencedProperty: string;
    referencingKindName: string;
    referencingProperty: string;
};
