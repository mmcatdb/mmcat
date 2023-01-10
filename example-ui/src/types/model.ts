import type { Id } from "./id";

export class Model {
    private constructor(
        public readonly jobId: Id,
        public readonly jobLabel: string,
        public readonly commands: string
    ) {}

    static fromServer(input: ModelFromServer): Model {
        return new Model(input.jobId, input.jobLabel, input.commands);
    }
}

export type ModelFromServer = {
    jobId: Id;
    jobLabel: string;
    commands: string;
};

export class ModelView {
    private constructor(
        public readonly jobId: Id,
        public readonly jobLabel: string,
    ) {}

    static fromServer(input: ModelViewFromServer): ModelView {
        return new ModelView(input.jobId, input.jobLabel);
    }
}

export type ModelViewFromServer = {
    jobId: Id;
    jobLabel: string;
};
