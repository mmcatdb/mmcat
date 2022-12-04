export enum JobType {
    ModelToCategory = 'ModelToCategory',
    CategoryToModel = 'CategoryToModel'
}

export const JOB_TYPES = [
    {
        label: 'Model to Category',
        value: JobType.ModelToCategory
    },
    {
        label: 'Category to Model',
        value: JobType.CategoryToModel
    }
];

export class Job {
    private constructor(
        public id: number,
        public logicalModelId: number,
        public categoryId: number,
        public label: string,
        public type: string,
        public status: Status
    ) {

    }

    static fromServer(input: JobFromServer): Job {
        return new Job(input.id, input.logicalModelId, input.categoryId, input.label, input.type, input.status);
    }

    setStatus(status: Status) {
        this.status = status;
    }
}

export type JobFromServer = {
    id: number;
    logicalModelId: number;
    categoryId: number;
    label: string;
    type: JobType;
    status: Status;
}

export enum Status {
    Default = 'Default',
    Ready = 'Ready',
    Running = 'Running',
    Finished = 'Finished',
    Canceled = 'Canceled'
}

export type JobInit = {
    logicalModelId: number,
    label: string,
    type: JobType
}
