import { type ActionPayload, actionPayloadFromServer, type ActionPayloadFromServer } from './action';
import type { Entity, Id } from './id';

export type JobFromServer = {
    id: Id;
    categoryId: Id;
    actionId: Id | null;
    label: string;
    state: JobState;
    payload: ActionPayloadFromServer;
    data: JobError | null;
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
        public readonly data: JobError | undefined,
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
            input.data ?? undefined,
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
    Finished = 'Finished',
    Canceled = 'Canceled',
    Failed = 'Failed',
}

type JobError = {
    name: string;
    data: unknown;
};
