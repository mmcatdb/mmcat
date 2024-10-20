db.dropDatabase();

db.orders.insertMany([ {
    _id: '2023001',
    customer: {
        id: '1',
        name: 'Mary',
        surname: 'Smith',
        knows: [
            { id: '2', name: 'John', surname: 'Newlin' },
            { id: '3', name: 'Anne', surname: 'Maxwell' },
        ],
    },
    street: 'Ke Karlovu',
    city: 'Prague',
    postCode: '110 00',
    items: [
        { pid: 'P5', title: 'Sourcery', quantity: '1', price: '350', currentPrice: '350' },
        { pid: 'P7', title: 'Pyramids', quantity: '1', price: '250', currentPrice: '275' },
    ],
}, {
    _id: '2023002',
    customer: { id: '2', name: 'John', surname: 'Newlin' },
    street: 'Technická', city: 'Prague', postCode: '162 00',
    items: [
        { pid: 'P7', title: 'Pyramids', quantity: '1', price: '275', currentPrice: '275' },
    ],
} ]);

db.order.insertMany([ {
    _id: '2023001',
    customer: { id: '1', name: 'Mary', surname: 'Smith' },
    street: 'Ke Karlovu',
    city: 'Prague',
    postCode: '110 00',
    items: [
        { pid: 'P5', title: 'Sourcery', quantity: '1', price: '350' },
        { pid: 'P7', title: 'Pyramids', quantity: '1', price: '250' },
    ],
}, {
    _id: '2023002',
    customer: { id: '2', name: 'John', surname: 'Newlin' },
    street: 'Technická',
    city: 'Prague',
    postCode: '162 00',
    items: [
        { pid: 'P7', title: 'Pyramids', quantity: '1', price: '275' },
    ],
} ]);
