package cz.matfyz.tests.example.benchmark.caldotcom;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class MongoDB {

    private MongoDB() {}

    public static final Datasource datasource = new Datasource(DatasourceType.mongodb, "mongodb");

    public static final String
        teamKind = "team",
        userKind = "caldotcom_user",
        scheduleKind = "schedule",
        eventTypeKind = "eventType",
        hostGroupKind = "hostGroup",
        featureKind = "feature",
        workflowKind = "workflow",
        bookingKind = "booking";

    public static TestMapping team(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.team,
            teamKind,
            b -> b.root(
                b.simple("id", Schema.teamToId),
                b.simple("name", Schema.teamToName),
                b.simple("parentId", Schema.teamToParent.signature().concatenate(Schema.teamToId.signature())),
                b.complex("roles", Schema.roleToTeam.dual(),
                    b.simple("id", Schema.roleToId),
                    b.simple("name", Schema.roleToName),
                    b.simple("description", Schema.roleToDescription)
                ),
                b.complex("attributes", Schema.attributeToTeam.dual(),
                    b.simple("id", Schema.attributeToId),
                    b.simple("name", Schema.attributeToName),
                    b.complex("options", Schema.attributeOptionToAttribute.dual(),
                        b.simple("id", Schema.attributeOptionToId),
                        b.simple("value", Schema.attributeOptionToValue)
                    )
                ),
                b.complex("verifiedEmails", Schema.verifiedEmailToTeam.dual(),
                    b.simple("id", Schema.verifiedEmailToId),
                    b.simple("value", Schema.verifiedEmailToValue),
                    b.simple("userId", Schema.verifiedEmailToUser.signature().concatenate(Schema.userToId.signature()))
                ),
                b.simple("organizedBy", Schema.teamOrgScopeT.dual()
                    .concatenate(Schema.teamOrgScopeU.signature())
                    .concatenate(Schema.userToId.signature())
                )
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
                b.simple("name", Schema.userToName),
                b.complex("memberOf", Schema.membershipToUser.dual(),
                    b.simple("id", Schema.membershipToId),
                    b.simple("teamId", Schema.membershipToTeam.signature().concatenate(Schema.teamToId.signature())),
                    b.simple("accepted", Schema.membershipToAccepted),
                    b.simple("role", Schema.membershipToRole),
                    b.simple("customRoleId", Schema.membershipToCustomRole.signature().concatenate(Schema.roleToId.signature())),
                    b.simple("attributes", Schema.attributeToUserU.dual()
                        .concatenate(Schema.attributeToUserA.signature())
                        .concatenate(Schema.attributeOptionToId.signature())
                    )
                ),
                b.complex("verifiedEmails", Schema.verifiedEmailToUser.dual(),
                    b.simple("id", Schema.verifiedEmailToId),
                    b.simple("value", Schema.verifiedEmailToValue),
                    b.simple("teamId", Schema.verifiedEmailToTeam.signature().concatenate(Schema.teamToId.signature()))
                ),
                b.simple("features", Schema.userFeaturesU.dual()
                    .concatenate(Schema.userFeaturesF.signature())
                    .concatenate(Schema.featureToId.signature())
                ),
                b.simple("eventTypes", Schema.userOnEventTypeU.dual()
                    .concatenate(Schema.userOnEventTypeET.signature())
                    .concatenate(Schema.eventTypeToId.signature())
                ),
                b.complex("availability", Schema.availabilityToUser.dual(),
                    b.simple("id", Schema.availabilityToId),
                    b.simple("start", Schema.availabilityToStart),
                    b.simple("end", Schema.availabilityToEnd),
                    b.simple("eventTypeId", Schema.availabilityToEventType.signature().concatenate(Schema.eventTypeToId.signature()))
                ),
                b.complex("outOfOffice", Schema.outOfOfficeToUser.dual(),
                    b.simple("id", Schema.outOfOfficeToId),
                    b.simple("start", Schema.outOfOfficeToStart),
                    b.simple("end", Schema.outOfOfficeToEnd),
                    b.simple("toUserId", Schema.outOfOfficeToNewUser.signature().concatenate(Schema.userToId.signature()))
                )
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
                b.simple("userId", Schema.scheduleToUser.signature().concatenate(Schema.userToId.signature())),
                b.simple("eventTypes", Schema.eventTypeToSchedule.dual()
                    .concatenate(Schema.eventTypeToId.signature())
                ),
                b.complex("availability", Schema.availabilityToSchedule.dual(),
                    b.simple("id", Schema.availabilityToId),
                    b.simple("start", Schema.availabilityToStart),
                    b.simple("end", Schema.availabilityToEnd),
                    b.simple("eventTypeId", Schema.availabilityToEventType.signature().concatenate(Schema.eventTypeToId.signature()))
                )
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
                b.simple("scheduleId", Schema.eventTypeToSchedule.signature().concatenate(Schema.scheduleToId.signature())),
                b.complex("hosts", Schema.eventHostToEventType.dual(),
                    b.simple("userId", Schema.eventHostToUser.signature().concatenate(Schema.userToId.signature())),
                    b.simple("memberId", Schema.eventHostToMembership.signature().concatenate(Schema.membershipToId.signature()))
                )
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



    public static TestMapping workflow(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflow,
            workflowKind,
            b -> b.root(
                b.simple("id", Schema.workflowToId),
                b.simple("name", Schema.workflowToName),
                b.simple("userId", Schema.workflowToUser.signature().concatenate(Schema.userToId.signature())),
                b.simple("teamId", Schema.workflowToTeam.signature().concatenate(Schema.teamToId.signature())),
                b.complex("steps", Schema.workflowStepToWorkflow.dual(),
                    b.simple("id", Schema.workflowStepToId),
                    b.simple("number", Schema.workflowStepToNumber),
                    b.simple("action", Schema.workflowStepToAction)
                ),
                b.simple("activeOn", Schema.workflowsOnEventTypesWF.dual()
                    .concatenate(Schema.workflowsOnEventTypesET.signature())
                    .concatenate(Schema.eventTypeToId.signature())
                ),
                b.simple("activeOnTeams", Schema.workflowsOnTeamsWF.dual()
                    .concatenate(Schema.workflowsOnTeamsT.signature())
                    .concatenate(Schema.teamToId.signature())
                )
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
                b.simple("eventTypeId", Schema.bookingToEventType.signature().concatenate(Schema.eventTypeToId.signature())),
                b.complex("attendees", Schema.attendeeToBooking.dual(),
                    b.simple("id", Schema.attendeeToId),
                    b.simple("email", Schema.attendeeToEmail)
                )
            )
        );
    }

}
