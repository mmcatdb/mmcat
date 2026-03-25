import type { StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import { type AdaptationResponse, type AdaptationJobResponse } from '@/components/adaptation/adaptation';

export const adaptationsApi = {
    getAdaptationForCategory: GET<{ categoryId: StringLike }, AdaptationResponse | ''>(
        u => `/schema-categories/${u.categoryId}/adaptation`,
    ),
    createAdaptationForCategory: POST<{ categoryId: StringLike }, AdaptationResponse>(
        u => `/schema-categories/${u.categoryId}/adaptation`,
    ),
    startAdaptation: POST<{ adaptationId: StringLike }, AdaptationJobResponse>(
        u => `/adaptations/${u.adaptationId}/start`,
    ),
    pollAdaptation: GET<{ adaptationId: StringLike }, AdaptationJobResponse | null>(
        u => `/adaptations/${u.adaptationId}/poll`,
    ),
    stopAdaptation: POST<{ adaptationId: StringLike }, void>(
        u => `/adaptations/${u.adaptationId}/stop`,
    ),
};
