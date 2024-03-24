import type { Empty, StringLike } from '@/types/api/routes';
import { GET } from '../routeFunctions';
import type { InstanceCategoryFromServer, InstanceMorphismFromServer, InstanceObjectFromServer } from '@/types/instance';

const instances = {
    /** @deprecated */
    getInstanceObject: GET<{ categoryId: StringLike, objectKey: StringLike }, InstanceObjectFromServer>(
        u => `/instances/${u.categoryId}/objects/${u.objectKey}`,
    ),
    /** @deprecated */
    getInstanceMorphism: GET<{ categoryId: StringLike, signature: StringLike }, InstanceMorphismFromServer>(
        u => `/instances/${u.categoryId}/morphisms/${u.signature}`,
    ),
    getInstanceCategory: GET<Empty, InstanceCategoryFromServer>(
        () => '/instances',
    ),
};

export default instances;
