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
    step: 'selectInputs';
    inputDatasourceIds: Id[];
} | {
    step: 'editCategory';
    inputDatasourceIds: Id[];
    inferenceJobId: Id;
} | {
    step: 'addMappings' | 'finish';
    inputDatasourceIds: Id[];
    inferenceJobId: Id;
    inputMappingIds: Id[];
});

export type InferenceWorkflowStep = typeof inferenceWorkflowSteps[number];
export const inferenceWorkflowSteps = [
    'selectInputs',
    'editCategory',
    'addMappings',
    'finish',
] as const;

