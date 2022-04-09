db.dropDatabase();

db.basic.insertMany([
    {
        number: "2043"
    },
    {
        number: "1653"
    }
]);

db.structure.insertMany([
    {
        number: "2043",
        nested: {
            propertyA: "hodnotaA",
            propertyB: "hodnotaB",
            propertyC: "hodnotaC"
        }
    },
    {
        number: "1653",
        nested: {
            propertyA: "hodnotaA2",
            propertyB: "hodnotaB2",
            propertyC: "hodnotaC2"
        }
    }
]);

db.simple_array.insertMany([
    {
        number: "2043",
        array: [
            "123",
            "456",
            "789"
        ]
    },
    {
        number: "1653",
        array: [
            "123",
            "String456",
            "String789"
        ]
    }
]);

db.complex_array.insertMany([
    {
        number: "2043",
        items: [
            {
                productId: "123",
                name: "Toy",
                price: "125",
                quantity: "1"
            },
            {
                productId: "765",
                name: "Book",
                price: "199",
                quantity: "2"
            }
        ]
    },
    {
        number: "2043", // There should be only one item with number 2043, but this is much easier to test.
        items: [
            {
                productId: "457",
                name: "Knife",
                price: "299",
                quantity: "7"
            },
            {
                productId: "734",
                name: "Doll",
                price: "350",
                quantity: "3"
            }
        ]
    }
]);

db.empty_array.insertMany([
    {
        number: "2043",
        items: null
    },
    {
        number: "2043",
        items: []
    }
]);

db.complex_map.insertMany([ // The order of address elements differ from the original test file because of the tests by string comparison.
    {
        number: "2043",
        address: {
            country: {
                text: "Czech republic",
                locale: "en"
            },
            city: {
                text: "Praha",
                locale: "cs"
            }
        }
    },
    {
        number: "1653",
        address: {
            country: {
                text: "Česká republika",
                locale: "cs"
            },
            location: {
                text: "Praha",
                locale: "cs"
            }
        }
    }
]);