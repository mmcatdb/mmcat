import type { StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import { type AdaptationResponse } from '@/components/adaptation/adaptation';
import { type Job } from '@/types/job';

export const adaptationsApi = {
    getAdaptationForCategory: GET<{ categoryId: StringLike }, AdaptationResponse | ''>(
        u => `/schema-categories/${u.categoryId}/adaptation`,
    ),
    createAdaptationForCategory: POST<{ categoryId: StringLike }, AdaptationResponse>(
        u => `/schema-categories/${u.categoryId}/adaptation`,
    ),
    startAdaptation: POST<{ adaptationId: StringLike }, Job>(
        u => `/adaptations/${u.adaptationId}/start`,
    ),
};
