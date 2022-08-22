const totalBatches = 1024;
//const totalBatches = 2;
const batchSize = 1000;
//const batchSize = 2;
const outputFileName = 'setupPostgresql.sql';
//const maxCustomers = 3;
//const maxItems = 3;

const minDate = new Date('2019-01-01');
const maxDate = new Date();

const data = require('./randomData');

const customerAmount = typeof maxCustomers !== 'undefined' ? maxCustomers : data.contact[data.contactType[0]].length;
const customers = [];

for (let i = 0; i < customerAmount; i++) {
    const contacts = [];
    let address = '';
    data.contactType.forEach(type => {
        contacts.push({
            type,
            value: data.contact[type][i]
        });

        if (type === 'address')
            address = data.contact[type][i];
    });

    customers.push({
        id: i + 1,
        address,
        contacts: removeRandomContacts(contacts),
        number: 1,
        name: data.customerName[i]
    });
}

const items = [];

const itemAmount = typeof maxItems !== 'undefined' ? maxItems : data.item.id.length;

for (let i = 0; i < itemAmount; i++) {
    items.push({
        id: data.item.id[i],
        price: data.item.price[i],
        name: data.item.name[i]
    });
}

function randomIntFromInterval(min, max) {
    return Math.floor(Math.random() * (max - min) + min);
}

const maxMilliseconds = maxDate.getTime();
const minMilliseconds = minDate.getTime();
const diffMilliseconds = maxMilliseconds - minMilliseconds;

function randomDate() {
    return new Date(Math.random() * diffMilliseconds + minMilliseconds).toISOString()
}

function randomNote() {
    if (Math.random < 0.8)
        return '';
    let size = randomIntFromInterval(5, 20);
    if (Math.random() < 0.1)
        size += randomIntFromInterval(30, 100);
    return [ ...Array(size) ].map(_ => randomIntFromInterval(0, data.word.length)).map(index => data.word[index]).join(' ');
}

function getRandomCustomer() {
    const index = randomIntFromInterval(0, customers.length);
    return customers[index];
}

function removeRandomContacts(contacts) {
    const output = {};
    const newContacts = contacts.filter(_ => Math.random() < 0.5);
    if (newContacts.length === 0)
        newContacts.push(contacts[randomIntFromInterval(0, 4)]);

    return newContacts;
}

function getRandomCustomizedItem() {
    const index = randomIntFromInterval(0, items.length);
    const quantity = randomIntFromInterval(1, 21);
    return {
        ...items[index],
        quantity,
        totalPrice: quantity * items[index].price * (Math.random < 0.1 ? 0.9 : 1)
    };
}

function getRandomItems() {
    const orderItems = [ ...Array(randomIntFromInterval(1, 6)) ]
        .map(_ => getRandomCustomizedItem())

    const ids = []
    return orderItems.filter(item => {
        if (ids.includes(item.id))
            return false;
        
        ids.push(item.id);
        return true;
    });
}

const fs = require('fs');
const outputFile = fs.createWriteStream(outputFileName);

outputFile.write('INSERT INTO app_customer (id, full_name) VALUES\n');

for (let i = 0; i < customers.length; i++) {
    outputFile.write(`('${customers[i].id}', '${customers[i].name}')`);
    outputFile.write(i === customers.length - 1 ? ';\n\n' : ',\n');
}

outputFile.write('INSERT INTO app_contact (id, type, value) VALUES\n');

let contactId = 1;
for (let i = 0; i < customers.length; i++) {
    const contacts = customers[i].contacts;
    for (let j = 0; j < contacts.length; j++) {
        contacts[j].id = contactId;
        contactId++;
        outputFile.write(`('${contacts[j].id}', '${contacts[j].type}', '${contacts[j].value}')`);
        outputFile.write(i === customers.length - 1 && j === contacts.length - 1 ? ';\n\n' : ',\n');
    }
}

outputFile.write('INSERT INTO app_customer_contact (customer_id, contact_id) VALUES\n');

for (let i = 0; i < customers.length; i++) {
    const contacts = customers[i].contacts;
    for (let j = 0; j < contacts.length; j++) {
        outputFile.write(`('${customers[i].id}', '${contacts[j].id}')`);
        outputFile.write(i === customers.length - 1 && j === contacts.length - 1 ? ';\n\n' : ',\n');
    }
}

outputFile.write('INSERT INTO app_product (id, name, price) VALUES\n');

for (let i = 0; i < items.length; i++) {
    outputFile.write(`('${items[i].id}', '${items[i].name}', '${items[i].price}')`);
    outputFile.write(i === items.length - 1 ? ';\n\n' : ',\n');
}

function getRandomOrder(id) {
    const customer = getRandomCustomer();

    return {
        id,
        customerId: customer.id,
        created: randomDate(),
        paid: Math.random() < 0.9 ? randomDate() : '',
        sent: Math.random() < 0.7 ? randomDate() : '',
        address: customer.address,
        note: randomNote()
    };
}

let orderId = 1;

for (let i = 1; i <= totalBatches; i++) {
    console.log('Batch: ' + i);
    outputFile.write('INSERT INTO app_order (id, customer_id, created, sent, paid, note, delivery_address) VALUES\n');

    for (let j = 0; j < batchSize; j++) {
        const order = getRandomOrder(orderId);
        orderId++;
        outputFile.write(`('${order.id}', '${order.customerId}', '${order.created}', '${order.sent}', '${order.paid}', '${order.note}', '${order.address}')`);
        outputFile.write(j === batchSize - 1 ? ';\n\n' : ',\n');
    }

    outputFile.write('INSERT INTO app_order_item (order_id, product_id, amount, total_price) VALUES\n');

    for (let j = 0; j < batchSize; j++) {
        const orderItems = getRandomItems();

        for (let k = 0; k < orderItems.length; k++) {
            outputFile.write(`('${orderId - batchSize + j}', '${orderItems[k].id}', '${orderItems[k].quantity}', '${orderItems[k].totalPrice}')`);
            outputFile.write(j === batchSize - 1 && k === orderItems.length - 1 ? ';\n\n' : ',\n');
        }
    }
}

outputFile.end();
