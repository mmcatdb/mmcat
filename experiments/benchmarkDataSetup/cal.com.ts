import { Importer, generateWithinRange } from './helpers/Importer.ts'
import random from 'random'
import { RandomHelper } from './helpers/RandomHelper.ts'

random.use('helloworld')
const randomHelper = new RandomHelper(random)
const importer = new Importer('caldotcom', parseFloat(process.argv[2] || '1.0'))

// # STEP 1: Create master entries (likely imperatively through this or other JS files)

// `random.shuffle` could be useful for remaking order of arrays for various independent distributions

const date1 = new Date(2020, 0, 1)
const date2 = new Date(date1.getTime() + 1000 * 60 * 60 * 24 * 366)

let idn = 1

// datetime format = YYYY-MM-DD hh:mm:ss +-h:mm

const schedulingTypeGen = () => random.choice(['ROUND_ROBIN', 'COLLECTIVE', 'MANAGED'])!
const periodTypeGen = () => random.choice(['UNLIMITED', 'ROLLING', 'ROLLING_WINDOW', 'RANGE'])!
const creationSourceGen = () => random.choice(['API_V1', 'API_V2', 'WEBAPP'])!
const membershipRoleGen = () => random.choice(['MEMBER', 'ADMIN', 'OWNER'])!

const user = importer.generateRecords(10, () => ({
    id: (idn++).toString(),
    username: randomHelper.string(8),
    name: randomHelper.string(6),
}), ['username'])

const team = importer.generateRecords(10, previous => ({
    id: (idn++).toString(),
    name: randomHelper.string(10),
    parentId: randomHelper.nullable(0.9, () => random.choice(previous).id)
}))

const role = importer.generateRecords(10, () => ({
    id: (idn++).toString(),
    name: randomHelper.string(8),
    description: randomHelper.string(generateWithinRange(() => Math.round(rcr()), 50, 950)),
    team: randomHelper.record(team)
}))

let membership = importer.generateRecords(10, () => {
    const teamId = randomHelper.record(team, random.geometric(1 / team.length)).id
    return {
        id: (idn++).toString(),
        userId: randomHelper.record(user, random.geometric(3 / user.length)).id,
        teamId,
        accepted: random.boolean(),
        role: membershipRoleGen(),
        customRoleId: random.choice(importer.findRecordByKey(role, 'team', teamId)).id
    }
}, ['userId', 'teamId'])

let team2 = random.shuffle(team)

const rcr = random.normal(500, 150)
const eventType = importer.generateRecords(10, () => {
    const teamId = randomHelper.record(team2, random.geometric(1 / team2.length)).id
    const users = importer.findRecordByKey(membership, 'teamId', teamId)
    return {
        id: (idn++).toString(),
        title: randomHelper.string(8),
        description: randomHelper.string(generateWithinRange(() => Math.round(rcr()), 50, 950)),
        teamId,
    }
})
// ensure event type parent and owner is not from unrelated team
for (const et of eventType) {
    if (!et.ownerId) {
        const teamId = et.teamId
        const eventTypes = importer.findRecordByKey(eventType, 'teamId', teamId)
        const users = importer.findRecordByKey(membership, 'teamId', teamId)

        for (let i = 0; i < eventTypes.length; i++) {
            eventTypes[i].ownerId = randomHelper.record(users, random.geometric(5 / users.length)).userId

            eventTypes[i].parentId = randomHelper.nullable(0.5, eventTypes[random.int(0, i - 1)].id)
        }
    }
}


const booking = importer.generateRecords(10, () => {
    const teamId = randomHelper.record(team2, random.geometric(1 / team2.length)).id
    const eventTypes = importer.findRecordByKey(eventType, 'teamId', teamId)
    const users = importer.findRecordByKey(membership, 'teamId', teamId)
    return {
        id: (idn++).toString(),
        title: randomHelper.string(8),
        description: randomHelper.string(generateWithinRange(() => Math.round(rcr()), 50, 950)),
        userId: randomHelper.record(users, random.geometric(5 / users.length)).userId,
        eventTypeId: randomHelper.record(eventTypes).id,
    }
})

