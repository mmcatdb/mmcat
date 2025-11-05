package cz.matfyz.tests.example.benchmark.caldotcom;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class PostgreSQL {

    private PostgreSQL() {}

    public static final Datasource datasource = new Datasource(DatasourceType.postgresql, "postgresql");

    public static final String
        teamKind = "team",
        roleKind = "role",
        attributeKind = "attribute",
        attributeOptionKind = "attributeOption",

        userKind = "caldotcom_user",
        membershipKind = "membership",
        teamOrgScopeKind = "teamOrgScope",
        attributeToUserKind = "attributeToUser",

        verifiedEmailKind = "verifiedEmail",
        scheduleKind = "schedule",
        eventTypeKind = "eventType",
        availabilityKind = "availability",
        outOfOfficeKind = "outOfOffice",

        hostGroupKind = "hostGroup",
        eventHostKind = "eventHost",
        userOnEventTypeKind = "userOnEventType",

        featureKind = "feature",
        userFeaturesKind = "userFeatures",
        teamFeaturesKind = "teamFeatures",

        workflowKind = "workflow",
        workflowStepKind = "workflowStep",
        workflowsOnEventTypesKind = "workflowsOnEventTypes",
        workflowsOnTeamsKind = "workflowsOnTeams",

        bookingKind = "booking",
        attendeeKind = "attendee";

    public static TestMapping team(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.team,
            teamKind,
            b -> b.root(
                b.simple("id", Schema.teamToId),
                b.simple("name", Schema.teamToName),
                b.simple("parentId", Schema.teamToParent.signature().concatenate(Schema.teamToId.signature()))
            )
        );
    }

    public static TestMapping role(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.role,
            roleKind,
            b -> b.root(
                b.simple("id", Schema.roleToId),
                b.simple("name", Schema.roleToName),
                b.simple("description", Schema.roleToDescription),
                b.simple("teamId", Schema.roleToTeam.signature().concatenate(Schema.teamToId.signature()))
            )
        );
    }

    public static TestMapping attribute(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attribute,
            attributeKind,
            b -> b.root(
                b.simple("id", Schema.attributeToId),
                b.simple("name", Schema.attributeToName),
                b.simple("teamId", Schema.attributeToTeam.signature().concatenate(Schema.teamToId.signature()))
            )
        );
    }

    public static TestMapping attributeOption(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attributeOption,
            attributeOptionKind,
            b -> b.root(
                b.simple("id", Schema.attributeOptionToId),
                b.simple("value", Schema.attributeOptionToValue),
                b.simple("attributeId", Schema.attributeOptionToAttribute.signature().concatenate(Schema.attributeToId.signature()))
            )
        );
    }



    public static TestMapping user(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.user,
            userKind,
            b -> b.root(
                b.simple("id", Schema.userToId),
                b.simple("username", Schema.userToUsername),
                b.simple("name", Schema.userToName)
            )
        );
    }

    public static TestMapping membership(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.membership,
            membershipKind,
            b -> b.root(
                b.simple("id", Schema.membershipToId),
                b.simple("userId", Schema.membershipToUser.signature().concatenate(Schema.userToId.signature())),
                b.simple("teamId", Schema.membershipToTeam.signature().concatenate(Schema.teamToId.signature())),
                b.simple("accepted", Schema.membershipToAccepted),
                b.simple("role", Schema.membershipToRole),
                b.simple("customRoleId", Schema.membershipToCustomRole.signature().concatenate(Schema.roleToId.signature()))
            )
        );
    }

    public static TestMapping teamOrgScope(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.teamOrgScope,
            teamOrgScopeKind,
            b -> b.root(
                b.simple("userId", Schema.teamOrgScopeU.signature().concatenate(Schema.userToId.signature())),
                b.simple("teamId", Schema.teamOrgScopeT.signature().concatenate(Schema.teamToId.signature()))
            )
        );
    }

    public static TestMapping attributeToUser(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attributeToUser,
            attributeToUserKind,
            b -> b.root(
                b.simple("attributeOptionId", Schema.attributeToUserA.signature().concatenate(Schema.attributeOptionToId.signature())),
                b.simple("memberId", Schema.attributeToUserU.signature().concatenate(Schema.membershipToId.signature()))
            )
        );
    }

    

    public static TestMapping verifiedEmail(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.verifiedEmail,
            verifiedEmailKind,
            b -> b.root(
                b.simple("id", Schema.verifiedEmailToId),
                b.simple("value", Schema.verifiedEmailToValue),
                b.simple("userId", Schema.verifiedEmailToUser.signature().concatenate(Schema.userToId.signature())),
                b.simple("teamId", Schema.verifiedEmailToTeam.signature().concatenate(Schema.teamToId.signature()))
            )
        );
    }

    public static TestMapping schedule(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.schedule,
            scheduleKind,
            b -> b.root(
                b.simple("id", Schema.scheduleToId),
                b.simple("name", Schema.scheduleToName),
                b.simple("userId", Schema.scheduleToUser.signature().concatenate(Schema.userToId.signature()))
            )
        );
    }

    public static TestMapping eventType(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventType,
            eventTypeKind,
            b -> b.root(
                b.simple("id", Schema.eventTypeToId),
                b.simple("title", Schema.eventTypeToTitle),
                b.simple("description", Schema.eventTypeToDescription),
                b.simple("teamId", Schema.eventTypeToTeam.signature().concatenate(Schema.teamToId.signature())),
                b.simple("ownerId", Schema.eventTypeToOwner.signature().concatenate(Schema.userToId.signature())),
                b.simple("parentId", Schema.eventTypeToParent.signature().concatenate(Schema.eventTypeToId.signature())),
                b.simple("scheduleId", Schema.eventTypeToSchedule.signature().concatenate(Schema.scheduleToId.signature()))
            )
        );
    }

    public static TestMapping availability(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.availability,
            availabilityKind,
            b -> b.root(
                b.simple("id", Schema.availabilityToId),
                b.simple("start", Schema.availabilityToStart),
                b.simple("end", Schema.availabilityToEnd),
                b.simple("userId", Schema.availabilityToUser.signature().concatenate(Schema.userToId.signature())),
                b.simple("eventTypeId", Schema.availabilityToEventType.signature().concatenate(Schema.eventTypeToId.signature())),
                b.simple("scheduleId", Schema.availabilityToSchedule.signature().concatenate(Schema.scheduleToId.signature()))
            )
        );
    }

    public static TestMapping outOfOffice(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.outOfOffice,
            outOfOfficeKind,
            b -> b.root(
                b.simple("id", Schema.outOfOfficeToId),
                b.simple("start", Schema.outOfOfficeToStart),
                b.simple("end", Schema.outOfOfficeToEnd),
                b.simple("userId", Schema.outOfOfficeToUser.signature().concatenate(Schema.userToId.signature())),
                b.simple("toUserId", Schema.outOfOfficeToNewUser.signature().concatenate(Schema.userToId.signature()))
            )
        );
    }



    public static TestMapping hostGroup(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.hostGroup,
            hostGroupKind,
            b -> b.root(
                b.simple("id", Schema.hostGroupToId),
                b.simple("eventTypeId", Schema.hostGroupToEventType.signature().concatenate(Schema.eventTypeToId.signature()))
            )
        );
    }

    public static TestMapping eventHost(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventHost,
            eventHostKind,
            b -> b.root(
                b.simple("userId", Schema.eventHostToUser.signature().concatenate(Schema.userToId.signature())),
                b.simple("memberId", Schema.eventHostToMembership.signature().concatenate(Schema.membershipToId.signature())),
                b.simple("eventTypeId", Schema.eventHostToEventType.signature().concatenate(Schema.eventTypeToId.signature())),
                b.simple("scheduleId", Schema.eventHostToSchedule.signature().concatenate(Schema.scheduleToId.signature())),
                b.simple("hostGroupId", Schema.eventHostToHostGroup.signature().concatenate(Schema.hostGroupToId.signature()))
            )
        );
    }

    public static TestMapping userOnEventType(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.userOnEventType,
            userOnEventTypeKind,
            b -> b.root(
                b.simple("userId", Schema.userOnEventTypeU.signature().concatenate(Schema.userToId.signature())),
                b.simple("eventTypeId", Schema.userOnEventTypeET.signature().concatenate(Schema.eventTypeToId.signature()))
            )
        );
    }



    public static TestMapping feature(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.feature,
            featureKind,
            b -> b.root(
                b.simple("id", Schema.featureToId),
                b.simple("name", Schema.featureToId)
            )
        );
    }

    public static TestMapping userFeatures(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.userFeatures,
            userFeaturesKind,
            b -> b.root(
                b.simple("userId", Schema.userFeaturesU.signature().concatenate(Schema.userToId.signature())),
                b.simple("eventTypeId", Schema.userFeaturesF.signature().concatenate(Schema.featureToId.signature()))
            )
        );
    }

    public static TestMapping teamFeatures(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.teamFeatures,
            teamFeaturesKind,
            b -> b.root(
                b.simple("teamId", Schema.teamFeaturesT.signature().concatenate(Schema.teamToId.signature())),
                b.simple("eventTypeId", Schema.teamFeaturesF.signature().concatenate(Schema.featureToId.signature()))
            )
        );
    }



    public static TestMapping workflow(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflow,
            workflowKind,
            b -> b.root(
                b.simple("id", Schema.workflowToId),
                b.simple("name", Schema.workflowToName),
                b.simple("userId", Schema.workflowToUser.signature().concatenate(Schema.userToId.signature())),
                b.simple("teamId", Schema.workflowToTeam.signature().concatenate(Schema.teamToId.signature()))
            )
        );
    }

    public static TestMapping workflowStep(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflowStep,
            workflowStepKind,
            b -> b.root(
                b.simple("id", Schema.workflowStepToId),
                b.simple("number", Schema.workflowStepToNumber),
                b.simple("action", Schema.workflowStepToAction),
                b.simple("workflowId", Schema.workflowStepToWorkflow.signature().concatenate(Schema.workflowToId.signature()))
            )
        );
    }

    public static TestMapping workflowsOnEventTypes(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflowsOnEventTypes,
            workflowsOnEventTypesKind,
            b -> b.root(
                b.simple("workflowId", Schema.workflowsOnEventTypesWF.signature().concatenate(Schema.workflowToId.signature())),
                b.simple("eventTypeId", Schema.workflowsOnEventTypesET.signature().concatenate(Schema.eventTypeToId.signature()))
            )
        );
    }

    public static TestMapping workflowsOnTeams(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflowsOnTeams,
            workflowsOnTeamsKind,
            b -> b.root(
                b.simple("workflowId", Schema.workflowsOnTeamsWF.signature().concatenate(Schema.workflowToId.signature())),
                b.simple("eventTypeId", Schema.workflowsOnTeamsT.signature().concatenate(Schema.teamToId.signature()))
            )
        );
    }



    public static TestMapping booking(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.booking,
            bookingKind,
            b -> b.root(
                b.simple("id", Schema.bookingToId),
                b.simple("title", Schema.bookingToTitle),
                b.simple("description", Schema.bookingToDescription),
                b.simple("userId", Schema.bookingToUser.signature().concatenate(Schema.userToId.signature())),
                b.simple("eventTypeId", Schema.bookingToEventType.signature().concatenate(Schema.eventTypeToId.signature()))
            )
        );
    }

    public static TestMapping attendee(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attendee,
            attendeeKind,
            b -> b.root(
                b.simple("id", Schema.attendeeToId),
                b.simple("email", Schema.attendeeToEmail),
                b.simple("bookingId", Schema.attendeeToBooking.signature().concatenate(Schema.bookingToId.signature()))
            )
        );
    }

}
