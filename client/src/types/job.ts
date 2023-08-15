import { DataSource, type DataSourceFromServer } from './dataSource';
import type { Entity, Id } from './id';
import { LogicalModelInfo, type LogicalModelInfoFromServer } from './logicalModel';

export enum JobType {
    ModelToCategory = 'ModelToCategory',
    CategoryToModel = 'CategoryToModel',
    JsonLdToCategory = 'JsonLdToCategory'
}

export const JOB_TYPES = [
    {
        label: 'Model to Category',
        value: JobType.ModelToCategory,
    },
    {
        label: 'Category to Model',
        value: JobType.CategoryToModel,
    },
    {
        label: 'Import data', // TODO
        value: JobType.JsonLdToCategory,
    },
];

export class Job implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly categoryId: Id,
        public readonly label: string,
        public state: JobState,
        public readonly payload: JobPayload,
        public readonly data?: JobError,
    ) {}

    static fromServer(input: JobFromServer): Job {
        return new Job(
            input.id,
            input.categoryId,
            input.label,
            input.state,
            jobPayloadFromServer(input.payload),
            input.data ?? undefined,
        );
    }

    setState(state: JobState) {
        this.state = state;
    }
}

export type JobFromServer = {
    id: Id;
    categoryId: Id;
    label: string;
    state: JobState;
    payload: JobPayloadFromServer;
    data: JobError | null;
};

export enum JobState {
    Default = 'Default',
    Ready = 'Ready',
    Running = 'Running',
    Finished = 'Finished',
    Canceled = 'Canceled',
    Failed = 'Failed',
}

type JobError = {
    name: string;
    data: unknown;
};

interface JobPayloadType<TType extends JobType = JobType> {
    readonly type: TType;
}

type JobPayload =
    | ModelToCategoryPayload
    | CategoryToModelPayload
    | JsonLdToCategoryPayload;

type JobPayloadFromServer<T extends JobType = JobType> = {
    type: T;
};

export function jobPayloadFromServer(input: JobPayloadFromServer): JobPayload {
    switch (input.type) {
    case JobType.ModelToCategory:
        return ModelToCategoryPayload.fromServer(input as ModelToCategoryPayloadFromServer);
    case JobType.CategoryToModel:
        return CategoryToModelPayload.fromServer(input as CategoryToModelPayloadFromServer);
    case JobType.JsonLdToCategory:
        return JsonLdToCategoryPayload.fromServer(input as JsonLdToCategoryPayloadFromServer);
    }
}

type ModelToCategoryPayloadFromServer = JobPayloadFromServer<JobType.ModelToCategory> & {
    logicalModel: LogicalModelInfoFromServer;
};

class ModelToCategoryPayload implements JobPayloadType<JobType.ModelToCategory> {
    readonly type = JobType.ModelToCategory;

    private constructor(
        readonly logicalModel: LogicalModelInfo,
    ) {}

    static fromServer(input: ModelToCategoryPayloadFromServer): ModelToCategoryPayload {
        return new ModelToCategoryPayload(
            LogicalModelInfo.fromServer(input.logicalModel),
        );
    }
}

type CategoryToModelPayloadFromServer = JobPayloadFromServer<JobType.CategoryToModel> & {
    logicalModel: LogicalModelInfoFromServer;
};

class CategoryToModelPayload implements JobPayloadType<JobType.CategoryToModel> {
    readonly type = JobType.CategoryToModel;

    private constructor(
        readonly logicalModel: LogicalModelInfo,
    ) {}

    static fromServer(input: CategoryToModelPayloadFromServer): CategoryToModelPayload {
        return new CategoryToModelPayload(
            LogicalModelInfo.fromServer(input.logicalModel),
        );
    }
}

type JsonLdToCategoryPayloadFromServer = JobPayloadFromServer<JobType.JsonLdToCategory> & {
    dataSource: DataSourceFromServer;
};

class JsonLdToCategoryPayload implements JobPayloadType<JobType.JsonLdToCategory> {
    readonly type = JobType.JsonLdToCategory;

    private constructor(
        readonly dataSource: DataSource,
    ) {}

    static fromServer(input: JsonLdToCategoryPayloadFromServer): JsonLdToCategoryPayloadFromServer {
        return new JsonLdToCategoryPayload(
            DataSource.fromServer(input.dataSource),
        );
    }
}

export type JobInit = {
    categoryId: Id;
    label: string;
    payload: JobPayloadInit;
};

export type JobPayloadInit = {
    type: JobType.ModelToCategory | JobType.CategoryToModel;
    logicalModelId: Id;
} | {
    type: JobType.JsonLdToCategory;
    dataSourceId: Id;
};
