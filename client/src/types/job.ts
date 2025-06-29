import { type JobPayload, jobPayloadFromResponse, type JobPayloadResponse, type JobPayloadInit } from './action';
import type { Entity, Id } from './id';
import type { SchemaCategoryInfo } from './schema';

type JobInfoResponse = {
    id: Id;
    label: string;
    createdAt: string;
    payload: JobPayloadInit;
    state: JobState;
};

export class JobInfo {
    constructor(
        public readonly id: Id,
        public readonly label: string,
        public readonly createdAt: Date,
        public readonly payload: JobPayload,
        public readonly state: JobState,
    ) {}

    static fromResponse(input: JobInfoResponse): JobInfo {
        return new JobInfo(
            input.id,
            input.label,
            new Date(input.createdAt),
            jobPayloadFromResponse(input.payload),
            input.state,
        );
    }
}

export type RunResponse = {
    id: Id;
    categoryId: Id;
    actionId?: Id;
    label: string;
    jobs: JobInfoResponse[];
};

export class Run implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly categoryId: Id,
        public readonly actionId: Id | undefined,
        public readonly label: string,
        public readonly jobs: JobInfo[],
    ) {}

    static fromResponse(input: RunResponse): Run {
        return new Run(
            input.id,
            input.categoryId,
            input.actionId,
            input.label,
            input.jobs.map(JobInfo.fromResponse),
        );
    }
}

export type JobResponse = Omit<JobInfoResponse, 'payload'> & {
    index: number;
    payload: JobPayloadResponse;
    data?: JobDataResponse;
    error?: JobError;
    runId: Id;
    categoryId: Id;
    runLabel: string;
    actionId: Id | null;
};

export class Job implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly index: number,
        public readonly label: string,
        public state: JobState,
        public readonly payload: JobPayload,
        public readonly data: JobData | undefined,
        public readonly error: JobError | undefined,
        public readonly createdAt: Date,
        public readonly runId: Id,
        public readonly categoryId: Id,
        public readonly runLabel: string,
        public readonly actionId: Id | undefined,
    ) {}

    static fromResponse(input: JobResponse, info: SchemaCategoryInfo): Job {
        return new Job(
            input.id,
            input.index,
            input.label,
            input.state,
            jobPayloadFromResponse(input.payload),
            input.data && jobDataFromResponse(input.data, info),
            input.error,
            new Date(input.createdAt),
            input.runId,
            input.categoryId,
            input.runLabel,
            input.actionId ?? undefined,
        );
    }

    setState(state: JobState) {
        this.state = state;
    }
}

export enum JobState {
    Disabled = 'Disabled',
    Ready = 'Ready',
    Running = 'Running',
    Waiting = 'Waiting',
    Finished = 'Finished',
    Failed = 'Failed',
}

type JobError = {
    name: string;
    data: unknown;
};

export enum JobDataType {
    Model = 'Model',
    Inference = 'Inference',
}

type JobDataResponse = ModelJobData;

type JobData = ModelJobData;

function jobDataFromResponse(input: JobDataResponse, info: SchemaCategoryInfo): JobData {
    console.log('Job data from server', info);
    switch (input.type) {
    case JobDataType.Model:
        return input;
    }
}

export type ModelJobData = {
    type: JobDataType.Model;
    value: string;
};

export type SessionResponse = {
    id: Id;
    categoryId: Id;
    createdAt: string;
};

export class Session implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly categoryId: Id,
        public readonly createdAt: Date,
    ) {}

    static fromResponse(input: SessionResponse): Session {
        return new Session(
            input.id,
            input.categoryId,
            new Date(input.createdAt),
        );
    }
}
