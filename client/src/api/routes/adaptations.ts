import type { StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import { type AdaptationResponse, type AdaptationJobResponse } from '@/components/adaptation/adaptation';

type AdaptationStartInput = {
    maxIterations: number;
    storageWeight: number;
    isRandomStart: boolean;
    seed: number | undefined;
};

export const adaptationsApi = {
    getAdaptationForCategory: GET<{ categoryId: StringLike }, AdaptationResponse | ''>(
        u => `/schema-categories/${u.categoryId}/adaptation`,
    ),
    createAdaptationForCategory: POST<{ categoryId: StringLike }, AdaptationResponse>(
        u => `/schema-categories/${u.categoryId}/adaptation`,
    ),
    startAdaptation: POST<{ adaptationId: StringLike }, AdaptationJobResponse, AdaptationStartInput>(
        u => `/adaptations/${u.adaptationId}/start`,
    ),
    pollAdaptation: GET<{ adaptationId: StringLike }, AdaptationJobResponse | null>(
        u => `/adaptations/${u.adaptationId}/poll`,
    ),
    stopAdaptation: POST<{ adaptationId: StringLike }, void>(
        u => `/adaptations/${u.adaptationId}/stop`,
    ),
};
