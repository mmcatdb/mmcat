export class Model {
    private constructor(
        public jobId: number,
        public jobName: string,
        public commands: string
    ) {

    }

    static fromServer(input: ModelFromServer): Model {
        return new Model(input.jobId, input.jobName, input.commands);
    }
}

export type ModelFromServer = {
    jobId: number;
    jobName: string;
    commands: string;
}

export class ModelView {
    private constructor(
        public jobId: number,
        public jobName: string,
    ) {

    }

    static fromServer(input: ModelViewFromServer): ModelView {
        return new ModelView(input.jobId, input.jobName);
    }
}

export type ModelViewFromServer = {
    jobId: number;
    jobName: string;
}
