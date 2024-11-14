import { type JobPayload, type JobPayloadFromServer, jobPayloadFromServer, type JobPayloadInit } from './action';
import type { Entity, Id } from './id';
import { InferenceJobData, type InferenceJobDataFromServer } from './inference/InferenceJobData';
import type { SchemaCategoryInfo } from './schema';

type JobInfoFromServer = {
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

    static fromServer(input: JobInfoFromServer): JobInfo {
        return new JobInfo(
            input.id,
            input.label,
            new Date(input.createdAt),
            jobPayloadFromServer(input.payload),
            input.state,
        );
    }
}

export type RunFromServer = {
    id: Id;
    categoryId: Id;
    actionId?: Id;
    label: string;
    jobs: JobInfoFromServer[];
};

export class Run implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly categoryId: Id,
        public readonly actionId: Id | undefined,
        public readonly label: string,
        public readonly jobs: JobInfo[],
    ) {}

    static fromServer(input: RunFromServer): Run {
        return new Run(
            input.id,
            input.categoryId,
            input.actionId,
            input.label,
            input.jobs.map(JobInfo.fromServer),
        );
    }
}

export type JobFromServer = Omit<JobInfoFromServer, 'payload'> & {
    index: number;
    payload: JobPayloadFromServer;
    data?: JobDataFromServer;
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

    static fromServer(input: JobFromServer, info: SchemaCategoryInfo): Job {
        return new Job(
            input.id,
            input.index,
            input.label,
            input.state,
            jobPayloadFromServer(input.payload),
            input.data && jobDataFromServer(input.data, info),
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

type JobDataFromServer = ModelJobData | InferenceJobDataFromServer;

type JobData = ModelJobData | InferenceJobData;

function jobDataFromServer(input: JobDataFromServer, info: SchemaCategoryInfo): JobData {
    switch (input.type) {
    case JobDataType.Model:
        return input;
    case JobDataType.Inference:
        return InferenceJobData.fromServer(input, info);
    }
}

export type ModelJobData = {
    type: JobDataType.Model;
    value: string;
};

export type SessionFromServer = {
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

    static fromServer(input: SessionFromServer): Session {
        return new Session(
            input.id,
            input.categoryId,
            new Date(input.createdAt),
        );
    }
}