const attendee = importer.generateRecords(10, () => ({
    id: (idn++).toString(),
    email: randomHelper.string(12),
    bookingId: randomHelper.record(booking, random.geometric(3 / booking.length))
}))

membership = random.shuffle(membership)

const workflow = importer.generateRecords(10, () => {
    const member = randomHelper.record(membership, random.geometric(5 / membership.length))

    return {
        id: (idn++).toString(),
        name: randomHelper.string(8),
        userId: member.userId,
        teamId: member.teamId
    }
})

const userOnEventType = importer.generateRecords(10, () => {
    const et = randomHelper.record(eventType)
    return {
        eventTypeId: et.id,
        userId: random.choice(importer.findRecordByKey(membership, 'teamId', et.teamId)).userId,
    }
}, ['userId', 'eventTypeId'])

const teamOrgScope = importer.generateRecords(10, () => {
    const t = random.choice(team)
    return {
        userId: random.choice(importer.findRecordByKey(user, 'teamId', t.id)),
        teamId: t.id,
    }
}, ['userId', 'teamId'])

const workflowsOnEventTypes = importer.generateRecords(10, () => {
    const wf = randomHelper.record(workflow)
    return {
        workflowId: wf.id,
        eventTypeId: random.choice(importer.findRecordByKey(eventType, 'teamId', wf.teamId)).id,
    }
}, ['workflowId', 'eventTypeId'])

// const workflowsOnTeams = importer.generateRecords(10, () => ({
// }))

// TODO: create indexes (like in yelp), import, figure out how to work with relations not in schema category, use faker

