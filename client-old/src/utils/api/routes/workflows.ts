import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST } from '../routeFunctions';
import type { Workflow, WorkflowInit } from '@/types/workflow';

const jobs = {
    getAllWorkflows: GET<Empty, Workflow[]>(
        () => `/workflows`,
    ),
    getWorkflow: GET<{ id: StringLike }, Workflow>(
        u => `/workflows/${u.id}`,
    ),
    createWorkflow: POST<Empty, Workflow, WorkflowInit>(
        () => `/workflows`,
    ),
};

export default jobs;
