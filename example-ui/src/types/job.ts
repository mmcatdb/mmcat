import type { Entity, Id } from "./id";

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

export class Job implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly logicalModelId: Id,
        public readonly categoryId: Id,
        public readonly label: string,
        public readonly type: string,
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
    id: Id;
    logicalModelId: Id;
    categoryId: Id;
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
    logicalModelId: Id;
    label: string;
    type: JobType;
}
