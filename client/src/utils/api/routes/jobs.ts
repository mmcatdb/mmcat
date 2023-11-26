import type { StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { JobFromServer } from '@/types/job';

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
    cancelJob: POST<{ id: StringLike }, JobFromServer>(
        u => `/jobs/${u.id}/cancel`,
    ),
};

export default jobs;
