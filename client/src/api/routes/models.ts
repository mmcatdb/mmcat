import type { StringLike } from '@/types/api/routes';
import { GET } from '../routeFunctions';
import type { ModelFromServer, ModelViewFromServer } from '@/types/model';

const models = {
    /** @deprecated */
    getAllModelsInCategory: GET<{ categoryId: StringLike }, ModelViewFromServer[]>(
        u => `/schema-categories/${u.categoryId}/models`,
    ),
    /** @deprecated */
    getModel: GET<{ jobId: StringLike }, ModelFromServer>(
        u => `/models/${u.jobId}`,
    ),
};

export default models;
