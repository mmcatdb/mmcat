import { type ActionPayload, actionPayloadFromServer, type ActionPayloadFromServer } from './action';
import type { Entity, Id } from './id';

export type JobFromServer = {
    id: Id;
    categoryId: Id;
    actionId: Id | null;
    label: string;
    state: JobState;
    payload: ActionPayloadFromServer;
    data: JobError | unknown;
    generatedDataModel: string | unknown;
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
        public readonly error: JobError | undefined,
        public readonly result: unknown | undefined,
        public readonly resultModel: unknown | undefined,
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
            input.state === JobState.Failed ? input.data as JobError : undefined,
            input.state === JobState.Finished || JobState.Waiting ? input.data : undefined,
            input.state === JobState.Finished ? input.generatedDataModel : undefined,
            new Date(input.createdAt),
        );
    }

    setState(state: JobState) {
        this.state = state;
    }

    withUpdatedResult(newResult: unknown): Job {
        return new Job(
            this.id,
            this.categoryId,
            this.actionId,
            this.label,
            this.state,
            this.payload,
            this.error,
            newResult,
            this.resultModel,
            this.createdAt
        );
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
