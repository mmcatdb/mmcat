const totalBatches = 64;
const batchSize = 1000;
const kindName = 'order';
const outputFilename = 'setupMongodb.js';

const data = require('./randomData');

const customerAmount = data.contact[data.contactType[0]].length;
const customers = [];

for (let i = 0; i < customerAmount; i++) {
    const contacts = [];
    data.contactType.forEach(type => {
        contacts.push({
            type,
            value: data.contact[type][i]
        });
    });

    customers.push({
        id: i + 1,
        contacts,
        number: 1
    });
}

const items = [];

for (let i = 0; i < data.item.id.length; i++) {
    items.push({
        id: data.item.id[i],
        price: data.item.price[i],
        name: data.item.name[i]
    });
}

function randomIntFromInterval(min, max) {
    return Math.floor(Math.random() * (max - min) + min);
}

function getRandomCustomer() {
    const index = randomIntFromInterval(0, customers.length);
    return customers[index];
}

function customizedContacts(customer) {
    const output = {};
    const contacts = customer.contacts.filter(_ => Math.random() < 0.5);
    if (contacts.length === 0)
        contacts.push(customer.contacts[randomIntFromInterval(0, 4)]);

    contacts.forEach(contact => output[contact.type] = contact.value);

    return output;
}

function getRandomCustomizedItem() {
    const index = randomIntFromInterval(0, items.length);
    return {
        ...items[index],
        quantity: randomIntFromInterval(1, 21)
    };
}

const fs = require('fs');
const outputFile = fs.createWriteStream(outputFilename);

outputFile.write(`db.dropDatabase();\n`);

for (let i = 1; i <= totalBatches; i++) {
    console.log('Batch: ' + i);
    outputFile.write(`db.${kindName}.insertMany([`);

    for (let j = 0; j < batchSize; j++) {
        const customer = getRandomCustomer();
        const items = [ ...Array(randomIntFromInterval(1, 6)) ]
            .map(_ => getRandomCustomizedItem())

        const order = {
            _id: {
                customer: customer.id,
                number: customer.number
            },
            contact: customizedContacts(customer),
            items
        }

        customer.number++;
        outputFile.write(JSON.stringify(order) + ',\n');
    }
    outputFile.write(']);\n')
}

outputFile.end();
