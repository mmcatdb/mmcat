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
                b.simple("id", Schema.team_id),
                b.simple("name", Schema.team_name),
                b.simple("parentId", Schema.team_parent.signature().concatenate(Schema.team_id.signature())),
                b.complex("roles", Schema.role_team.dual(),
                    b.simple("id", Schema.role_id),
                    b.simple("name", Schema.role_name),
                    b.simple("description", Schema.role_description)
                ),
                b.complex("attributes", Schema.attribute_team.dual(),
                    b.simple("id", Schema.attribute_id),
                    b.simple("name", Schema.attribute_name),
                    b.complex("options", Schema.attributeOption_attribute.dual(),
                        b.simple("id", Schema.attributeOption_id),
                        b.simple("value", Schema.attributeOption_value)
                    )
                ),
                b.complex("verifiedEmails", Schema.verifiedEmail_team.dual(),
                    b.simple("id", Schema.verifiedEmail_id),
                    b.simple("value", Schema.verifiedEmail_value),
                    b.simple("userId", Schema.verifiedEmail_user.signature().concatenate(Schema.user_id.signature()))
                ),
                b.simple("organizedBy", Schema.teamOrgScope_t.dual()
                    .concatenate(Schema.teamOrgScope_u.signature())
                    .concatenate(Schema.user_id.signature())
                )
            )
        );
    }



    public static TestMapping user(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.user,
            userKind,
            b -> b.root(
                b.simple("id", Schema.user_id),
                b.simple("username", Schema.user_username),
                b.simple("name", Schema.user_name),
                b.complex("memberOf", Schema.membership_user.dual(),
                    b.simple("id", Schema.membership_id),
                    b.simple("teamId", Schema.membership_team.signature().concatenate(Schema.team_id.signature())),
                    b.simple("accepted", Schema.membership_accepted),
                    b.simple("role", Schema.membership_role),
                    b.simple("customRoleId", Schema.membership_customRole.signature().concatenate(Schema.role_id.signature())),
                    b.simple("attributes", Schema.attributeToUser_m.dual()
                        .concatenate(Schema.attributeToUser_ao.signature())
                        .concatenate(Schema.attributeOption_id.signature())
                    )
                ),
                b.complex("verifiedEmails", Schema.verifiedEmail_user.dual(),
                    b.simple("id", Schema.verifiedEmail_id),
                    b.simple("value", Schema.verifiedEmail_value),
                    b.simple("teamId", Schema.verifiedEmail_team.signature().concatenate(Schema.team_id.signature()))
                ),
                b.simple("features", Schema.userFeaturesU.dual()
                    .concatenate(Schema.userFeaturesF.signature())
                    .concatenate(Schema.feature_id.signature())
                ),
                b.simple("eventTypes", Schema.userOnEventTypeU.dual()
                    .concatenate(Schema.userOnEventTypeET.signature())
                    .concatenate(Schema.eventType_id.signature())
                ),
                b.complex("availability", Schema.availability_user.dual(),
                    b.simple("id", Schema.availability_id),
                    b.simple("start", Schema.availability_start),
                    b.simple("end", Schema.availability_end),
                    b.simple("eventTypeId", Schema.availability_eventType.signature().concatenate(Schema.eventType_id.signature()))
                ),
                b.complex("outOfOffice", Schema.outOfOffice_user.dual(),
                    b.simple("id", Schema.outOfOffice_id),
                    b.simple("start", Schema.outOfOffice_start),
                    b.simple("end", Schema.outOfOffice_end),
                    b.simple("toUserId", Schema.outOfOffice_newUser.signature().concatenate(Schema.user_id.signature()))
                )
            )
        );
    }

    public static TestMapping schedule(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.schedule,
            scheduleKind,
            b -> b.root(
                b.simple("id", Schema.schedule_id),
                b.simple("name", Schema.schedule_name),
                b.simple("userId", Schema.schedule_user.signature().concatenate(Schema.user_id.signature())),
                b.simple("eventTypes", Schema.eventType_schedule.dual()
                    .concatenate(Schema.eventType_id.signature())
                ),
                b.complex("availability", Schema.availability_schedule.dual(),
                    b.simple("id", Schema.availability_id),
                    b.simple("start", Schema.availability_start),
                    b.simple("end", Schema.availability_end),
                    b.simple("eventTypeId", Schema.availability_eventType.signature().concatenate(Schema.eventType_id.signature()))
                )
            )
        );
    }

    public static TestMapping eventType(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventType,
            eventTypeKind,
            b -> b.root(
                b.simple("id", Schema.eventType_id),
                b.simple("title", Schema.eventType_title),
                b.simple("description", Schema.eventType_description),
                b.simple("teamId", Schema.eventType_team.signature().concatenate(Schema.team_id.signature())),
                b.simple("ownerId", Schema.eventType_owner.signature().concatenate(Schema.user_id.signature())),
                b.simple("parentId", Schema.eventType_parent.signature().concatenate(Schema.eventType_id.signature())),
                b.simple("scheduleId", Schema.eventType_schedule.signature().concatenate(Schema.schedule_id.signature())),
                b.complex("hosts", Schema.eventHost_eventType.dual(),
                    b.simple("userId", Schema.eventHost_user.signature().concatenate(Schema.user_id.signature())),
                    b.simple("memberId", Schema.eventHost_membership.signature().concatenate(Schema.membership_id.signature()))
                )
            )
        );
    }


    public static TestMapping hostGroup(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.hostGroup,
            hostGroupKind,
            b -> b.root(
                b.simple("id", Schema.hostGroup_id),
                b.simple("eventTypeId", Schema.hostGroup_eventType.signature().concatenate(Schema.eventType_id.signature()))
            )
        );
    }



    public static TestMapping feature(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.feature,
            featureKind,
            b -> b.root(
                b.simple("id", Schema.feature_id),
                b.simple("name", Schema.feature_id)
            )
        );
    }



    public static TestMapping workflow(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflow,
            workflowKind,
            b -> b.root(
                b.simple("id", Schema.workflow_id),
                b.simple("name", Schema.workflow_name),
                b.simple("userId", Schema.workflow_user.signature().concatenate(Schema.user_id.signature())),
                b.simple("teamId", Schema.workflow_team.signature().concatenate(Schema.team_id.signature())),
                b.complex("steps", Schema.workflowStep_workflow.dual(),
                    b.simple("id", Schema.workflowStep_id),
                    b.simple("number", Schema.workflowStep_number),
                    b.simple("action", Schema.workflowStep_action)
                ),
                b.simple("activeOn", Schema.workflowsOnEventTypesWF.dual()
                    .concatenate(Schema.workflowsOnEventTypesET.signature())
                    .concatenate(Schema.eventType_id.signature())
                ),
                b.simple("activeOnTeams", Schema.workflowsOnTeamsWF.dual()
                    .concatenate(Schema.workflowsOnTeamsT.signature())
                    .concatenate(Schema.team_id.signature())
                )
            )
        );
    }



    public static TestMapping booking(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.booking,
            bookingKind,
            b -> b.root(
                b.simple("id", Schema.booking_id),
                b.simple("title", Schema.booking_title),
                b.simple("description", Schema.booking_description),
                b.simple("userId", Schema.booking_user.signature().concatenate(Schema.user_id.signature())),
                b.simple("eventTypeId", Schema.booking_eventType.signature().concatenate(Schema.eventType_id.signature())),
                b.complex("attendees", Schema.attendee_booking.dual(),
                    b.simple("id", Schema.attendee_id),
                    b.simple("email", Schema.attendee_email)
                )
            )
        );
    }

}
