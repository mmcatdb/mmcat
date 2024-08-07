import { type ActionPayload, actionPayloadFromServer, type ActionPayloadFromServer } from './action';
import type { Entity, Id } from './id';
import { InferenceJobData, type InferenceJobDataFromServer } from './inference/InferenceJobData';

export type JobFromServer = {
    id: Id;
    categoryId: Id;
    actionId: Id | null;
    label: string;
    state: JobState;
    payload: ActionPayloadFromServer;
    data?: JobDataFromServer;
    error?: JobError;
    createdAt: string;
};

export class Job implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly categoryId: Id,
        public readonly actionId: Id | undefined,
        public readonly label: string,
        public state: JobState,
        public readonly payload: ActionPayload,
        public readonly data: JobData | undefined,
        public readonly error: JobError | undefined,
        public readonly createdAt: Date,
    ) {}

    static fromServer(input: JobFromServer): Job {
        return new Job(
            input.id,
            input.categoryId,
            input.actionId ?? undefined,
            input.label,
            input.state,
            actionPayloadFromServer(input.payload),
            input.data && jobDataFromServer(input.data),
            input.error,
            new Date(input.createdAt),
        );
    }

    setState(state: JobState) {
        this.state = state;
    }
}

export enum JobState {
    Paused = 'Paused',
    Ready = 'Ready',
    Running = 'Running',
    Waiting = 'Waiting',
    Finished = 'Finished',
    Canceled = 'Canceled',
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

function jobDataFromServer(input: JobDataFromServer): JobData {
    switch (input.type) {
    case JobDataType.Model:
        return input;
    case JobDataType.Inference:
        return InferenceJobData.fromServer(input);
    }
}

export type ModelJobData = {
    type: JobDataType.Model;
    model: string;
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
