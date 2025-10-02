import type { StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { JobResponse, RunResponse, SessionResponse } from '@/types/job';

export const jobsApi = {
    getAllJobsInCategory: GET<{ categoryId: StringLike }, JobResponse[]>(
        u => `/schema-categories/${u.categoryId}/jobs`,
    ),
    getJob: GET<{ id: StringLike }, JobResponse>(
        u => `/jobs/${u.id}`,
    ),
    createRun: POST<{ actionId: StringLike }, RunResponse>(
        u => `/actions/${u.actionId}/jobs`,
    ),
    createRestartedJob: POST<{ id: StringLike }, JobResponse>(
        u => `/jobs/${u.id}/restart`,
    ),
    enableJob: POST<{ id: StringLike }, JobResponse>(
        u => `/jobs/${u.id}/enable`,
    ),
    disableJob: POST<{ id: StringLike }, JobResponse>(
        u => `/jobs/${u.id}/disable`,
    ),
    saveJobResult: POST<{ id: StringLike }, JobResponse>(
        u => `/jobs/${u.id}/update-result`,
    ),
    getAllSessionsInCategory: GET<{ categoryId: StringLike }, SessionResponse[]>(
        u => `/schema-categories/${u.categoryId}/sessions`,
    ),
    createSession: POST<{ categoryId: StringLike }, SessionResponse>(
        u => `/schema-categories/${u.categoryId}/sessions`,
    ),
};
