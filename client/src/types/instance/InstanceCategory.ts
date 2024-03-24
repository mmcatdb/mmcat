import { ComparableMap } from '@/utils/ComparableMap';
import { Key, Signature, SignatureId, type KeyFromServer, type SignatureFromServer, type SignatureIdFromServer } from '../identifiers';
import type { Id } from '../id';
import type { InstanceObjectFromServer } from './InstanceObject';
import type { InstanceMorphismFromServer } from './InstanceMorphism';

export class InstanceCategory {
    private constructor(
    ) {}

    static fromServer(input: InstanceCategoryFromServer): InstanceCategory {
        return new InstanceCategory(
        );
    }
}

export type InstanceCategoryFromServer = {
    sessionId: Id;
    categoryId: Id;
    objects: InstanceObjectFromServer[];
    morphism: InstanceMorphismFromServer[];
};
