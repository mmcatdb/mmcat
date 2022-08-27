const totalBatches = 1024;
const batchSize = 1000;
const kindName = 'customer';
const outputFileName = 'initialUserData.js';

const data = require('./randomData');

function randomIntFromInterval(min, max) {
    return Math.floor(Math.random() * (max - min) + min);
}

function getRandomAddress() {
    return {
        street: randomIntFromInterval(1, 50) + ' ' + data.customerName[randomIntFromInterval(0, data.customerName.length)],
        city: data.word[randomIntFromInterval(0, data.word.length)],
    };
}

const fs = require('fs');
const outputFile = fs.createWriteStream(outputFileName);

outputFile.write(`db.dropDatabase();\n`);

let customerId = 1;
let orderId = 1;

for (let i = 1; i <= totalBatches; i++) {
    console.log('Batch: ' + i);
    outputFile.write(`db.${kindName}.insertMany([`);

    for (let j = 0; j < batchSize; j++) {
        const orderAmount = randomIntFromInterval(0, 10)
            + (Math.random < 0.2 ? randomIntFromInterval(5, 20) : 0)
            + (Math.random < 0.05 ? randomIntFromInterval(20, 50) : 0);

        const order = [ ...Array(orderAmount) ].map(_ => {
            const id = '#order_' + orderId;
            orderId++;
            return {
                id
            };
        });

        const customer = {
            _id: '#user_' + customerId,
            contact_address: getRandomAddress(),
            order
        };
        customerId++;

      outputFile.write(JSON.stringify(customer) + ',\n');
    }
    outputFile.write(']);\n')
}
console.log('Customer: ', customerId, ' Order: ', orderId);
outputFile.end();
