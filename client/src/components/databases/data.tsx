const columns = [
    { name: 'LABEL', uid: 'label' },
    { name: 'ID', uid: 'id' },
    { name: 'TYPE', uid: 'type' },
    { name: 'ACTIONS', uid: 'actions' },
];

const users = [
    {
        id: 1,
        label: 'PostgreSQL - Basic',
        type: 'postgreSQL',
        host: 'mmcat-postgresql',
        port: '5432',
        database: 'example_basic',
        actions: 'todo: delete this',
    },
    {
        id: 2,
        label: 'MongoDB - Basic',
        type: 'mongoDB',
        host: 'mmcat-mongodb',
        port: '27017',
        database: 'example_basic',
        actions: 'todo: delete this',
    },
];

export { columns, users };
