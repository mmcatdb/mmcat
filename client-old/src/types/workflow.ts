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
    inputDatasource?: Id;
    allDatasources?: Id[];
    inferenceJob?: Id;
    datasourceIds?: Id[];
    // Mappings are already accessible via the category.
    mtcJobs?: Id[];
};

export type InferenceWorkflowStep = typeof inferenceWorkflowSteps[number];
export const inferenceWorkflowSteps = [
    'addDatasources',
    'editCategory',
    'addMappings',
    'setOutput',
    'finish',
];

