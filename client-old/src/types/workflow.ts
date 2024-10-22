import type { Id } from './id';

export type Workflow = {
    id: Id;
    categoryId: Id;
    label: string;
    jobId?: Id;
    data: WorkflowData;
};

export type WorkflowType = 'inference';

export type WorkflowInit = {
    label: string;
    type: WorkflowType;
};

export type WorkflowData = {
    type: 'inference';
} & ({
    step: 'selectInput';
    inputDatasourceId?: Id;
} | {
    step: 'editCategory';
    inputDatasourceId: Id;
    inferenceJobId: Id;
} | {
    step: 'addMappings' | 'selectOutputs' | 'finish';
    inputDatasourceId: Id;
    inferenceJobId: Id;
    mtcActionIds: Id[];
});

export type InferenceWorkflowStep = typeof inferenceWorkflowSteps[number];
export const inferenceWorkflowSteps = [
    'selectInput',
    'editCategory',
    'addMappings',
    'selectOutputs',
    'finish',
] as const;

