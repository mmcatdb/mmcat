import { Importer, SubCollection, generateWithinRange } from './helpers/Importer.ts'
import random from 'random'
import { RandomHelper } from './helpers/RandomHelper.ts'
import { faker } from '@faker-js/faker';

random.use(123)
faker.seed(123)

const randomHelper = new RandomHelper(random)
const importer = new Importer('benchmark_caldotcom', parseFloat(process.argv[2] || '1.0'))

// # STEP 1: Create master entries (likely imperatively through this or other JS files)

// `random.shuffle` could be useful for remaking order of arrays for various independent distributions

const date1 = new Date(2020, 0, 1)
const date2 = new Date(date1.getTime() + 1000 * 60 * 60 * 24 * 366)

let idn = 1

// datetime format = YYYY-MM-DD hh:mm:ss +-h:mm

const membershipRoleGen = () => random.choice(['MEMBER', 'ADMIN', 'OWNER'])!

const team = importer.generateRecords(10, previous => ({
    id: (idn++).toString(),
    name: faker.company.name(),
    parentId: randomHelper.nullable(0.8, () => random.choice(previous).id),
}))

const role = importer.generateRecords(40, () => ({
    id: (idn++).toString(),
    name: faker.commerce.department(),
    description: faker.company.catchPhraseDescriptor(),
    teamId: randomHelper.record(team).id,
}))

const attribute = importer.generateRecords(40, () => ({
    id: (idn++).toString(),
    name: faker.commerce.productAdjective(),
    teamId: randomHelper.record(team).id,
}))

const attributeOption = attribute.map(attr => ({ // for now booleans are OK
    id: (idn++).toString(),
    value: "false",
    attributeId: attr.id
})).concat(attribute.map(attr => ({
    id: (idn++).toString(),
    value: "true",
    attributeId: attr.id
})))



const user = importer.generateRecords(100, () => {
    return {
        id: (idn++).toString(),
        name: faker.person.fullName(),
        username: faker.internet.username(),
    }
}, ['username'])

const membership = importer.removeDuplicateRecords(
team.map(t => ({
    id: (idn++).toString(),
    userId: random.choice(user).id,
    teamId: t.id,
    accepted: true,
    role: 'OWNER',
    customRoleId: null,
})).concat(importer.generateRecords(150, () => {
    const teamId = randomHelper.record(team, randomHelper.geometricFromZero(1 / team.length)).id
    return {
        id: (idn++).toString(),
        userId: random.choice(user).id,
        teamId,
        accepted: random.boolean(),
        role: membershipRoleGen(),
        customRoleId: random.choice(importer.findRecordByKey(role, 'teamId', teamId))?.id ?? null
    }
})), ['userId', 'teamId'])

const teamOrgScope = importer.generateRecords(25, () => {
    const t = random.choice(team)
    return {
        userId: random.choice(importer.findRecordByKey(membership, 'teamId', t.id)).userId,
        teamId: t.id,
    }
}, ['userId', 'teamId'])

let attributeToUser = importer.generateRecords(250, () => {
    const attr = random.choice(attribute)
    const member = random.choice(importer.findRecordByKey(membership, 'teamId', attr.teamId))
    const attrId = attr.id
    const attrOptionId = Number.parseInt(attrId) + attribute.length * (random.boolean() ? 2 : 1)
    return {
        memberId: member.id,
        attributeOptionId: attrOptionId.toString(),
    }
}, ['memberId', 'attributeOptionId'])



let team2 = random.shuffle(team)

const verifiedEmail = importer.generateRecords(300, () => {
    const usr = random.choice(user)
    const members = importer.findRecordByKey(membership, 'userId', usr.id)
    return {
        id: (idn++).toString(),
        value: faker.internet.email(),
        userId: usr.id,
        teamId: random.choice(members)?.teamId ?? null
    }
})

const schedule = importer.generateRecords(250, () => {
    return {
        id: (idn++).toString(),
        name: faker.internet.email(),
        userId: random.choice(user).id,
    }
})

const eventType = importer.generateRecords(200, () => {
    const teamId = randomHelper.record(team2, randomHelper.geometricFromZero(1 / team2.length)).id
    const members = importer.findRecordByKey(membership, 'teamId', teamId)
    const owner = random.choice(members)
    const sched = random.choice(importer.findRecordByKey(schedule, 'userId', owner.userId))
    return {
        id: (idn++).toString(),
        title: faker.commerce.product(),
        description: faker.commerce.productDescription(),
        teamId,
        ownerId: owner.userId,
        scheduleId: sched?.id ?? null,
    }
})
// ensure event type parent and owner is not from unrelated team
for (const et of eventType) {
    if (!et.parentId) {
        const teamId = et.teamId
        const eventTypes = importer.findRecordByKey(eventType, 'teamId', teamId)

        for (let i = 0; i < eventTypes.length; i++) {
            eventTypes[i].parentId = randomHelper.nullable(0.5, () => eventTypes[random.int(0, i - 1)].id)
        }
    }
}

