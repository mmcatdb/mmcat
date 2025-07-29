#!/usr/bin/env node

import { createReadStream } from 'fs'
import { stderr } from 'process'
import { createInterface } from 'readline'

const users = new Set()

// FIXME: proper datatypes (not just text, because that messes up cost estimation)

const initString = `
DROP TABLE IF EXISTS "friendship";
DROP TABLE IF EXISTS "review";
DROP TABLE IF EXISTS "business";
DROP TABLE IF EXISTS "yelp_user";

CREATE TABLE "business" (
    "business_id" CHAR(22) PRIMARY KEY,
    "name" TEXT,
    "city" TEXT,
    "state" TEXT,
    "stars" INTEGER,
    "review_count" INTEGER,
    "is_open" INTEGER
);

CREATE TABLE "yelp_user" (
    "user_id" CHAR(22) PRIMARY KEY,
    "name" TEXT,
    "review_count" INTEGER,
    "yelping_since" TEXT,
    "useful" INTEGER,
    "funny" INTEGER,
    "cool" INTEGER
);

CREATE TABLE "friendship" (
    "user_id" CHAR(22),
    "friend_id" CHAR(22),
    PRIMARY KEY ("user_id", "friend_id"),
    CONSTRAINT fk_uid FOREIGN KEY ("user_id") REFERENCES "yelp_user" ("user_id") ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_fid FOREIGN KEY ("friend_id") REFERENCES "yelp_user" ("user_id") ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE "review" (
    "review_id" CHAR(22) PRIMARY KEY,
    "user_id" CHAR(22),
    "business_id" CHAR(22),
    "stars" INTEGER,
    "date" TEXT,
    "useful" INTEGER,
    "funny" INTEGER,
    "cool" INTEGER,
    CONSTRAINT fk_uid FOREIGN KEY ("user_id") REFERENCES "yelp_user" ("user_id") ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_bid FOREIGN KEY ("business_id") REFERENCES "business" ("business_id") ON DELETE SET NULL ON UPDATE CASCADE
);
`

/** @param {string} str */
function sanitize(str) {
    return str.replaceAll("'", "''")
}

/** @param {string} line */
function business(line) {
    const jsonObject = JSON.parse(line)
    return `('${jsonObject.business_id}','${sanitize(jsonObject.name)}','${sanitize(jsonObject.city)}','${jsonObject.state}',${jsonObject.stars},${jsonObject.review_count},${jsonObject.is_open})`
}

/** @param {string} line */
function yelp_user(line) {
    const jsonObject = JSON.parse(line)

    users.add(jsonObject.user_id)

    return `('${jsonObject.user_id}','${sanitize(jsonObject.name)}',${jsonObject.review_count},'${jsonObject.yelping_since}',${jsonObject.useful},${jsonObject.funny},${jsonObject.cool})`
}

/** @param {string} line */
function review(line) {
    const jsonObject = JSON.parse(line)

    if (users.has(jsonObject.user_id)) {
        return `('${jsonObject.review_id}','${jsonObject.user_id}','${jsonObject.business_id}',${jsonObject.stars},'${jsonObject.date}',${jsonObject.useful},${jsonObject.funny},${jsonObject.cool})`
    } else {
        return `('${jsonObject.review_id}',NULL,'${jsonObject.business_id}',${jsonObject.stars},'${jsonObject.date}',${jsonObject.useful},${jsonObject.funny},${jsonObject.cool})`
    }
}

/** @param {string} line */
function friendship(line) {
    const jsonObject = JSON.parse(line)
    const friends = jsonObject.friends.split(', ')

    if (friends.length == 1 && friends[0].length == 0) return ''

    return friends.filter(fid => users.has(fid)).map(fid => `('${fid}','${jsonObject.user_id}')`).join(',')
}

const businessFile = process.argv[2]
const userFile = process.argv[3]
const reviewFile = process.argv[4]
const friendshipFile = userFile


async function writeToTableFromFile(table, filename) {
    let fn = null
    if (table == 'business') fn = business
    if (table == 'yelp_user') fn = yelp_user
    if (table == 'friendship') fn = friendship
    if (table == 'review') fn = review

    const fileStream = createReadStream(filename);

    const rl = createInterface({
        input: fileStream,
        crlfDelay: Infinity
    });

    const MAX_ROWS = 100000
    let buffer = []

    const clearBuffer = async () => {
        process.stdout.write(`INSERT INTO "${table}" VALUES `)

        let first = true

        for (const row of buffer) {
            if (!first) process.stdout.write(',')
            process.stdout.write(row)
            first = false
        }

        if (!process.stdout.write(';\n')) {
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

await writeToTableFromFile('business', businessFile)
await writeToTableFromFile('yelp_user', userFile)
await writeToTableFromFile('review', reviewFile)
// await writeToTableFromFile('friendship', friendFile)

// FIXME: loading takes too long, probably due to integrity constraints; this could be sped up using
// https://stackoverflow.com/questions/38112379/disable-postgresql-foreign-key-checks-for-migrations
// but for that the user needs admin privileges
// deferring does not do much, maybe if it was also parallelized
// So far I have commented it out until some query using the friendship relation is needed
