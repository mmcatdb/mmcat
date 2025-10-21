package cz.matfyz.tests.example.benchmark.caldotcom;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class MongoDB {

    private MongoDB() {}

    public static final Datasource datasource = new Datasource(DatasourceType.mongodb, "mongodb");

    public static final String
        eventTypeKind = "eventType",
        userKind = "caldotcom_user",
        userOnEventTypeKind = "userOnEventType",
        teamKind = "team",
        teamOrgScopeKind = "teamOrgScope",
        membershipKind = "membership",
        bookingKind = "booking",
        attendeeKind = "attendee",
        workflowKind = "workflow",
        workflowsOnEventTypesKind = "workflowsOnEventTypes",
        roleKind = "role";

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
                b.simple("parentId", Schema.eventTypeToParent.signature().concatenate(Schema.eventTypeToId.signature()))
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

    public static TestMapping team(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.team,
            userKind,
            b -> b.root(
                b.simple("id", Schema.userToId),
                b.simple("name", Schema.userToName),
                b.simple("parentId", Schema.teamToParent.signature().concatenate(Schema.teamToId.signature()))
            )
        );
    }

    public static TestMapping role(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.role,
            userKind,
            b -> b.root(
                b.simple("id", Schema.roleToId),
                b.simple("name", Schema.roleToName),
                b.simple("description", Schema.roleToDescription),
                b.simple("team", Schema.roleToTeam.signature().concatenate(Schema.teamToId.signature()))
            )
        );
    }

    public static TestMapping membership(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.membership,
            userKind,
            b -> b.root(
                b.simple("id", Schema.teamToId),
                b.simple("userId", Schema.membershipToUser.signature().concatenate(Schema.userToId.signature())),
                b.simple("teamId", Schema.membershipToTeam.signature().concatenate(Schema.teamToId.signature())),
                b.simple("accepted", Schema.membershipToAccepted),
                b.simple("role", Schema.membershipToRole),
                b.simple("customRoleId", Schema.membershipToCustomRole.signature().concatenate(Schema.roleToId.signature()))
            )
        );
    }

    public static TestMapping booking(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.booking,
            userKind,
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
            userKind,
            b -> b.root(
                b.simple("id", Schema.attendeeToId),
                b.simple("email", Schema.attendeeToEmail),
                b.simple("bookingId", Schema.attendeeToBooking.signature().concatenate(Schema.bookingToId.signature()))
            )
        );
    }

    public static TestMapping workflow(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflow,
            userKind,
            b -> b.root(
                b.simple("id", Schema.workflowToId),
                b.simple("name", Schema.workflowToName),
                b.simple("userId", Schema.workflowToUser.signature().concatenate(Schema.userToId.signature())),
                b.simple("teamId", Schema.workflowToTeam.signature().concatenate(Schema.teamToId.signature()))
            )
        );
    }

    public static TestMapping userOnEventType(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.userOnEventType,
            userKind,
            b -> b.root(
                b.simple("userId", Schema.userOnEventTypeU.signature().concatenate(Schema.userToId.signature())),
                b.simple("eventTypeId", Schema.userOnEventTypeET.signature().concatenate(Schema.eventTypeToId.signature()))
            )
        );
    }

    public static TestMapping teamOrgScope(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.teamOrgScope,
            userKind,
            b -> b.root(
                b.simple("userId", Schema.teamOrgScopeU.signature().concatenate(Schema.userToId.signature())),
                b.simple("teamId", Schema.teamOrgScopeT.signature().concatenate(Schema.teamToId.signature()))
            )
        );
    }

    public static TestMapping workflowsOnEventTypes(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflowsOnEventTypes,
            userKind,
            b -> b.root(
                b.simple("workflowId", Schema.workflowsOnEventTypesWF.signature().concatenate(Schema.workflowToId.signature())),
                b.simple("eventTypeId", Schema.workflowsOnEventTypesET.signature().concatenate(Schema.eventTypeToId.signature()))
            )
        );
    }

}