const availability = importer.generateRecords(1000, () => {
    const sched = randomHelper.record(schedule)
    const eventT = importer.findRecordByKey(eventType, 'scheduleId', sched.id)
    let d1 = randomHelper.date(date1, date2)
    let d2 = randomHelper.date(date1, date2)
    if (d1 > d2) [d1, d2] = [d2, d1]
    return {
        id: (idn++).toString(),
        start: d1.toISOString(),
        end: d2.toISOString(),
        userId: sched.userId,
        eventTypeId: random.choice(eventT)?.id ?? null,
        scheduleId: sched.id,
    }
})

const outOfOffice = importer.generateRecords(1000, () => {
    const u1 = randomHelper.record(user)
    let u2 = randomHelper.record(user)
    while (u1 === u2) u2 = randomHelper.record(user)
    let d1 = randomHelper.date(date1, date2)
    let d2 = randomHelper.date(date1, date2)
    if (d1 > d2) [d1, d2] = [d2, d1]
    return {
        id: (idn++).toString(),
        start: d1.toISOString(),
        end: d2.toISOString(),
        userId: u1.id,
        toUserId: u2.id,
    }
})



const userOnEventType = importer.generateRecords(300, () => {
    const et = randomHelper.record(eventType)
    return {
        eventTypeId: et.id,
        userId: random.choice(importer.findRecordByKey(membership, 'teamId', et.teamId)).userId,
    }
}, ['userId', 'eventTypeId'])

const hostGroup = importer.generateRecords(15, () => {
    const et = randomHelper.record(eventType)
    return {
        id: (idn++).toString(),
        eventTypeId: et.id, // <- probs should match for all its members

        teamId: et.teamId, // auxiliary for generation, not in the schema
    }
})

const eventHost = importer.removeDuplicateRecords(
    importer.generateRecords(450, () => {
        // No group
        const et = randomHelper.record(eventType)
        const member = random.choice(importer.findRecordByKey(membership, 'teamId', et.teamId))
        const sched = random.choice(importer.findRecordByKey(schedule, 'userId', member.userId))
        return {
            id: (idn++).toString(),
            userId: member.userId,
            memberId: member.id,
            eventTypeId: et.id,
            scheduleId: sched?.id ?? null,
            hostGroupId: null,
        }
    }).concat(importer.generateRecords(150, () => {
        // In group
        const hGroup = random.choice(hostGroup)
        const member = random.choice(importer.findRecordByKey(membership, 'teamId', hGroup.teamId))
        return {
            id: (idn++).toString(),
            userId: member.userId,
            memberId: member.id,
            eventTypeId: hGroup.eventTypeId,
            scheduleId: null,
            hostGroupId: hGroup.id,
        }
    })),
    ['eventTypeId', 'userId']
)



const feature = importer.generateUnscaledRecords(8, () => ({
    id: (idn++).toString(),
    name: faker.company.buzzVerb(),
}))

const userFeatures = importer.generateRecords(150, () => ({
    userId: random.choice(user).id,
    featureId: random.choice(feature).id,
}), ['userId', 'featureId'])

const teamFeatures = importer.generateRecords(25, () => ({
    teamId: random.choice(team).id,
    featureId: random.choice(feature).id,
}), ['teamId', 'featureId'])



const workflow = importer.generateRecords(120, () => {
    const member = randomHelper.record(membership, randomHelper.geometricFromZero(5 / membership.length))

    return {
        id: (idn++).toString(),
        name: faker.company.buzzAdjective() + ' ' + faker.company.buzzNoun(),
        userId: member.userId,
        teamId: member.teamId
    }
})

const workflowStepNumberToString = (n: number) => n.toString().padStart(3, "0")
const workflowStepCountGen = () => generateWithinRange(random.geometric(1 / 5), 1, 10)
const workflowStep = workflow.map(wf => {
    const numberReal = workflowStepCountGen()
    return {
        id: (idn++).toString(),
        workflowId: wf.id,
        numberReal,
        number: workflowStepNumberToString(numberReal),
        action: faker.company.buzzVerb(),
    }
})
for (let i = 0; i < workflow.length; i++) {
    const step = workflowStep[i]
    for (let stepNumber = step.numberReal - 1; stepNumber >= 1; stepNumber--) {
        workflowStep.push({
            id: (idn++).toString(),
            workflowId: step.workflowId,
            numberReal: stepNumber,
            number: workflowStepNumberToString(stepNumber),
            action: faker.company.buzzNoun(),
        })
    }
}

const workflowsOnEventTypes = importer.generateRecords(300, () => {
    const wf = randomHelper.record(workflow)
    const et = random.choice(importer.findRecordByKey(eventType, 'teamId', wf.teamId))
    if (!et) return null
    return {
        workflowId: wf.id,
        eventTypeId: et.id,
    }
}, ['workflowId', 'eventTypeId'])

const workflowsOnTeams = importer.generateRecords(180, () => ({
    workflowId: randomHelper.record(workflow).id,
    teamId: randomHelper.record(team).id,
}), ['workflowId', 'teamId'])



