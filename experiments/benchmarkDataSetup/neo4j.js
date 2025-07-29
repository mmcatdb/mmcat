#!/usr/bin/env node

import { createReadStream } from 'fs'
import { stderr } from 'process'
import { createInterface } from 'readline'

const users = new Set()

// TODO: Batch importing, possibly with UNWIND

const initString = `
MATCH (a:YelpBusiness)-[r]->() DELETE a, r;
MATCH (a:YelpUser)-[r]->() DELETE a, r;
MATCH (a:YelpBusiness) DELETE a;
MATCH (a:YelpUser) DELETE a;
`

/** @param {string} str */
function sanitize(str) {
    return str.replaceAll("'", "\\'")
}

/** @param {string} line */
function business(line) {
    const jsonObject = JSON.parse(line)
    return `{ business_id: '${jsonObject.business_id}', name: '${sanitize(jsonObject.name)}', city: '${sanitize(jsonObject.city)}', state: '${jsonObject.state}', stars: ${jsonObject.stars}, review_count: ${jsonObject.review_count}, is_open: ${jsonObject.is_open} }`
}

/** @param {string} line */
function user(line) {
    const jsonObject = JSON.parse(line)

    users.add(jsonObject.user_id)

    return `{ user_id: '${jsonObject.user_id}', name: '${sanitize(jsonObject.name)}', review_count: ${jsonObject.review_count}, yelping_since: '${jsonObject.yelping_since}', useful: ${jsonObject.useful}, funny: ${jsonObject.funny}, cool: ${jsonObject.cool} }`
}

/** @param {string} line */
function review(line) {
    const jsonObject = JSON.parse(line)

    if (users.has(jsonObject.user_id)) {
        return `{ user_id: '${jsonObject.user_id}', business_id: '${jsonObject.business_id}', review_id: '${jsonObject.review_id}', stars: ${jsonObject.stars}, date: '${jsonObject.date}', useful: ${jsonObject.useful}, funny: ${jsonObject.funny}, cool: ${jsonObject.cool} }`
    }
    else {
        // return `('${jsonObject.review_id}',NULL,'${jsonObject.business_id}',${jsonObject.stars},'${jsonObject.date}',${jsonObject.useful},${jsonObject.funny},${jsonObject.cool})`
        return ''
    }
}

/** @param {string} line */
function friendship(line) {
    const jsonObject = JSON.parse(line)
    const friends = jsonObject.friends.split(', ')

    if (friends.length == 1 && friends[0].length == 0) return ''

    return friends.filter(fid => users.has(fid)).map(fid =>
        `{ user_id: '${jsonObject.user_id}', friend_id: '${fid}' }`
    ).join(',')
}

const businessFile = process.argv[2]
const userFile = process.argv[3]
const reviewFile = process.argv[4]
const friendshipFile = userFile


async function writeToTableFromFile(table, filename) {
    let fn = null
    let creationStr = null
    if (table == 'YelpBusiness') {
        fn = business
        creationStr = `] AS jsonObject CREATE (:YelpBusiness { business_id: jsonObject.business_id, name: jsonObject.name, city: jsonObject.city, state: jsonObject.state, stars: jsonObject.stars, review_count: jsonObject.review_count, is_open: jsonObject.is_open });\n`

    }
    if (table == 'YelpUser') {
        fn = user
        creationStr = `] AS jsonObject CREATE (:YelpUser { user_id: jsonObject.user_id, name: jsonObject.name, review_count: jsonObject.review_count, yelping_since: jsonObject.yelping_since, useful: jsonObject.useful, funny: jsonObject.funny, cool: jsonObject.cool });\n`
    }
    if (table == 'YELP_FRIENDSHIP') {
        fn = friendship
        creationStr = `] AS jsonObject MATCH (a:YelpUser { user_id: jsonObject.user_id}), (b:YelpUser { user_id: jsonObject.friend_id}) CREATE (a)-[:YELP_FRIENDSHIP]->(b);\n`
    }
    if (table == 'YELP_REVIEW') {
        fn = review
        creationStr = `] AS jsonObject MATCH (a:YelpUser { user_id: jsonObject.user_id}), (b:YelpBusiness { business_id: jsonObject.business_id}) CREATE (a)-[:YELP_REVIEW { review_id: jsonObject.review_id, stars: jsonObject.stars, date: jsonObject.date, useful: jsonObject.useful, funny: jsonObject.funny, cool: jsonObject.cool }]->(b);\n`
    }

    const fileStream = createReadStream(filename);

    const rl = createInterface({
        input: fileStream,
        crlfDelay: Infinity
    });

    const MAX_ROWS = 1000
    let buffer = []

    const clearBuffer = async () => {
        process.stdout.write(`UNWIND [`)

        let first = true

        for (const row of buffer) {
            if (!first) process.stdout.write(',')
            process.stdout.write(row)
            first = false
        }

        if (!process.stdout.write(creationStr)) {
            const p = new Promise((resolve, reject) => {
                process.stdout.once('drain', resolve)
            });

            await p
        }

        buffer = []
    }

    for await (const line of rl) {
        const row = fn(line)
        if (row == '') continue
        if (buffer.length == MAX_ROWS) {
            await clearBuffer()
        }
        buffer.push(row)
    }

    await clearBuffer()

    rl.close()
}

console.log(initString)

await writeToTableFromFile('YelpBusiness', businessFile)
await writeToTableFromFile('YelpUser', userFile)
await writeToTableFromFile('YELP_REVIEW', reviewFile)
await writeToTableFromFile('YELP_FRIENDSHIP', friendshipFile)
