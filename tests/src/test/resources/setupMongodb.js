db.dropDatabase();

db.order.insertMany([
    {
        number: "o_100",
        address: {
            street: "hodnotaA",
            city: "hodnotaB",
            zip: "hodnotaC"
        },
        tags: [
            "123",
            "456",
            "789"
        ],
        note: {
            'en-US': {
                subject: "subject 1",
                content: "content en"
            },
            'cs-CZ': {
                subject: "subject 1",
                content: "content cz"
            },
        }
    },
    {
        number: "o_200",
        address: {
            street: "hodnotaA2",
            city: "hodnotaB2",
            zip: "hodnotaC2"
        },
        tags: [
            "123",
            "String456",
            "String789"
        ],
        note: {
            'cs-CZ': {
                subject: "subject cz",
                content: "content 1"
            },
            'en-GB': {
                subject: "subject gb",
                content: "content 2"
            }
        }
    }
]);

db.orderItem.insertMany([
    {
        number: "o_100",
        items: [
            {
                id: "123",
                label: "Clean Code",
                price: "125",
                quantity: "1"
            }
        ]
    },
    {
        number: "o_100",
        items: [
            {
                id: "765",
                label: "The Lord of the Rings",
                price: "199",
                quantity: "2"
            }
        ]
    },
    {
        number: "o_200",
        items: [
            {
                id: "457",
                label: "The Art of War",
                price: "299",
                quantity: "7"
            },
            {
                id: "734",
                label: "Animal Farm",
                price: "350",
                quantity: "3"
            }
        ]
    }
]);

db.orderItemEmpty.insertMany([
    {
        number: "o_100",
        items: null
    },
    {
        number: "o_200",
        items: []
    }
]);