const booking = importer.generateRecords(800, () => {
    const teamId = randomHelper.record(team2, randomHelper.geometricFromZero(1 / team2.length)).id
    const eventTypes = importer.findRecordByKey(eventType, 'teamId', teamId)
    const users = importer.findRecordByKey(membership, 'teamId', teamId)
    return {
        id: (idn++).toString(),
        title: faker.commerce.product(),
        description: faker.commerce.productDescription(),
        time: randomHelper.date(date1, date2).toISOString(),
        userId: randomHelper.record(users, randomHelper.geometricFromZero(Math.min(5 / users.length, 0.6))).userId,
        eventTypeId: randomHelper.record(eventTypes).id,
    }
})

const attendee = importer.generateRecords(5000, () => ({
    id: (idn++).toString(),
    email: faker.internet.email(),
    bookingId: randomHelper.record(booking, randomHelper.geometricFromZero(3 / booking.length)).id
}))

importer.importData({
    /*
    postgreSQL: [
        {
            name: 'team',
            schema: `
                id text PRIMARY KEY,
                name text,
                parentId text REFERENCES team(id)
            `,
            data: team,
            structure: {
                id: true,
                name: true,
                parentId: true
            }
        },
        {
            name: 'role',
            schema: `
                id text PRIMARY KEY,
                name text,
                description text,
                teamId text REFERENCES team(id)
            `,
            data: role,
            structure: {
                id: true,
                name: true,
                description: true,
                teamId: true,
            }
        },
        {
            name: 'attribute',
            schema: `
                id text PRIMARY KEY,
                name text,
                teamId text REFERENCES team(id)
            `,
            data: attribute,
            structure: {
                id: true,
                name: true,
                teamId: true,
            }
        },
        {
            name: 'attributeOption',
            schema: `
                id text PRIMARY KEY,
                value text,
                attributeId text REFERENCES attribute(id)
            `,
            data: attributeOption,
            structure: {
                id: true,
                value: true,
                attributeId: true,
            }
        },



        {
            name: 'caldotcom_user',
            schema: `
                id text PRIMARY KEY,
                username text UNIQUE NOT NULL,
                name text
            `,
            data: user,
            structure: {
                id: true,
                username: true,
                name: true
            }
        },
        {
            name: 'membership',
            schema: `
                id text PRIMARY KEY,
                userId text REFERENCES caldotcom_user(id),
                teamId text REFERENCES team(id),
                accepted text,
                role text,
                customRoleId text REFERENCES role(id)
            `,
            data: membership,
            structure: {
                id: true,
                userId: true,
                teamId: true,
                accepted: true,
                role: true,
                customRoleId: true,
            }
        },
        {
            name: 'teamOrgScope',
            schema: `
                userId text REFERENCES caldotcom_user(id),
                teamId text REFERENCES team(id),
                CONSTRAINT teamOrgScope_pk PRIMARY KEY (userId, teamId)
            `,
            data: teamOrgScope,
            structure: {
                userId: true,
                teamId: true,
            }
        },
        {
            name: 'attributeToUser',
            schema: `
                attributeOptionId text REFERENCES attributeOption(id),
                memberId text REFERENCES membership(id),
                CONSTRAINT attributeToUser_pk PRIMARY KEY (attributeOptionId, memberId)
            `,
            data: attributeToUser,
            structure: {
                attributeOptionId: true,
                memberId: true,
            }
        },



        {
            name: 'verifiedEmail',
            schema: `
                id text PRIMARY KEY,
                value text,
                userId text REFERENCES caldotcom_user(id),
                teamId text REFERENCES team(id)
            `,
            data: verifiedEmail,
            structure: {
                id: true,
                value: true,
                userId: true,
                teamId: true,
            }
        },
        {
            name: 'schedule',
            schema: `
                id text PRIMARY KEY,
                name text,
                userId text REFERENCES caldotcom_user(id)
            `,
            data: schedule,
            structure: {
                id: true,
                name: true,
                userId: true,
            }
        },
        {
            name: 'eventType',
            schema: `
                id text PRIMARY KEY,
                title text,
                description text,
                teamId text REFERENCES team(id),
                ownerId text REFERENCES caldotcom_user(id),
                parentId text REFERENCES eventType(id),
                scheduleId text REFERENCES schedule(id)
            `,
            data: eventType,
            structure: {
                id: true,
                title: true,
                description: true,
                teamId: true,
                ownerId: true,
                parentId: true,
                scheduleId: true,
            }
        },
        {
            name: 'availability',
            schema: `
                id text PRIMARY KEY,
                startTime text,
                endTime text,
                userId text REFERENCES caldotcom_user(id),
                eventTypeId text REFERENCES eventType(id),
                scheduleId text REFERENCES schedule(id)
            `,
            data: availability,
            structure: {
                id: true,
                startTime: 'start',
                endTime: 'end',
                userId: true,
                eventTypeId: true,
                scheduleId: true,
            }
        },
        {
            name: 'outOfOffice',
            schema: `
                id text PRIMARY KEY,
                startTime text,
                endTime text,
                userId text REFERENCES caldotcom_user(id),
                toUserId text REFERENCES caldotcom_user(id)
            `,
            data: outOfOffice,
            structure: {
                id: true,
                startTime: 'start',
                endTime: 'end',
                userId: true,
                toUserId: true,
            }
        },



        {
            name: 'hostGroup',
            schema: `
                id text PRIMARY KEY,
                eventTypeId text REFERENCES eventType(id)
            `,
            data: hostGroup,
            structure: {
                id: true,
                eventTypeId: true,
            }
        },
        {
            name: 'eventHost',
            schema: `
                userId text REFERENCES caldotcom_user(id),
                memberId text REFERENCES membership(id),
                eventTypeId text REFERENCES eventType(id),
                scheduleId text REFERENCES schedule(id),
                hostGroupId text REFERENCES hostGroup(id),
                CONSTRAINT eventHost_pk PRIMARY KEY (userId, eventTypeId)
            `,
            data: eventHost,
            structure: {
                userId: true,
                memberId: true,
                eventTypeId: true,
                scheduleId: true,
                hostGroupId: true,
            }
        },
        {
            name: 'userOnEventType',
            schema: `
                userId text REFERENCES caldotcom_user(id),
                eventTypeId text REFERENCES eventType(id),
                CONSTRAINT userOnEventType_pk PRIMARY KEY (userId, eventTypeId)
            `,
            data: userOnEventType,
            structure: {
                userId: true,
                eventTypeId: true,
            }
        },



        {
            name: 'feature',
            schema: `
                id text PRIMARY KEY,
                name text
            `,
            data: feature,
            structure: {
                id: true,
                name: true,
            }
        },
        {
            name: 'userFeatures',
            schema: `
                userId text REFERENCES caldotcom_user(id),
                featureId text REFERENCES feature(id),
                CONSTRAINT userFeatures_pk PRIMARY KEY (userId, featureId)
            `,
            data: userFeatures,
            structure: {
                userId: true,
                featureId: true,
            }
        },
        {
            name: 'teamFeatures',
            schema: `
                teamId text REFERENCES team(id),
                featureId text REFERENCES feature(id),
                CONSTRAINT teamFeatures_pk PRIMARY KEY (teamId, featureId)
            `,
            data: teamFeatures,
            structure: {
                teamId: true,
                featureId: true,
            }
        },



        {
            name: 'workflow',
            schema: `
                id text PRIMARY KEY,
                name text,
                userId text REFERENCES caldotcom_user(id),
                teamId text REFERENCES team(id)
            `,
            data: workflow,
            structure: {
                id: true,
                name: true,
                userId: true,
                teamId: true,
            }
        },
        {
            name: 'workflowStep',
            schema: `
                id text PRIMARY KEY,
                number text,
                action text,
                workflowId text REFERENCES workflow(id)
            `,
            data: workflowStep,
            structure: {
                id: true,
                number: true,
                action: true,
                workflowId: true,
            }
        },
        {
            name: 'workflowsOnEventTypes',
            schema: `
                workflowId text REFERENCES workflow(id),
                eventTypeId text REFERENCES eventType(id),
                CONSTRAINT workflowsOnEventTypes_pk PRIMARY KEY (workflowId, eventTypeId)
            `,
            data: workflowsOnEventTypes,
            structure: {
                workflowId: true,
                eventTypeId: true,
            }
        },
        {
            name: 'workflowsOnTeams',
            schema: `
                workflowId text REFERENCES workflow(id),
                teamId text REFERENCES team(id),
                CONSTRAINT workflowsOnTeams_pk PRIMARY KEY (workflowId, teamId)
            `,
            data: workflowsOnTeams,
            structure: {
                workflowId: true,
                teamId: true,
            }
        },



        {
            name: 'booking',
            schema: `
                id text PRIMARY KEY,
                title text,
                description text,
                time text,
                userId text REFERENCES caldotcom_user(id),
                eventTypeId text REFERENCES eventType(id)
            `,
            data: booking,
            structure: {
                id: true,
                title: true,
                description: true,
                time: true,
                userId: true,
                eventTypeId: true,
            }
        },
        {
            name: 'attendee',
            schema: `
                id text PRIMARY KEY,
                email text,
                bookingId text REFERENCES booking(id)
            `,
            data: attendee,
            structure: {
                id: true,
                email: true,
                bookingId: true,
            }
        },
    ],
    mongoDB: [
        {
            name: 'team',
            data: team,
            structure: {
                id: true,
                name: true,
                parentId: true,
                roles: new SubCollection(
                    record => importer.findRecordByKey(role, 'teamId', record.id),
                    {
                        id: true,
                        name: true,
                        description: true,
                    }
                ),
                attributes: new SubCollection(
                    record => importer.findRecordByKey(attribute, 'teamId', record.id),
                    {
                        id: true,
                        name: true,
                        options: new SubCollection(
                            record => importer.findRecordByKey(attributeOption, 'attributeId', record.id),
                            {
                                id: true,
                                value: true,
                            }
                        )
                    }
                ),
                organizedBy: new SubCollection(
                    record => importer.findRecordByKey(teamOrgScope, 'teamId', record.id),
                    'userId'
                ),
                verifiedEmails: new SubCollection(
                    record => importer.findRecordByKey(verifiedEmail, 'teamId', record.id),
                    {
                        id: true,
                        value: true,
                        userId: true,
                    }
                ),
                features: new SubCollection(
                    record => importer.findRecordByKey(teamFeatures, 'teamId', record.id),
                    'featureId'
                )
            },
            indexes: [ ['id'] ],
        },
        // {
        //     name: 'role',
        //     data: role,
        //     structure: {
        //         id: true,
        //         name: true,
        //         description: true,
        //         teamId: true,
        //     }
        // },
        // {
        //     name: 'attribute',
        //     data: attribute,
        //     structure: {
        //         id: true,
        //         name: true,
        //         teamId: true,
        //     }
        // },
        // {
        //     name: 'attributeOption',
        //     data: attributeOption,
        //     structure: {
        //         id: true,
        //         value: true,
        //         attributeId: true,
        //     }
        // },



        {
            name: 'caldotcom_user',
            data: user,
            structure: {
                id: true,
                username: true,
                name: true,
                memberOf: new SubCollection(
                    record => importer.findRecordByKey(membership, 'userId', record.id),
                    {
                        id: true,
                        teamId: true,
                        accepted: true,
                        role: true,
                        customRoleId: true,
                        // NOTE: this m:n relationship is only represented from one side (it is recreatable, but probably expensively)
                        attributes: new SubCollection(
                            record => importer.findRecordByKey(attributeToUser, 'memberId', record.id),
                            'attributeOptionId'
                        )
                    }
                ),
                verifiedEmails: new SubCollection(
                    record => importer.findRecordByKey(verifiedEmail, 'userId', record.id),
                    {
                        id: true,
                        value: true,
                        teamId: true,
                    }
                ),
                features: new SubCollection(
                    record => importer.findRecordByKey(userFeatures, 'userId', record.id),
                    'featureId'
                ),
                eventTypes: new SubCollection(
                    record => importer.findRecordByKey(userOnEventType, 'userId', record.id),
                    'eventTypeId'
                ),
                availability: new SubCollection(
                    record => importer.findRecordByKey(availability, 'userId', record.id),
                    {
                        id: true,
                        start: true,
                        end: true,
                        eventTypeId: true,
                    }
                ),
                outOfOffice: new SubCollection(
                    record => importer.findRecordByKey(outOfOffice, 'userId', record.id),
                    {
                        id: true,
                        start: true,
                        end: true,
                        toUserId: true,
                    }
                ),
            },
            indexes: [ ['id'] ],
        },
        // {
        //     name: 'membership',
        //     data: membership,
        //     structure: {
        //         id: true,
        //         userId: true,
        //         teamId: true,
        //         accepted: true,
        //         role: true,
        //         customRoleId: true,
        //     }
        // },
        // {
        //     name: 'teamOrgScope',
        //     data: teamOrgScope,
        //     structure: {
        //         userId: true,
        //         teamId: true,
        //     }
        // },
        // {
        //     name: 'attributeToUser',
        //     data: attributeToUser,
        //     structure: {
        //         attributeOptionId: true,
        //         memberId: true,
        //     }
        // },


        // {
        //     name: 'verifiedEmail',
        //     data: verifiedEmail,
        //     structure: {
        //         id: true,
        //         value: true,
        //         userId: true,
        //         teamId: true,
        //     }
        // },
        {
            name: 'schedule',
            data: schedule,
            structure: {
                id: true,
                name: true,
                userId: true,
                eventTypes: new SubCollection(
                    record => importer.findRecordByKey(eventType, 'scheduleId', record.id),
                    'id'
                ),
                availability: new SubCollection(
                    record => importer.findRecordByKey(availability, 'scheduleId', record.id),
                    {
                        id: true,
                        start: true,
                        end: true,
                        eventTypeId: true,
                    }
                ),
            },
            indexes: [ ['id'], ['userId'] ],
        },
        {
            name: 'eventType',
            data: eventType,
            structure: {
                id: true,
                title: true,
                description: true,
                teamId: true,
                ownerId: true,
                parentId: true,
                scheduleId: true,
                hosts: new SubCollection(
                    record => importer.findRecordByKey(eventHost, 'eventTypeId', record.id),
                    {
                        userId: true,
                        memberId: true,
                    }
                ),
            },
            indexes: [ ['id'] ],
        },
        // {
        //     name: 'availability',
        //     data: availability,
        //     structure: {
        //         id: true,
        //         start: true,
        //         end: true,
        //         userId: true,
        //         eventTypeId: true,
        //         scheduleId: true,
        //     }
        // },
        // {
        //     name: 'outOfOffice',
        //     data: outOfOffice,
        //     structure: {
        //         id: true,
        //         start: true,
        //         end: true,
        //         userId: true,
        //         toUserId: true,
        //     }
        // },



        {
            name: 'hostGroup',
            data: hostGroup,
            structure: {
                id: true,
                eventTypeId: true,
                hosts: new SubCollection(
                    record => importer.findRecordByKey(eventHost, 'hostGroupId', record.id),
                    {
                        userId: true,
                        memberId: true,
                    }
                )
            },
            indexes: [ ['id'] ],
        },
        // { // TODO (maybe?) event host is not represented fully in MongoDB, but maybe that's ok
        //     name: 'eventHost',
        //     data: eventHost,
        //     structure: {
        //         userId: true,
        //         memberId: true,
        //         eventTypeId: true,
        //         scheduleId: true,
        //         // hostGroupId: true,
        //     }
        // },
        // {
        //     name: 'userOnEventType',
        //     data: userOnEventType,
        //     structure: {
        //         userId: true,
        //         eventTypeId: true,
        //     }
        // },



        {
            name: 'feature',
            data: feature,
            structure: {
                id: true,
                name: true,
            }
        },
        // {
        //     name: 'userFeatures',
        //     data: userFeatures,
        //     structure: {
        //         userId: true,
        //         featureId: true,
        //     }
        // },
        // {
        //     name: 'teamFeatures',
        //     data: teamFeatures,
        //     structure: {
        //         teamId: true,
        //         featureId: true,
        //     }
        // },



        {
            name: 'workflow',
            data: workflow,
            structure: {
                id: true,
                name: true,
                userId: true,
                teamId: true,
                steps: new SubCollection(
                    record => importer.findRecordByKey(workflowStep, 'workflowId', record.id),
                    {
                        id: true,
                        number: true,
                        action: true,
                    }
                ),
                activeOn: new SubCollection(
                    record => importer.findRecordByKey(workflowsOnEventTypes, 'workflowId', record.id),
                    'eventTypeId'
                ),
                activeOnTeams: new SubCollection(
                    record => importer.findRecordByKey(workflowsOnTeams, 'workflowId', record.id),
                    'teamId'
                )
            },
            indexes: [ ['id'], ['userId'], ['teamId'] ],
        },
        // {
        //     name: 'workflowStep',
        //     data: workflowStep,
        //     structure: {
        //         id: true,
        //         number: true,
        //         action: true,
        //         workflowId: true,
        //     }
        // },
        // {
        //     name: 'workflowsOnEventTypes',
        //     data: workflowsOnEventTypes,
        //     structure: {
        //         workflowId: true,
        //         eventTypeId: true,
        //     }
        // },
        // {
        //     name: 'workflowsOnTeams',
        //     data: workflowsOnTeams,
        //     structure: {
        //         workflowId: true,
        //         teamId: true,
        //     }
        // },



        {
            name: 'booking',
            data: booking,
            structure: {
                id: true,
                title: true,
                description: true,
                time: true,
                userId: true,
                eventTypeId: true,
                attendees: new SubCollection(
                    record => importer.findRecordByKey(attendee, 'bookingId', record.id),
                    {
                        id: true,
                        email: true,
                    }
                )
            },
            indexes: [ ['id'], ['userId'] ],
        },
        // {
        //     name: 'attendee'?,
        //     data: attendee,
        //     structure: {
        //         id: true,
        //         email: true,
        //         bookingId: true,
        //     }
        // },
    ],
    */
    neo4j: [
        {
            name: 'CDCTeam',
            data: team,
            structure: {
                id: true,
                name: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_TEAM_PARENT',
            data: team,
            structure: { },
            from: {
                label: 'CDCTeam',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCTeam',
                match: { id: 'parentId' },
            },
        },
        {
            name: 'CDCRole',
            data: role,
            structure: {
                id: true,
                name: true,
                description: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_TEAM_ROLE',
            data: role,
            structure: { },
            from: {
                label: 'CDCTeam',
                match: { id: 'teamId' },
            },
            to: {
                label: 'CDCRole',
                match: { id: 'id' },
            },
        },
        {
            name: 'CDCAttribute',
            data: attribute,
            structure: {
                id: true,
                name: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_TEAM_ATTRIBUTE',
            data: attribute,
            structure: { },
            from: {
                label: 'CDCTeam',
                match: { id: 'teamId' },
            },
            to: {
                label: 'CDCAttribute',
                match: { id: 'id' },
            },
        },
        {
            name: 'CDCAttributeOption',
            data: attributeOption,
            structure: {
                id: true,
                value: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_ATTRIBUTE_OPTION',
            data: attributeOption,
            structure: { },
            from: {
                label: 'CDCAttribute',
                match: { id: 'attributeId' },
            },
            to: {
                label: 'CDCAttributeOption',
                match: { id: 'id' },
            },
        },



        {
            name: 'CDCUser',
            data: user,
            structure: {
                id: true,
                username: true,
                name: true
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDCMembership',
            data: membership,
            structure: {
                id: true,
                accepted: true,
                role: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_MEMBERSHIP_USER',
            data: membership,
            structure: { },
            from: {
                label: 'CDCMembership',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCUser',
                match: { id: 'userId' },
            },
        },
        {
            name: 'CDC_MEMBERSHIP_TEAM',
            data: membership,
            structure: { },
            from: {
                label: 'CDCMembership',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCTeam',
                match: { id: 'teamId' },
            },
        },
        {
            name: 'CDC_MEMBERSHIP_ROLE',
            data: membership,
            structure: { },
            from: {
                label: 'CDCMembership',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCRole',
                match: { id: 'customRoleId' },
            },
        },
        {
            name: 'CDC_TEAM_ORG_SCOPE',
            data: teamOrgScope,
            structure: { },
            from: {
                label: 'CDCUser',
                match: { id: 'userId' },
            },
            to: {
                label: 'CDCTeam',
                match: { id: 'teamId' },
            },
        },
        {
            name: 'CDC_USER_TO_ATTRIBUTE',
            data: attributeToUser,
            structure: { },
            from: {
                label: 'CDCMembership',
                match: { id: 'memberId' },
            },
            to: {
                label: 'CDCAttributeOption',
                match: { id: 'attributeOptionId' },
            },
        },



        {
            name: 'CDCVerifiedEmail',
            data: verifiedEmail,
            structure: {
                id: true,
                value: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_USER_EMAIL',
            data: verifiedEmail,
            structure: { },
            from: {
                label: 'CDCUser',
                match: { id: 'userId' },
            },
            to: {
                label: 'CDCVerifiedEmail',
                match: { id: 'id' },
            },
        },
        {
            name: 'CDC_TEAM_EMAIL',
            data: verifiedEmail,
            structure: { },
            from: {
                label: 'CDCTeam',
                match: { id: 'teamId' },
            },
            to: {
                label: 'CDCVerifiedEmail',
                match: { id: 'id' },
            },
        },
        {
            name: 'CDCSchedule',
            data: schedule,
            structure: {
                id: true,
                name: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_USER_SCHEDULE',
            data: schedule,
            structure: { },
            from: {
                label: 'CDCUser',
                match: { id: 'userId' },
            },
            to: {
                label: 'CDCSchedule',
                match: { id: 'id' },
            },
        },
        {
            name: 'CDCEventType',
            data: eventType,
            structure: {
                id: true,
                title: true,
                description: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_TEAM_EVENT_TYPE',
            data: eventType,
            structure: { },
            from: {
                label: 'CDCTeam',
                match: { id: 'teamId' },
            },
            to: {
                label: 'CDCEventType',
                match: { id: 'id' },
            },
        },
        {
            name: 'CDC_EVENT_TYPE_OWNER',
            data: eventType,
            structure: { },
            from: {
                label: 'CDCUser',
                match: { id: 'ownerId' },
            },
            to: {
                label: 'CDCEventType',
                match: { id: 'id' },
            },
        },
        {
            name: 'CDC_EVENT_TYPE_PARENT',
            data: eventType,
            structure: { },
            from: {
                label: 'CDCEventType',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCEventType',
                match: { id: 'parentId' },
            },
        },
        {
            name: 'CDC_SCHEDULE_EVENT_TYPE',
            data: eventType,
            structure: { },
            from: {
                label: 'CDCSchedule',
                match: { id: 'scheduleId' },
            },
            to: {
                label: 'CDCEventType',
                match: { id: 'id' },
            },
        },
        {
            name: 'CDCAvailability',
            data: availability,
            structure: {
                id: true,
                start: true,
                end: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_AVAILABILITY_USER',
            data: availability,
            structure: { },
            from: {
                label: 'CDCAvailability',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCUser',
                match: { id: 'userId' },
            },
        },
        {
            name: 'CDC_AVAILABILITY_EVENT_TYPE',
            data: availability,
            structure: { },
            from: {
                label: 'CDCAvailability',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCEventType',
                match: { id: 'eventTypeId' },
            },
        },
        {
            name: 'CDC_AVAILABILITY_SCHEDULE',
            data: availability,
            structure: { },
            from: {
                label: 'CDCAvailability',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCSchedule',
                match: { id: 'scheduleId' },
            },
        },
        {
            name: 'CDCOutOfOffice',
            data: outOfOffice,
            structure: {
                id: true,
                start: true,
                end: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_USER_OUT_OF_OFFICE',
            data: outOfOffice,
            structure: { },
            from: {
                label: 'CDCUser',
                match: { id: 'userId' },
            },
            to: {
                label: 'CDCOutOfOffice',
                match: { id: 'id' },
            },
        },
        {
            name: 'CDC_OUT_OF_OFFICE_NEWUSER',
            data: outOfOffice,
            structure: { },
            from: {
                label: 'CDCOutOfOffice',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCUser',
                match: { id: 'toUserId' },
            },
        },



        {
            name: 'CDCHostGroup',
            data: hostGroup,
            structure: {
                id: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_HOST_GROUP_EVENT_TYPE',
            data: hostGroup,
            structure: { },
            from: {
                label: 'CDCHostGroup',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCEventType',
                match: { id: 'eventTypeId' },
            },
        },
        {
            name: 'CDCEventHost',
            data: eventHost,
            structure: {
                id: true,
            }
        },
        {
            name: 'CDC_HOST_GROUP_HOST',
            data: eventHost,
            structure: { },
            from: {
                label: 'CDCHostGroup',
                match: { id: 'hostGroupId' },
            },
            to: {
                label: 'CDCEventHost',
                match: { id: 'id' },
            },
        },
        {
            name: 'CDC_HOST_USER',
            data: eventHost,
            structure: { },
            from: {
                label: 'CDCEventHost',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCUser',
                match: { id: 'userId' },
            },
        },
        {
            name: 'CDC_HOST_MEMBER',
            data: eventHost,
            structure: { },
            from: {
                label: 'CDCEventHost',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCMembership',
                match: { id: 'memberId' },
            },
        },
        {
            name: 'CDC_HOST_EVENT_TYPE',
            data: eventHost,
            structure: { },
            from: {
                label: 'CDCEventHost',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCEventType',
                match: { id: 'eventTypeId' },
            },
        },
        {
            name: 'CDC_USER_ON_EVENT_TYPE',
            data: userOnEventType,
            structure: { },
            from: {
                label: 'CDCUser',
                match: { id: 'userId' },
            },
            to: {
                label: 'CDCEventType',
                match: { id: 'eventTypeId' },
            },
        },



        {
            name: 'CDCFeature',
            data: feature,
            structure: {
                id: true,
                name: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_USER_FEATURES',
            data: userFeatures,
            structure: { },
            from: {
                label: 'CDCUser',
                match: { id: 'userId' },
            },
            to: {
                label: 'CDCFeature',
                match: { id: 'featureId' },
            },
        },
        {
            name: 'CDC_TEAM_FEATURES',
            data: teamFeatures,
            structure: { },
            from: {
                label: 'CDCTeam',
                match: { id: 'teamId' },
            },
            to: {
                label: 'CDCFeature',
                match: { id: 'featureId' },
            },
        },



        {
            name: 'CDCWorkflow',
            data: workflow,
            structure: {
                id: true,
                name: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_WORKFLOW_USER',
            data: workflow,
            structure: { },
            from: {
                label: 'CDCWorkflow',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCUser',
                match: { id: 'userId' },
            },
        },
        {
            name: 'CDC_WORKFLOW_TEAM',
            data: workflow,
            structure: { },
            from: {
                label: 'CDCWorkflow',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCTeam',
                match: { id: 'teamId' },
            },
        },
        {
            name: 'CDCWorkflowStep',
            data: workflowStep,
            structure: {
                id: true,
                number: true,
                action: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_WORKFLOW_STEP',
            data: workflowStep,
            structure: { },
            from: {
                label: 'CDCWorkflow',
                match: { id: 'workflowId' },
            },
            to: {
                label: 'CDCWorkflowStep',
                match: { id: 'id' },
            },
        },
        {
            name: 'CDC_WORKFLOWS_ON_EVENT_TYPES',
            data: workflowsOnEventTypes,
            structure: { },
            from: {
                label: 'CDCWorkflow',
                match: { id: 'workflowId' },
            },
            to: {
                label: 'CDCEventType',
                match: { id: 'eventTypeId' },
            },
        },
        {
            name: 'CDC_WORKFLOWS_ON_TEAMS',
            data: workflowsOnTeams,
            structure: { },
            from: {
                label: 'CDCWorkflow',
                match: { id: 'workflowId' },
            },
            to: {
                label: 'CDCTeam',
                match: { id: 'teamId' },
            },
        },



        {
            name: 'CDCBooking',
            data: booking,
            structure: {
                id: true,
                title: true,
                description: true,
                time: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_BOOKING_USER',
            data: booking,
            structure: { },
            from: {
                label: 'CDCBooking',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCUser',
                match: { id: 'userId' },
            },
        },
        {
            name: 'CDC_BOOKING_EVENT_TYPE',
            data: booking,
            structure: { },
            from: {
                label: 'CDCBooking',
                match: { id: 'id' },
            },
            to: {
                label: 'CDCEventType',
                match: { id: 'eventTypeId' },
            },
        },
        {
            name: 'CDCAttendee',
            data: attendee,
            structure: {
                id: true,
                email: true,
            },
            indexes: [ ['id'] ],
        },
        {
            name: 'CDC_BOOKING_ATTENDEE',
            data: attendee,
            structure: { },
            from: {
                label: 'CDCBooking',
                match: { id: 'bookingId' },
            },
            to: {
                label: 'CDCAttendee',
                match: { id: 'id' },
            },
        },
    ],
})
