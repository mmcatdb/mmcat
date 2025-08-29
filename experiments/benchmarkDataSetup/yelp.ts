import { Importer, generateWithinRange } from './generator.ts'
import random from 'random'
import randomstring from 'randomstring'

random.use('helloworld') // seed for number generator (TODO somehow make random string generation seedable too)

const importer = new Importer('benchmark_yelp', parseFloat(process.argv[2] || '1.0'))

// # STEP 1: Create master entries (likely imperatively through this or other JS files)

// `random.shuffle` could be useful for remaking order of arrays for various independent distributions

let idn = 1
const business = importer.generateRecords(10, () => ({
    business_id: (idn++).toString(),
    name: randomstring.generate({ length: 8, charset: 'alphabetic', capitalization: 'lowercase' }),
    city: randomstring.generate({ length: 2, charset: 'alphabetic', capitalization: 'uppercase' }),
    state: randomstring.generate({ length: 1, charset: 'alphabetic', capitalization: 'uppercase' }),
}))

const user_length = importer.scalingFactor * 100
const ufr = random.pareto(1.2)
const user = importer.generateRecords(100, () => ({
    user_id: (idn++).toString(),
    name: randomstring.generate({ length: 8, charset: 'alphabetic', capitalization: 'lowercase' }),
    fans: generateWithinRange(() => Math.round(ufr()), 1, user_length) - 1
}))

const rbr = random.geometric(3 / business.length)
const rur = random.geometric(10 / user.length)
const rcr = random.normal(500, 150)
const stars = [ 0.0, 0.5, 1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0 ]
const review = importer.generateRecords(300, () => ({
    review_id: (idn++).toString(),
    business_id: business[generateWithinRange(
        rbr, 0, business.length-1
    )].business_id,
    user_id: user[generateWithinRange(
        rur, 0, user.length-1
    )].user_id,
    stars: stars[random.int(0, stars.length - 1)],
    content: randomstring.generate(generateWithinRange(() => Math.round(rcr()), 50, 950)),
}))

// # STEP 2: Specify import of data

importer.importData({
    postgreSQL: [
        {
            name: 'business',
            schema: `
                business_id char(22) PRIMARY KEY,
                name text,
                city text,
                state text
            `,
            data: business,
            structure: {
                business_id: true,
                name: true,
                city: true,
                state: true,
            }
        },
        {
            name: 'yelp_user',
            schema: `
                user_id char(22) PRIMARY KEY,
                name text,
                fans integer
            `,
            data: user,
            structure: {
                user_id: true,
                name: true,
                fans: true,
            }
        },
        {
            name: 'review',
            schema: `
                review_id char(22) PRIMARY KEY,
                user_id char(22) REFERENCES yelp_user (user_id) ON DELETE SET NULL ON UPDATE CASCADE,
                business_id char(22) REFERENCES business (business_id) ON DELETE SET NULL ON UPDATE CASCADE,
                stars char(3),
                content text
            `,
            data: review,
            structure: {
                review_id: true,
                user_id: true,
                business_id: true,
                stars: true,
                content: true,
            }
        },
    ],
    mongoDB: [
        {
            name: 'business',
            data: business,
            structure: {
                business_id: true,
                name: true,
                city: true,
                state: true,
            }
        },
        {
            name: 'user',
            data: user,
            structure: {
                user_id: true,
                name: true,
                fans: true,
            }
        },
        {
            name: 'review',
            data: review,
            structure: {
                review_id: true,
                user_id: true,
                business_id: true,
                stars: true,
                content: true,
            }
        },

        // Example nested implementation...
        // {
        //     name: "user",
        //     data: [],
        //     structure: {
        //         user_id: true,
        //         personal: {
        //             name: true,
        //             address: true,
        //         },
        //         friends: new SubCollection(
        //             record => importer.findRecordByKey(friendship, "user_a", record.user_id)
        //                 // .map(record2 => importer.findRecordByKey(user, "user_id", record2.user_b)),
        //             {
        //                 user_id: true,
        //                 name: true
        //             }
        //         )
        //     },
        // },
    ],
    neo4j: [
        {
            name: 'YelpBusiness',
            data: business,
            structure: {
                business_id: true,
                name: true,
                city: true,
                state: true,
            }
        },
        {
            name: 'YelpUser',
            data: user,
            structure: {
                user_id: true,
                name: true,
                fans: true,
            }
        },
        {
            name: 'YELP_REVIEW',
            data: review,
            structure: {
                review_id: true,
                stars: true,
                content: true,
            },
            from: {
                label: 'YelpUser',
                match: { user_id: 'user_id' },
            },
            to: {
                label: 'YelpBusiness',
                match: { business_id: 'business_id' },
            },
        },
    ],
})
