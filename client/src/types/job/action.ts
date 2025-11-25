import type { Entity, Id } from '../id';
import { type JobPayload, jobPayloadFromResponse, type JobPayloadInit, type JobPayloadResponse } from './payload';

export type ActionResponse = {
    id: Id;
    categoryId: Id;
    label: string;
    payloads: JobPayloadResponse[];
};

export class Action implements Entity {
    private constructor(
        public readonly id: Id,
        public readonly categoryId: Id,
        public readonly label: string,
        public readonly payloads: JobPayload[],
    ) {}

    static fromResponse(input: ActionResponse): Action {
        return new Action(
            input.id,
            input.categoryId,
            input.label,
            input.payloads.map(jobPayloadFromResponse),
        );
    }
}

export type ActionInit = {
    categoryId: Id;
    label: string;
    payloads: JobPayloadInit[];
};
