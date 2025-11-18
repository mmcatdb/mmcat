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
                b.simple("id", Schema.team_id),
                b.simple("name", Schema.team_name),
                b.simple("parentId", Schema.team_parent.signature().concatenate(Schema.team_id.signature()))
            )
        );
    }

    public static TestMapping role(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.role,
            roleKind,
            b -> b.root(
                b.simple("id", Schema.role_id),
                b.simple("name", Schema.role_name),
                b.simple("description", Schema.role_description),
                b.simple("teamId", Schema.role_team.signature().concatenate(Schema.team_id.signature()))
            )
        );
    }

    public static TestMapping attribute(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attribute,
            attributeKind,
            b -> b.root(
                b.simple("id", Schema.attribute_id),
                b.simple("name", Schema.attribute_name),
                b.simple("teamId", Schema.attribute_team.signature().concatenate(Schema.team_id.signature()))
            )
        );
    }

    public static TestMapping attributeOption(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attributeOption,
            attributeOptionKind,
            b -> b.root(
                b.simple("id", Schema.attributeOption_id),
                b.simple("value", Schema.attributeOption_value),
                b.simple("attributeId", Schema.attributeOption_attribute.signature().concatenate(Schema.attribute_id.signature()))
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
                b.simple("name", Schema.user_name)
            )
        );
    }

    public static TestMapping membership(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.membership,
            membershipKind,
            b -> b.root(
                b.simple("id", Schema.membership_id),
                b.simple("userId", Schema.membership_user.signature().concatenate(Schema.user_id.signature())),
                b.simple("teamId", Schema.membership_team.signature().concatenate(Schema.team_id.signature())),
                b.simple("accepted", Schema.membership_accepted),
                b.simple("role", Schema.membership_role),
                b.simple("customRoleId", Schema.membership_customRole.signature().concatenate(Schema.role_id.signature()))
            )
        );
    }

    public static TestMapping teamOrgScope(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.teamOrgScope,
            teamOrgScopeKind,
            b -> b.root(
                b.simple("userId", Schema.teamOrgScope_u.signature().concatenate(Schema.user_id.signature())),
                b.simple("teamId", Schema.teamOrgScope_t.signature().concatenate(Schema.team_id.signature()))
            )
        );
    }

    public static TestMapping attributeToUser(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attributeToUser,
            attributeToUserKind,
            b -> b.root(
                b.simple("attributeOptionId", Schema.attributeToUser_ao.signature().concatenate(Schema.attributeOption_id.signature())),
                b.simple("memberId", Schema.attributeToUser_m.signature().concatenate(Schema.membership_id.signature()))
            )
        );
    }

    public static TestMapping verifiedEmail(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.verifiedEmail,
            verifiedEmailKind,
            b -> b.root(
                b.simple("id", Schema.verifiedEmail_id),
                b.simple("value", Schema.verifiedEmail_value),
                b.simple("userId", Schema.verifiedEmail_user.signature().concatenate(Schema.user_id.signature())),
                b.simple("teamId", Schema.verifiedEmail_team.signature().concatenate(Schema.team_id.signature()))
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
                b.simple("userId", Schema.schedule_user.signature().concatenate(Schema.user_id.signature()))
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
                b.simple("scheduleId", Schema.eventType_schedule.signature().concatenate(Schema.schedule_id.signature()))
            )
        );
    }

    public static TestMapping availability(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.availability,
            availabilityKind,
            b -> b.root(
                b.simple("id", Schema.availability_id),
                b.simple("startTime", Schema.availability_start),
                b.simple("endTime", Schema.availability_end),
                b.simple("userId", Schema.availability_user.signature().concatenate(Schema.user_id.signature())),
                b.simple("eventTypeId", Schema.availability_eventType.signature().concatenate(Schema.eventType_id.signature())),
                b.simple("scheduleId", Schema.availability_schedule.signature().concatenate(Schema.schedule_id.signature()))
            )
        );
    }

    public static TestMapping outOfOffice(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.outOfOffice,
            outOfOfficeKind,
            b -> b.root(
                b.simple("id", Schema.outOfOffice_id),
                b.simple("startTime", Schema.outOfOffice_start),
                b.simple("endTime", Schema.outOfOffice_end),
                b.simple("userId", Schema.outOfOffice_user.signature().concatenate(Schema.user_id.signature())),
                b.simple("toUserId", Schema.outOfOffice_newUser.signature().concatenate(Schema.user_id.signature()))
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

    public static TestMapping eventHost(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventHost,
            eventHostKind,
            b -> b.root(
                b.simple("userId", Schema.eventHost_user.signature().concatenate(Schema.user_id.signature())),
                b.simple("memberId", Schema.eventHost_membership.signature().concatenate(Schema.membership_id.signature())),
                b.simple("eventTypeId", Schema.eventHost_eventType.signature().concatenate(Schema.eventType_id.signature())),
                b.simple("scheduleId", Schema.eventHost_schedule.signature().concatenate(Schema.schedule_id.signature())),
                b.simple("hostGroupId", Schema.eventHost_hostGroup.signature().concatenate(Schema.hostGroup_id.signature()))
            )
        );
    }

    public static TestMapping userOnEventType(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.userOnEventType,
            userOnEventTypeKind,
            b -> b.root(
                b.simple("userId", Schema.userOnEventTypeU.signature().concatenate(Schema.user_id.signature())),
                b.simple("eventTypeId", Schema.userOnEventTypeET.signature().concatenate(Schema.eventType_id.signature()))
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

    public static TestMapping userFeatures(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.userFeatures,
            userFeaturesKind,
            b -> b.root(
                b.simple("userId", Schema.userFeaturesU.signature().concatenate(Schema.user_id.signature())),
                b.simple("eventTypeId", Schema.userFeaturesF.signature().concatenate(Schema.feature_id.signature()))
            )
        );
    }

    public static TestMapping teamFeatures(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.teamFeatures,
            teamFeaturesKind,
            b -> b.root(
                b.simple("teamId", Schema.teamFeaturesT.signature().concatenate(Schema.team_id.signature())),
                b.simple("eventTypeId", Schema.teamFeaturesF.signature().concatenate(Schema.feature_id.signature()))
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
                b.simple("teamId", Schema.workflow_team.signature().concatenate(Schema.team_id.signature()))
            )
        );
    }

    public static TestMapping workflowStep(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflowStep,
            workflowStepKind,
            b -> b.root(
                b.simple("id", Schema.workflowStep_id),
                b.simple("number", Schema.workflowStep_number),
                b.simple("action", Schema.workflowStep_action),
                b.simple("workflowId", Schema.workflowStep_workflow.signature().concatenate(Schema.workflow_id.signature()))
            )
        );
    }

    public static TestMapping workflowsOnEventTypes(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflowsOnEventTypes,
            workflowsOnEventTypesKind,
            b -> b.root(
                b.simple("workflowId", Schema.workflowsOnEventTypesWF.signature().concatenate(Schema.workflow_id.signature())),
                b.simple("eventTypeId", Schema.workflowsOnEventTypesET.signature().concatenate(Schema.eventType_id.signature()))
            )
        );
    }

    public static TestMapping workflowsOnTeams(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflowsOnTeams,
            workflowsOnTeamsKind,
            b -> b.root(
                b.simple("workflowId", Schema.workflowsOnTeamsWF.signature().concatenate(Schema.workflow_id.signature())),
                b.simple("eventTypeId", Schema.workflowsOnTeamsT.signature().concatenate(Schema.team_id.signature()))
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
                b.simple("eventTypeId", Schema.booking_eventType.signature().concatenate(Schema.eventType_id.signature()))
            )
        );
    }

    public static TestMapping attendee(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attendee,
            attendeeKind,
            b -> b.root(
                b.simple("id", Schema.attendee_id),
                b.simple("email", Schema.attendee_email),
                b.simple("bookingId", Schema.attendee_booking.signature().concatenate(Schema.booking_id.signature()))
            )
        );
    }

}
