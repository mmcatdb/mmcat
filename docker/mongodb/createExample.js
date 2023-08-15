db.dropDatabase();

db.order.insertMany([
    {
        customer: {
            name: '1',
            number: '1'
        },
        contact: {
            email: 'alice@gmail.com'
        },
        items: [
            {
                id: 'B1',
                name: 'Pyramids',
                price: '200',
                quantity: '2'
            },
            {
                id: 'A7',
                name: 'Colosseum',
                price: '400',
                quantity: '1'
            }
        ]
    },
    {
        customer: {
            name: '1',
            number: '2'
        },
        contact: {
            cellphone: '+420123456789',
            email: 'alice@gmail.com'
        },
        items: [
            {
                id: 'B2',
                name: 'Sphynx',
                price: '100',
                quantity: '3'
            },
            {
                id: 'A8',
                name: 'Pantheon',
                price: '300',
                quantity: '2'
            }
        ]
    },
    {
        customer: {
            name: '2',
            number: '1'
        },
        contact: {
            email: 'bob@gmail.com'
        },
        items: [
            {
                id: 'B3',
                name: 'Luxor',
                price: '500',
                quantity: '1'
            }
        ]
    },
    {
        customer: {
            name: '2',
            number: '2'
        },
        contact: {
            facebook: 'bob.fb'
        },
        items: [
            {
                id: 'A6',
                name: 'Forum Romanum',
                price: '150',
                quantity: '1'
            },
            {
                id: 'A9',
                name: 'Spanish Steps',
                price: '300',
                quantity: '9'
            }
        ]
    },
]);
