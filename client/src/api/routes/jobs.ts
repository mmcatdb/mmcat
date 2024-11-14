import type { StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { JobFromServer, RunFromServer, SessionFromServer } from '@/types/job';

const jobs = {
    getAllJobsInCategory: GET<{ categoryId: StringLike }, JobFromServer[]>(
        u => `/schema-categories/${u.categoryId}/jobs`,
    ),
    getJob: GET<{ id: StringLike }, JobFromServer>(
        u => `/jobs/${u.id}`,
    ),
    createRun: POST<{ actionId: StringLike }, RunFromServer>(
        u => `/actions/${u.actionId}/jobs`,
    ),
    createRestartedJob: POST<{ id: StringLike }, JobFromServer>(
        u => `/jobs/${u.id}/restart`,
    ),
    enableJob: POST<{ id: StringLike }, JobFromServer>(
        u => `/jobs/${u.id}/enable`,
    ),
    disableJob: POST<{ id: StringLike }, JobFromServer>(
        u => `/jobs/${u.id}/disable`,
    ),
    saveJobResult: POST<{ id: StringLike }, JobFromServer>(
        u => `/jobs/${u.id}/update-result`,
    ),
    getAllSessionsInCategory: GET<{ categoryId: StringLike }, SessionFromServer[]>(
        u => `/schema-categories/${u.categoryId}/sessions`,
    ),
    createSession: POST<{ categoryId: StringLike }, SessionFromServer>(
        u => `/schema-categories/${u.categoryId}/sessions`,
    ),
};

export default jobs;
