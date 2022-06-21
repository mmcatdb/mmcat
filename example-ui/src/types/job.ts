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
        public name: string,
        public type: string,
        public status: string
    ) {

    }

    static fromServer(input: JobFromServer): Job {
        return new Job(input.id, input.mappingId, input.name, input.type, input.status);
    }

    setStatus(status: string) {
        this.status = status;
    }
}

export type JobFromServer = {
    id: number;
    mappingId: number;
    name: string;
    type: string;
    status: string;
}
