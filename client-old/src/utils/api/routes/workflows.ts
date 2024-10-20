import type { Empty, StringLike } from '@/types/api/routes';
import { GET, POST, PUT } from '../routeFunctions';
import type { Workflow, WorkflowData, WorkflowInit } from '@/types/workflow';

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
    updateWorkflowData: PUT<{ id: StringLike }, Workflow, WorkflowData>(
        u => `/workflows/${u.id}/data`,
    ),
    continueWorkflow: POST<{ id: StringLike }, Workflow>(
        u => `/workflows/${u.id}/continue`,
    ),
};

export default jobs;
