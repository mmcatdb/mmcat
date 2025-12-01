import type { Entity, Id } from '../id';
import { type JobPayload, jobPayloadFromResponse, type JobPayloadInit, type JobPayloadResponse } from './payload';

export type ActionInfo = {
    id: Id;
    categoryId: Id;
    label: string;
};

export type ActionResponse = ActionInfo & {
    payloads: JobPayloadResponse[];
};

export class Action implements Entity {
    private constructor(
        readonly id: Id,
        readonly categoryId: Id,
        readonly label: string,
        readonly payloads: JobPayload[],
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
