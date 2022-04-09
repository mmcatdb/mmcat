db.dropDatabase();

db.order.insertMany([
    {
        number: "2043",
        array: [ 123, 456, 789 ],
        nested: {
            propertyA: "hodnotaA",
            propertyB: "hodnotaB",
            propertyC: "hodnotaC"
        },
        contact: {
            email: "anna@seznam.cz",
            cellphone: "+420777123456"
        },
        items: [
            {
                product: {
                    id: 123
                }

            },
            {
                product: {
                    id: 765
                }

            }
        ]
    },
    {
        number: "1653",
        array: [ "123", "String456", "String789" ],
        nested: {
            propertyA: "hodnotaA2",
            propertyB: "hodnotaB2",
            propertyC: "hodnotaC2"
        },
        contact: {
            skype: "skype123",
            cellphone: "+420123456789"
        },
        items: [
            {
                product: {
                    id: 457
                }

            },
            {
                product: {
                    id: 734
                }

            }
        ]
    }
]);

db.items.insertMany([
    {
        id: 123,
        name: "Toy",
        price: 125,
        quantity: 1
    },
    {
        id: 765,
        name: "Book",
        price: 199,
        quantity: 2
    },
    {
        id: 457,
        name: "Knife",
        price: 299,
        quantity: 7
    },
    {
        id: 734,
        name: "Doll",
        price: 350,
        quantity: 3
    }
])