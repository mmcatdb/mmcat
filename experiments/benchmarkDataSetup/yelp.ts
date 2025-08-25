#!/usr/bin/env node

import { SubCollection, Importer, generateWithinRange } from './generator.ts'
import random from 'random'
import randomstring from 'randomstring'

random.use('helloworld')

const importer = new Importer('benchmark_yelp', parseFloat(process.argv[2] || '1.0'))

// # STEP 1: Create master entries (likely imperatively through this or other JS files)

// `random.shuffle` could be useful for remaking order of arrays for various independent distributions

let idn = 1
const business = importer.generateRecords(100, () => ({
    business_id: (idn++).toString(), // TODO: unique string, somehow...
    name: randomstring.generate({ length: 8, charset: 'alphabetic', capitalization: 'lowercase' }),
    state: randomstring.generate({ length: 1, charset: 'alphabetic', capitalization: 'uppercase' }),
}))

const user = importer.generateRecords(1000, () => ({
    user_id: (idn++).toString(),
    name: randomstring.generate({ length: 8, charset: 'alphabetic', capitalization: 'lowercase' }),
}))

const rbr = random.geometric(1 / (business.length))
const rur = random.geometric(1 / 1000)
const rrr = random.normal(500, 150)
const review = importer.generateRecords(3000, () => ({
    review_id: (idn++).toString(),
    business: business[generateWithinRange(
        rbr, 0, business.length-1
    )].business_id,
    user: user[generateWithinRange(
        rur, 0, user.length-1
    )].user_id,
    content: randomstring.generate(generateWithinRange(() => Math.round(rrr()), 50, 950)),
}))

// # STEP 2: Specify import of data

importer.importData({
    postgreSQL: [
        {
            name: "business",
            schema: `
                "business_id" CHAR(16) PRIMARY KEY,
                "name" TEXT,
                "city" TEXT,
                "state" TEXT,
                "stars" INTEGER,
                "review_count" INTEGER,
                "is_open" INTEGER
            `,
            data: [], // TODO
            structure: {
                business_id: true,
                name: true,
                city: true,
                state: true,
                stars: true,
                review_count: true,
                is_open: true,
            }
        },
    ],
    mongoDB: [
        {
            name: "user",
            data: [],
            structure: {
                user_id: true,
                personal: {
                    name: true,
                    address: true,
                },

                // friends: new SubCollection(
                //     Join(Join(friendship, "user_a", user, "user_id"), "user_b", user, ""),
                //     {
                //         user_id: true,
                //         name: true
                //     }
                // ),

                // friends: Join([
                //     { collection: "friendship", where: "user_1", is: "user_id" },
                //     { collection: "user", where: "user_id", is: "user_2" },
                //     // start with [ user_id ], then, join by join, flat map it to the next collection and set current collection to the joined one
                //     // you could even somehow allow referring to previous attributes, like `previous("business")`
                // ]),
                // NOTE: Joins are difficult since they require aliasing (e.g. when joining two users together), so I decided to avoid them for now

                // OR (this is probably the best one)
                friends: new SubCollection(
                    record => findByKey([], "user_a", record.user_id).map(record2 => user.getById(record2.user_b)),
                    {
                        user_id: true,
                        name: true
                    }
                )
                },
        },
    ],
    neo4j: [
        {
            name: "YelpUser",
            data: [], // TODO
            structure: {
                user_id: true,
                name: true,
            },
        },
        {
            name: "YELP_FRIENDSHIP",
            data: [], // TODO
            structure: {},
            from: {
                label: "YelpUser",
                match: { user_id: "user_a" },
            },
            to: {
                label: "YelpUser",
                match: { user_id: "user_b" },
            },
        },
    ],
})
