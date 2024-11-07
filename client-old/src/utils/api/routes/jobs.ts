import type { StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { JobFromServer, SessionFromServer } from '@/types/job';
import type { SaveJobResultPayload } from '@/types/inference/inferenceEdit';

const jobs = {
    getAllJobsInCategory: GET<{ categoryId: StringLike }, JobFromServer[]>(
        u => `/schema-categories/${u.categoryId}/jobs`,
    ),
    getJob: GET<{ id: StringLike }, JobFromServer>(
        u => `/jobs/${u.id}`,
    ),
    createRun: POST<{ actionId: StringLike }, JobFromServer>(
        u => `/actions/${u.actionId}/jobs`,
    ),
    createRestartedJob: POST<{ id: StringLike }, JobFromServer>(
        u => `/jobs/${u.id}/restart`,
    ),
    startJob: POST<{ id: StringLike }, JobFromServer>(
        u => `/jobs/${u.id}/start`,
    ),
    cancelJob: POST<{ id: StringLike }, JobFromServer>(
        u => `/jobs/${u.id}/cancel`,
    ),
    updateJobResult: POST<{ id: StringLike }, JobFromServer, SaveJobResultPayload>(
        u => `/jobs/${u.id}/updateResult`,
    ),
    getAllSessionsInCategory: GET<{ categoryId: StringLike }, SessionFromServer[]>(
        u => `/schema-categories/${u.categoryId}/sessions`,
    ),
    createSession: POST<{ categoryId: StringLike }, SessionFromServer>(
        u => `/schema-categories/${u.categoryId}/sessions`,
    ),
};

export default jobs;
