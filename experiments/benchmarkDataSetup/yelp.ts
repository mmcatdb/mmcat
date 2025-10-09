import { Importer, SubCollection, generateWithinRange } from './helpers/Importer.ts'
import random from 'random'
import randomstring from 'randomstring'
import { RandomHelper } from './helpers/RandomHelper.ts'

random.use('helloworld') // seed for number generator (TODO somehow make random string generation seedable too)
const randomHelper = new RandomHelper(random)

const importer = new Importer('benchmark_yelp', parseFloat(process.argv[2] || '1.0'))

// # STEP 1: Create master entries (likely imperatively through this or other JS files)

// `random.shuffle` could be useful for remaking order of arrays for various independent distributions

const minDate = new Date(2020, 0, 1)
const maxDate = new Date(2021, 0, 1)

let idn = 1
const business = importer.generateRecords(10, () => ({
    business_id: (idn++).toString(),
    name: randomHelper.string(14),
    city: randomHelper.string(6),
    state: randomHelper.string(1),
    is_open: random.boolean(),
}))

const user = importer.generateRecords(100, () => ({
    user_id: (idn++).toString(),
    name: randomstring.generate({ length: 8, charset: 'alphabetic', capitalization: 'lowercase' }),
    yelping_since: randomHelper.date(minDate, maxDate).toISOString(),
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
    stars: random.choice(stars),
    date: randomHelper.date(minDate, maxDate).toISOString(),
    useful: generateWithinRange(() => Math.round(rcr()), 0, user.length),
    funny: generateWithinRange(() => Math.round(rcr()), 0, user.length),
    cool: generateWithinRange(() => Math.round(rcr()), 0, user.length),
    content: randomstring.generate(generateWithinRange(() => Math.round(rcr()), 50, 950)),
}))

// const rur2 = random.geometric(4 / user.length)
// const friendship = importer.generateRecords(400, () => {
//     const uid1 = generateWithinRange(rur2, 0, user.length-1)
//     let uid2 = uid1
//     while (uid1 == uid2) uid2 = generateWithinRange(rur2, 0, user.length-1)

//     return {
//         friendship_id: (idn++).toString(),
//         user_id_1: user[uid1].user_id,
//         user_id_2: user[uid2].user_id,
//     }
// }, ['user_id_1', 'user_id_2'])

// # STEP 2: Specify import of data

importer.importData({
    postgreSQL: [
        {
            name: 'business',
            schema: `
                business_id char(22) PRIMARY KEY,
                name text,
                city text,
                state text,
                is_open boolean
            `,
            data: business,
            structure: {
                business_id: true,
                name: true,
                city: true,
                state: true,
                is_open: true,
            }
        },
        {
            name: 'yelp_user',
            schema: `
                user_id char(22) PRIMARY KEY,
                name text,
                yelping_since timestamp
            `,
            data: user,
            structure: {
                user_id: true,
                name: true,
                yelping_since: true,
            }
        },
        {
            name: 'review',
            schema: `
                review_id char(22) PRIMARY KEY,
                user_id char(22) REFERENCES yelp_user (user_id) ON DELETE SET NULL ON UPDATE CASCADE,
                business_id char(22) REFERENCES business (business_id) ON DELETE SET NULL ON UPDATE CASCADE,
                stars char(3),
                date timestamp,
                useful integer,
                funny integer,
                cool integer,
                content text
            `,
            data: review,
            structure: {
                review_id: true,
                user_id: true,
                business_id: true,
                stars: true,
                date: true,
                useful: true,
                funny: true,
                cool: true,
                content: true,
            }
        },
        // {
        //     name: 'friendship',
        //     schema: `
        //         user_id_1 char(22) REFERENCES yelp_user (user_id) ON DELETE SET NULL ON UPDATE CASCADE,
        //         user_id_2 char(22) REFERENCES yelp_user (user_id) ON DELETE SET NULL ON UPDATE CASCADE,
        //         CONSTRAINT pk PRIMARY KEY (user_id_1, user_id_2)
        //     `,
        //     data: friendship,
        //     structure: {
        //         user_id_1: true,
        //         user_id_2: true,
        //     }
        // },
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
                is_open: true,
            }
        },
        {
            name: 'yelp_user',
            data: user,
            structure: {
                user_id: true,
                name: true,
                yelping_since: true
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
                date: true,
                useful: true,
                funny: true,
                cool: true,
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
                is_open: true,
            }
        },
        {
            name: 'YelpUser',
            data: user,
            structure: {
                user_id: true,
                name: true,
                yelping_since: true,
            }
        },
        {
            name: 'YELP_REVIEW',
            data: review,
            structure: {
                review_id: true,
                stars: true,
                date: true,
                useful: true,
                funny: true,
                cool: true,
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
