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

export type Job = {
    id: number,
    mappingId: number,
    name: string,
    type: string,
    status: string
};
