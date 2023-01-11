import { DataSource, type DataSourceFromServer } from "./dataSource";
import type { Entity, Id } from "./id";
import { LogicalModelInfo, type LogicalModelInfoFromServer } from "./logicalModel";

export enum JobType {
    ModelToCategory = 'ModelToCategory',
    CategoryToModel = 'CategoryToModel',
    JsonLdToCategory = 'JsonLdToCategory'
}

export const JOB_TYPES = [
    {
        label: 'Model to Category',
        value: JobType.ModelToCategory
    },
    {
        label: 'Category to Model',
        value: JobType.CategoryToModel
    },
    {
        label: 'Import data', // TODO
        value: JobType.JsonLdToCategory
    }
];

export class Job implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly categoryId: Id,
        public readonly logicalModel: LogicalModelInfo | undefined,
        public readonly dataSource: DataSource | undefined,
        public readonly label: string,
        public readonly type: JobType,
        public status: Status
    ) {}

    static fromServer(input: JobFromServer): Job {
        return new Job(
            input.id,
            input.categoryId,
            input.logicalModel ? LogicalModelInfo.fromServer(input.logicalModel) : undefined,
            input.dataSource ? DataSource.fromServer(input.dataSource) : undefined,
            input.label,
            input.type,
            input.status
        );
    }

    setStatus(status: Status) {
        this.status = status;
    }
}

export type JobFromServer = {
    id: Id;
    categoryId: Id;
    logicalModel?: LogicalModelInfoFromServer;
    dataSource?: DataSourceFromServer;
    label: string;
    type: JobType;
    status: Status;
};

export enum Status {
    Default = 'Default',
    Ready = 'Ready',
    Running = 'Running',
    Finished = 'Finished',
    Canceled = 'Canceled'
}

export type JobInit = {
    categoryId: Id;
    logicalModelId?: Id;
    dataSourceId?: Id;
    label: string;
    type: JobType;
};
