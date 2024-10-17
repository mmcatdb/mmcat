import type { Id } from './id';

export type Workflow = {
    id: Id;
    categoryId: Id;
    label: string;
    state: WorkflowState;
    data: WorkflowData;
};

export enum WorkflowState {
    Running = 'Running',
    Finished = 'Finished',
    Canceled = 'Canceled',
    Failed = 'Failed',
}

export type WorkflowType = 'inference';

export type WorkflowData = {
    type: 'inference';
    datasourceIds?: Id[];
    jobId?: Id;
};

export type WorkflowInit = {
    label: string;
    type: WorkflowType;
};
