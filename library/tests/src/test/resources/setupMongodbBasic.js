db.dropDatabase();

db.order.insertMany([ {
    number: "o_100",
}, {
    number: "o_200",
} ]);

db.address.insertMany([ {
    number: "o_100",
    address: {
        street: "Ke Karlovu 2027/3",
        city: "Praha 2",
        zip: "121 16",
    },
}, {
    number: "o_200",
    address: {
        street: "Malostranské nám. 2/25",
        city: "Praha 1",
        zip: "118 00",
    },
} ]);

db.addressMissingSimple.insertMany([ {
    number: "o_100",
    address: {
        street: "Ke Karlovu 2027/3",
        city: null,
        zip: "121 16",
    },
}, {
    number: "o_200",
    address: {
        street: "Malostranské nám. 2/25",
        zip: "118 00",
    },
} ]);

db.addressMissingComplex.insertMany([ {
    number: "o_100",
    address: null,
}, {
    number: "o_200",
} ]);

db.tag.insertMany([ {
    number: "o_100",
    tags: [
        "123",
        "456",
        "789",
    ],
}, {
    number: "o_200",
    tags: [
        "123",
        "String456",
        "String789",
    ],
} ]);

db.orderItem.insertMany([ {
    number: "o_100",
    items: [
        { id: "123", label: "Clean Code", price: "125", quantity: "1" },
    ],
}, {
    number: "o_100",
    items: [
        { id: "765", label: "The Lord of the Rings", price: "199", quantity: "2" },
    ],
}, {
    number: "o_200",
    items: [
        { id: "457", label: "The Art of War", price: "299", quantity: "7" },
        { id: "734", label: "Animal Farm", price: "350", quantity: "3" },
    ],
} ]);

db.orderItemEmpty.insertMany([ {
    number: "o_100",
    items: null,
}, {
    number: "o_200",
    items: [],
} ]);

db.contact.insertMany([ {
    number: "o_100",
    contact: {
        "phone": "123456789",
        "email": "alice@mmcatdb.com",
    },
}, {
    number: "o_200",
    contact: {
        "email": "bob@mmcatdb.com",
        "github": "https://github.com/mmcatdb",
    },
} ]);

db.customer.insertMany([ {
    customer: {
        name: "Alice",
        number: "o_100",
    },
}, {
    customer: {
        name: "Bob",
        number: "o_200",
    },
} ]);

db.note.insertMany([ {
    number: "o_100",
    note: {
        'en-US': { subject: "subject 1", content: "content en" },
        'cs-CZ': { subject: "subject 1", content: "content cz" },
    },
}, {
    number: "o_200",
    note: {
        'cs-CZ': { subject: "subject cz", content: "content 1" },
        'en-GB': { subject: "subject gb", content: "content 2" },
    },
} ]);
