export const JOB_TYPES = [
    {
        label: 'Model to Category',
        value: 'ModelToCategory'
    },
    {
        label: 'Category to Model',
        value: 'CategoryToModel'
    }
];

export class Job {
    private constructor(
        public id: number,
        public mappingId: number,
        public schemaId: number,
        public name: string,
        public type: string,
        public status: Status
    ) {

    }

    static fromServer(input: JobFromServer): Job {
        return new Job(input.id, input.mappingId, input.schemaId, input.name, input.type, input.status);
    }

    setStatus(status: Status) {
        this.status = status;
    }
}

export type JobFromServer = {
    id: number;
    mappingId: number;
    schemaId: number;
    name: string;
    type: string;
    status: Status;
}

export enum Status {
    Default = 'Default',
    Ready = 'Ready',
    Running = 'Running',
    Finished = 'Finished',
    Canceled = 'Canceled'
}
