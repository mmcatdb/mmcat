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
        "t_123",
        "t_456",
        "t_789",
    ],
}, {
    number: "o_200",
    tags: [
        "t_123",
        "t_555",
        "t_888",
    ],
} ]);

db.orderItem.insertMany([ {
    number: "o_100",
    items: [
        { id: "p_123", label: "Clean Code", price: "125", quantity: "1" },
    ],
}, {
    number: "o_100",
    items: [
        { id: "p_765", label: "The Lord of the Rings", price: "199", quantity: "2" },
    ],
}, {
    number: "o_200",
    items: [
        { id: "p_457", label: "The Art of War", price: "299", quantity: "7" },
        { id: "p_734", label: "Animal Farm", price: "350", quantity: "3" },
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
        "en-US": { subject: "subject 1", content: "content en" },
        "cs-CZ": { subject: "subject 1", content: "content cz" },
    },
}, {
    number: "o_200",
    note: {
        "cs-CZ": { subject: "subject cz", content: "content 1" },
        "en-GB": { subject: "subject gb", content: "content 2" },
    },
} ]);


db.hardcore.insertMany([ {
    id: "h_1",
    a: [ [
        [ "v_a-00-0", "v_a-00-1" ],
        [ "v_a-01-0", "v_a-01-1" ],
    ], [
        [ "v_a-10-0", "v_a-10-1" ],
        [ "v_a-11-0", "v_a-11-1" ],
    ] ],
    b: [ [
        [ "v_b-00-0", "v_b-00-1" ],
        [ "v_b-01-0", "v_b-01-1" ],
    ], [
        [ "v_b-10-0", "v_b-10-1" ],
        [ "v_b-11-0", "v_b-11-1" ],
    ] ],
    array: [],
}, {
    id: "h_2",
    array: [ {
        id: "c_0",
        x: { i: "v_0-x-i", j: "v_0-x-j" },
        y: { i: "v_0-y-i", j: "v_0-y-j" },
    }, {
        id: "c_1",
        x: { i: "v_1-x-i", j: "v_1-x-j" },
        y: { i: "v_1-y-i", j: "v_1-y-j" },
    } ],
} ]);