importer.importData({
    postgreSQL: [
        {
            name: 'caldotcom_user',
            schema: `
                id integer PRIMARY KEY,
                username char(22) UNIQUE NOT NULL,
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
            name: 'team',
            schema: `
                id integer PRIMARY KEY,
                name text,
                parentId integer REFERENCES team(id)
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
                id integer PRIMARY KEY,
                name text,
                description text
                team integer REFERENCES team(id)
            `,
            data: role,
            structure: {
                id: true,
                name: true,
                description: true,
                team: true,
            }
        },
        {
            name: 'membership',
            schema: `
                id integer PRIMARY KEY,
                userId integer REFERENCES caldotcom_user(id),
                teamId integer REFERENCES team(id),
                accepted boolean,
                role text,
                customRoleId integer REFERENCES role(id)
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
            name: 'eventType',
            schema: `
                id integer PRIMARY KEY,
                title text,
                description text,
                teamId integer REFERENCES team(id),
                ownerId integer REFERENCES caldotcom_user(id),
                parentId integer REFERENCES eventType(id),
            `,
            data: eventType,
            structure: {
                id: true,
                title: true,
                description: true,
                teamId: true,
                ownerId: true,
                parentId: true,
            }
        },
        {
            name: 'booking',
            schema: `
                id integer PRIMARY KEY,
                title text,
                description text,
                userId integer REFERENCES caldotcom_user(id)
                eventTypeId integer REFERENCES eventType(id)
            `,
            data: booking,
            structure: {
                id: true,
                title: true,
                description: true,
                userId: true,
                eventTypeId: true,
            }
        },
        {
            name: 'attendee',
            schema: `
                id integer PRIMARY KEY,
                email text,
                bookingId integer REFERENCES booking(id)
            `,
            data: attendee,
            structure: {
                id: true,
                email: true,
                bookingId: true,
            }
        },
        {
            name: 'workflow',
            schema: `
                id integer PRIMARY KEY,
                name text,
                userId integer REFERENCES caldotcom_user(id),
                teamId integer REFERENCES team(id)
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
            name: 'userOnEventType',
            schema: `
                userId integer REFERENCES caldotcom_user(id),
                eventTypeId integer REFERENCES eventType(id),
                CONSTRAINT pk PRIMARY KEY (userId, eventTypeId)
            `,
            data: userOnEventType,
            structure: {
                userId: true,
                eventTypeId: true,
            }
        },
        {
            name: 'teamOrgScope',
            schema: `
                userId integer REFERENCES caldotcom_user(id),
                teamId integer REFERENCES team(id),
                CONSTRAINT pk PRIMARY KEY (userId, teamId)
            `,
            data: teamOrgScope,
            structure: {
                userId: true,
                teamId: true,
            }
        },
        {
            name: 'workflowsOnEventTypes',
            schema: `
                workflowId integer REFERENCES workflow(id),
                eventTypeId integer REFERENCES eventType(id),
                CONSTRAINT pk PRIMARY KEY (workflowId, eventTypeId)
            `,
            data: workflowsOnEventTypes,
            structure: {
                workflowId: true,
                eventTypeId: true,
            }
        },
    ],
    mongoDB: [
        {
            name: 'caldotcom_user',
            data: user,
            structure: {
                id: true,
                username: true,
                name: true
            }
        },
        {
            name: 'team',
            data: team,
            structure: {
                id: true,
                name: true,
                parentId: true
            }
        },
        {
            name: 'role',
            data: role,
            structure: {
                id: true,
                name: true,
                description: true,
                team: true,
            }
        },
        {
            name: 'membership',
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
            name: 'eventType',
            data: eventType,
            structure: {
                id: true,
                title: true,
                description: true,
                teamId: true,
                ownerId: true,
                parentId: true,
            }
        },
        {
            name: 'booking',
            data: booking,
            structure: {
                id: true,
                title: true,
                description: true,
                userId: true,
                eventTypeId: true,
            }
        },
        {
            name: 'attendee',
            data: attendee,
            structure: {
                id: true,
                email: true,
                bookingId: true,
            }
        },
        {
            name: 'workflow',
            data: workflow,
            structure: {
                id: true,
                name: true,
                userId: true,
                teamId: true,
            }
        },
        {
            name: 'userOnEventType',
            data: userOnEventType,
            structure: {
                userId: true,
                eventTypeId: true,
            }
        },
        {
            name: 'teamOrgScope',
            data: teamOrgScope,
            structure: {
                userId: true,
                teamId: true,
            }
        },
        {
            name: 'workflowsOnEventTypes',
            data: workflowsOnEventTypes,
            structure: {
                workflowId: true,
                eventTypeId: true,
            }
        },
    ],
    neo4j: [
        {
            name: 'CDCUser',
            data: user,
            structure: {
                id: true,
                username: true,
                name: true
            }
        },
        {
            name: 'CDCTeam',
            data: team,
            structure: {
                id: true,
                name: true,
                parentId: true
            }
        },
        {
            name: 'CDCRole',
            data: role,
            structure: {
                id: true,
                name: true,
                description: true,
                team: true,
            }
        },
        {
            name: 'CDCMembership',
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
            name: 'CDCEventType',
            data: eventType,
            structure: {
                id: true,
                title: true,
                description: true,
                teamId: true,
                ownerId: true,
                parentId: true,
            }
        },
        {
            name: 'CDCBooking',
            data: booking,
            structure: {
                id: true,
                title: true,
                description: true,
                userId: true,
                eventTypeId: true,
            }
        },
        {
            name: 'CDCAttendee',
            data: attendee,
            structure: {
                id: true,
                email: true,
                bookingId: true,
            }
        },
        {
            name: 'CDCWorkflow',
            data: workflow,
            structure: {
                id: true,
                name: true,
                userId: true,
                teamId: true,
            }
        },
        {
            name: 'CDC_USER_ON_EVENT_TYPE',
            data: userOnEventType,
            structure: { },
            from: {
                label: 'CDCUser',
                match: { userId: 'id' },
            },
            to: {
                label: 'CDCEventType',
                match: { eventTypeId: 'id' },
            },
        },
        {
            name: 'CDC_TEAM_ORG_SCOPE',
            data: teamOrgScope,
            structure: { },
            from: {
                label: 'CDCUser',
                match: { userId: 'id' },
            },
            to: {
                label: 'CDCTeam',
                match: { teamId: 'id' },
            },
        },
        {
            name: 'CDC_WORKFLOWS_ON_EVENT_TYPES',
            data: workflowsOnEventTypes,
            structure: { },
            from: {
                label: 'CDCWorkflow',
                match: { userId: 'id' },
            },
            to: {
                label: 'CDCTeam',
                match: { teamId: 'id' },
            },
        },
    ],
})

