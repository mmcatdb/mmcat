export class Model {
    private constructor(
        public readonly jobId: number,
        public readonly jobLabel: string,
        public readonly commands: string
    ) {

    }

    static fromServer(input: ModelFromServer): Model {
        return new Model(input.jobId, input.jobLabel, input.commands);
    }
}

export type ModelFromServer = {
    jobId: number;
    jobLabel: string;
    commands: string;
}

export class ModelView {
    private constructor(
        public readonly jobId: number,
        public readonly jobLabel: string,
    ) {

    }

    static fromServer(input: ModelViewFromServer): ModelView {
        return new ModelView(input.jobId, input.jobLabel);
    }
}

export type ModelViewFromServer = {
    jobId: number;
    jobLabel: string;
}
