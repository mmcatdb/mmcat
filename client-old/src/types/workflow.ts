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
    step: InferenceWorkflowStep;
    inputDatasourceId?: Id;
    allDatasourceIds: Id[];
    inferenceJobId?: Id;
    // Mappings are already accessible via the category.
    mtcActionIds: Id[];
};

export type InferenceWorkflowStep = typeof inferenceWorkflowSteps[number];
export const inferenceWorkflowSteps = [
    'addDatasources',
    'editCategory',
    'addMappings',
    'setOutput',
    'finish',
];

