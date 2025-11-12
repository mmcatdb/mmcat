package cz.matfyz.tests.example.benchmark.caldotcom;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.TestMapping;

public abstract class Neo4j {

    private Neo4j() {}

    public static final Datasource datasource = new Datasource(DatasourceType.neo4j, "neo4j");

    public static final String
        teamKind = "CDCTeam",
        team_parentKind = "CDC_TEAM_PARENT",
        roleKind = "CDCRole",
        team_roleKind = "CDC_TEAM_ROLE",
        attributeKind = "CDCAttribute",
        team_attributeKind = "CDC_TEAM_ATTRIBUTE",
        attributeOptionKind = "CDCAttributeOption",
        attribute_optionKind = "CDC_ATTRIBUTE_OPTION",
        userKind = "CDCUser",
        membershipKind = "CDCMembership",
        membership_userKind = "CDC_MEMBERSHIP_USER",
        membership_teamKind = "CDC_MEMBERSHIP_TEAM",
        membership_roleKind = "CDC_MEMBERSHIP_ROLE",
        team_orgKind = "CDC_TEAM_ORG_SCOPE",
        user_attributeKind = "CDC_USER_TO_ATTRIBUTE",
        verifiedEmailKind = "CDCVerifiedEmail",
        user_emailKind = "CDC_USER_EMAIL",
        team_emailKind = "CDC_TEAM_EMAIL",
        scheduleKind = "CDCSchedule",
        user_scheduleKind = "CDC_USER_SCHEDULE",
        eventTypeKind = "CDCEventType",
        team_eventTypeKind = "CDC_TEAM_EVENT_TYPE",
        eventType_ownerKind = "CDC_EVENT_TYPE_OWNER",
        eventType_parentKind = "CDC_EVENT_TYPE_PARENT",
        schedule_eventTypeKind = "CDC_SCHEDULE_EVENT_TYPE",
        availabilityKind = "CDCAvailability",
        availability_userKind = "CDC_AVAILABILITY_USER",
        availability_eventTypeKind = "CDC_AVAILABILITY_EVENT_TYPE",
        availability_scheduleKind = "CDC_AVAILABILITY_SCHEDULE",
        outOfOfficeKind = "CDC_OUT_OF_OFFICE",
        hostGroupKind = "CDCHostGroup",
        hostGroup_eventTypeKind = "CDC_HOST_GROUP_EVENT_TYPE",
        eventHostKind = "CDCEventHost",
        hostGroup_hostKind = "CDC_HOST_GROUP_HOST",
        host_userKind = "CDC_HOST_USER",
        host_memberKind = "CDC_HOST_MEMBER",
        host_eventTypeKind = "CDC_HOST_EVENT_TYPE",
        userOnEventTypeKind = "CDC_USER_ON_EVENT_TYPE",
        featureKind = "CDCFeature",
        userFeaturesKind = "CDC_USER_FEATURES",
        teamFeaturesKind = "CDC_TEAM_FEATURES",
        workflowKind = "CDCWorkflow",
        workflow_userKind = "CDC_WORKFLOW_USER",
        workflow_teamKind = "CDC_WORKFLOW_TEAM",
        workflowStepKind = "CDCWorkflowStep",
        workflow_stepKind = "CDC_WORKFLOW_STEP",
        workflowsOnEventTypesKind = "CDC_WORKFLOWS_ON_EVENT_TYPES",
        workflowsOnTeamsKind = "CDC_WORKFLOWS_ON_TEAMS",
        bookingKind = "CDCBooking",
        booking_userKind = "CDC_BOOKING_USER",
        booking_eventTypeKind = "CDC_BOOKING_EVENT_TYPE",
        attendeeKind = "CDCAttendee",
        booking_attendeeKind = "CDC_BOOKING_ATTENDEE";



    public static TestMapping team(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.team,
            teamKind,
            b -> b.root(
                b.simple("id", Schema.team_id),
                b.simple("name", Schema.team_name)
            )
        );
    }

    public static TestMapping team_parent(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.team,
            team_parentKind,
            b -> b.root(
                b.complex("_from.CDCTeam", Signature.empty(),
                    b.simple("id", Schema.team_id)
                ),
                b.complex("_to.CDCTeam", Schema.team_parent,
                    b.simple("id", Schema.team_id)
                )
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
                b.simple("description", Schema.role_description)
            )
        );
    }

    public static TestMapping team_role(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.role,
            team_roleKind,
            b -> b.root(
                b.complex("_from.CDCTeam", Schema.role_team,
                    b.simple("id", Schema.team_id)
                ),
                b.complex("_to.CDCRole", Signature.empty(),
                    b.simple("id", Schema.role_id)
                )
            )
        );
    }

    public static TestMapping attribute(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attribute,
            attributeKind,
            b -> b.root(
                b.simple("id", Schema.attribute_id),
                b.simple("name", Schema.attribute_name)
            )
        );
    }

    public static TestMapping team_attribute(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attribute,
            team_attributeKind,
            b -> b.root(
                b.complex("_from.CDCTeam", Schema.attribute_team,
                    b.simple("id", Schema.team_id)
                ),
                b.complex("_to.CDCAttribute", Signature.empty(),
                    b.simple("id", Schema.attribute_id)
                )
            )
        );
    }

    public static TestMapping attributeOption(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attributeOption,
            attributeOptionKind,
            b -> b.root(
                b.simple("id", Schema.attributeOption_id),
                b.simple("value", Schema.attributeOption_value)
            )
        );
    }

    public static TestMapping attribute_option(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attributeOption,
            attribute_optionKind,
            b -> b.root(
                b.complex("_from.CDCAttribute", Schema.attributeOption_attribute,
                    b.simple("id", Schema.attribute_id)
                ),
                b.complex("_to.CDCAttributeOption", Signature.empty(),
                    b.simple("id", Schema.attributeOption_id)
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
                b.simple("accepted", Schema.membership_accepted),
                b.simple("role", Schema.membership_role)
            )
        );
    }

    public static TestMapping membership_user(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.membership,
            membership_userKind,
            b -> b.root(
                b.complex("_from.CDCMembership", Signature.empty(),
                    b.simple("id", Schema.membership_id)
                ),
                b.complex("_to.CDCUser", Schema.membership_user,
                    b.simple("id", Schema.user_id)
                )
            )
        );
    }

    public static TestMapping membership_team(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.membership,
            membership_teamKind,
            b -> b.root(
                b.complex("_from.CDCMembership", Signature.empty(),
                    b.simple("id", Schema.membership_id)
                ),
                b.complex("_to.CDCTeam", Schema.membership_team,
                    b.simple("id", Schema.team_id)
                )
            )
        );
    }

    public static TestMapping membership_role(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.membership,
            membership_roleKind,
            b -> b.root(
                b.complex("_from.CDCMembership", Signature.empty(),
                    b.simple("id", Schema.membership_id)
                ),
                b.complex("_to.CDCRole", Schema.membership_role,
                    b.simple("id", Schema.role_id)
                )
            )
        );
    }

    public static TestMapping team_org(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.teamOrgScope,
            team_orgKind,
            b -> b.root(
                b.complex("_from.CDCUser", Schema.teamOrgScope_u,
                    b.simple("id", Schema.user_id)
                ),
                b.complex("_to.CDCTeam", Schema.teamOrgScope_t,
                    b.simple("id", Schema.team_id)
                )
            )
        );
    }

    public static TestMapping attributeToUser(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attributeToUser,
            user_attributeKind,
            b -> b.root(
                b.complex("_from.CDCMembership", Schema.attributeToUser_m,
                    b.simple("id", Schema.membership_id)
                ),
                b.complex("_to.CDCAttributeOption", Schema.attributeToUser_ao,
                    b.simple("id", Schema.attributeOption_id)
                )
            )
        );
    }

    public static TestMapping verifiedEmail(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.verifiedEmail,
            verifiedEmailKind,
            b -> b.root(
                b.simple("id", Schema.verifiedEmail_id),
                b.simple("value", Schema.verifiedEmail_value)
            )
        );
    }

    public static TestMapping user_email(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.verifiedEmail,
            user_emailKind,
            b -> b.root(
                b.complex("_from.CDCUser", Schema.verifiedEmail_user,
                    b.simple("id", Schema.user_id)
                ),
                b.complex("_to.CDCVerifiedEmail", Signature.empty(),
                    b.simple("id", Schema.verifiedEmail_id)
                )
            )
        );
    }

    public static TestMapping team_email(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.verifiedEmail,
            team_emailKind,
            b -> b.root(
                b.complex("_from.CDCTeam", Schema.verifiedEmail_team,
                    b.simple("id", Schema.team_id)
                ),
                b.complex("_to.CDCVerifiedEmail", Signature.empty(),
                    b.simple("id", Schema.verifiedEmail_id)
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
                b.simple("name", Schema.schedule_name)
            )
        );
    }

    public static TestMapping user_schedule(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.schedule,
            user_scheduleKind,
            b -> b.root(
                b.complex("_from.CDCUser", Schema.schedule_user,
                    b.simple("id", Schema.user_id)
                ),
                b.complex("_to.CDCSchedule", Signature.empty(),
                    b.simple("id", Schema.schedule_id)
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
                b.simple("description", Schema.eventType_description)
            )
        );
    }

    public static TestMapping team_eventType(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventType,
            team_eventTypeKind,
            b -> b.root(
                b.complex("_from.CDCTeam", Schema.eventType_team,
                    b.simple("id", Schema.team_id)
                ),
                b.complex("_to.CDCEventType", Signature.empty(),
                    b.simple("id", Schema.eventType_id)
                )
            )
        );
    }

    public static TestMapping eventType_owner(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventType,
            eventType_ownerKind,
            b -> b.root(
                b.complex("_from.CDCEventType", Signature.empty(),
                    b.simple("id", Schema.eventType_id)
                ),
                b.complex("_to.CDCUser", Schema.eventType_owner,
                    b.simple("id", Schema.user_id)
                )
            )
        );
    }

    public static TestMapping eventType_parent(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventType,
            eventType_parentKind,
            b -> b.root(
                b.complex("_from.CDCEventType", Signature.empty(),
                    b.simple("id", Schema.eventType_id)
                ),
                b.complex("_to.CDCEventType", Schema.eventType_parent,
                    b.simple("id", Schema.eventType_id)
                )
            )
        );
    }

    public static TestMapping schedule_eventType(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventType,
            schedule_eventTypeKind,
            b -> b.root(
                b.complex("_from.CDCSchedule", Schema.eventType_schedule,
                    b.simple("id", Schema.schedule_id)
                ),
                b.complex("_to.CDCEventType", Signature.empty(),
                    b.simple("id", Schema.eventType_id)
                )
            )
        );
    }

    public static TestMapping availability(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.availability,
            availabilityKind,
            b -> b.root(
                b.simple("id", Schema.availability_id),
                b.simple("start", Schema.availability_start),
                b.simple("end", Schema.availability_end)
            )
        );
    }

    public static TestMapping availability_user(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.availability,
            availability_userKind,
            b -> b.root(
                b.complex("_from.CDCAvailability", Signature.empty(),
                    b.simple("id", Schema.availability_id)
                ),
                b.complex("_to.CDCUser", Schema.availability_user,
                    b.simple("id", Schema.user_id)
                )
            )
        );
    }

    public static TestMapping availability_eventType(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.availability,
            availability_eventTypeKind,
            b -> b.root(
                b.complex("_from.CDCAvailability", Signature.empty(),
                    b.simple("id", Schema.availability_id)
                ),
                b.complex("_to.CDCEventType", Schema.availability_eventType,
                    b.simple("id", Schema.eventType_id)
                )
            )
        );
    }

    public static TestMapping availability_schedule(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.availability,
            availability_scheduleKind,
            b -> b.root(
                b.complex("_from.CDCAvailability", Signature.empty(),
                    b.simple("id", Schema.availability_id)
                ),
                b.complex("_to.CDCSchedule", Schema.availability_schedule,
                    b.simple("id", Schema.schedule_id)
                )
            )
        );
    }

    public static TestMapping outOfOffice(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.outOfOffice,
            outOfOfficeKind,
            b -> b.root(
                b.simple("id", Schema.outOfOffice_id),
                b.simple("start", Schema.outOfOffice_start),
                b.simple("end", Schema.outOfOffice_end),
                b.complex("_from.CDCUser", Schema.outOfOffice_user,
                    b.simple("id", Schema.user_id)
                ),
                b.complex("_to.CDCUser", Schema.outOfOffice_newUser,
                    b.simple("id", Schema.user_id)
                )
            )
        );
    }

    public static TestMapping hostGroup(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.hostGroup,
            hostGroupKind,
            b -> b.root(
                b.simple("id", Schema.hostGroup_id)
            )
        );
    }

    public static TestMapping hostGroup_eventType(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.hostGroup,
            hostGroup_eventTypeKind,
            b -> b.root(
                b.complex("_from.CDCHostGroup", Signature.empty(),
                    b.simple("id", Schema.hostGroup_id)
                ),
                b.complex("_to.CDCEventType", Schema.hostGroup_eventType,
                    b.simple("id", Schema.eventType_id)
                )
            )
        );
    }

    public static TestMapping eventHost(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventHost,
            eventHostKind,
            b -> b.root(
                b.simple("id", Schema.eventHost_id)
            )
        );
    }

    public static TestMapping hostGroup_host(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventHost,
            hostGroup_hostKind,
            b -> b.root(
                b.complex("_from.CDCHostGroup", Schema.eventHost_hostGroup,
                    b.simple("id", Schema.hostGroup_id)
                ),
                b.complex("_to.CDCEventHost", Signature.empty(),
                    b.simple("id", Schema.eventHost_id)
                )
            )
        );
    }

    public static TestMapping host_user(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventHost,
            host_userKind,
            b -> b.root(
                b.complex("_from.CDCEventHost", Signature.empty(),
                    b.simple("id", Schema.eventHost_id)
                ),
                b.complex("_to.CDCUser", Schema.eventHost_user,
                    b.simple("id", Schema.user_id)
                )
            )
        );
    }

    public static TestMapping host_member(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventHost,
            host_memberKind,
            b -> b.root(
                b.complex("_from.CDCEventHost", Signature.empty(),
                    b.simple("id", Schema.eventHost_id)
                ),
                b.complex("_to.CDCMember", Schema.eventHost_membership,
                    b.simple("id", Schema.membership_id)
                )
            )
        );
    }

    public static TestMapping host_eventType(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.eventHost,
            host_eventTypeKind,
            b -> b.root(
                b.complex("_from.CDCEventHost", Signature.empty(),
                    b.simple("id", Schema.eventHost_id)
                ),
                b.complex("_to.CDCEventType", Schema.eventHost_eventType,
                    b.simple("id", Schema.eventType_id)
                )
            )
        );
    }

    public static TestMapping userOnEventType(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.userOnEventType,
            userOnEventTypeKind,
            b -> b.root(
                b.complex("_from.CDCUser",  Schema.userOnEventTypeU,
                    b.simple("id", Schema.user_id)
                ),
                b.complex("_to.CDCEventType", Schema.userOnEventTypeET,
                    b.simple("id", Schema.eventType_id)
                )
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
                b.complex("_from.CDCUser", Schema.userFeaturesU,
                    b.simple("id", Schema.user_id)
                ),
                b.complex("_to.CDCFeature", Schema.userFeaturesF,
                    b.simple("id", Schema.feature_id)
                )
            )
        );
    }

    public static TestMapping teamFeatures(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.teamFeatures,
            teamFeaturesKind,
            b -> b.root(
                b.complex("_from.CDCUser", Schema.teamFeaturesT,
                    b.simple("id", Schema.team_id)
                ),
                b.complex("_to.CDCFeature", Schema.teamFeaturesF,
                    b.simple("id", Schema.feature_id)
                )
            )
        );
    }

    public static TestMapping workflow(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflow,
            workflowKind,
            b -> b.root(
                b.simple("id", Schema.workflow_id),
                b.simple("name", Schema.workflow_name)
            )
        );
    }

    public static TestMapping workflow_user(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflow,
            workflow_userKind,
            b -> b.root(
                b.complex("_from.CDCWorkflow", Signature.empty(),
                    b.simple("id", Schema.workflow_id)
                ),
                b.complex("_to.CDCUser", Schema.workflow_user,
                    b.simple("id", Schema.user_id)
                )
            )
        );
    }

    public static TestMapping workflow_team(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflow,
            workflow_teamKind,
            b -> b.root(
                b.complex("_from.CDCWorkflow", Signature.empty(),
                    b.simple("id", Schema.workflow_id)
                ),
                b.complex("_to.CDCTeam", Schema.workflow_team,
                    b.simple("id", Schema.team_id)
                )
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
                b.simple("action", Schema.workflowStep_action)
            )
        );
    }

    public static TestMapping workflow_step(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflowStep,
            workflow_stepKind,
            b -> b.root(
                b.complex("_from.CDCWorkflow", Schema.workflowStep_workflow,
                    b.simple("id", Schema.workflow_id)
                ),
                b.complex("_to.CDCWorkflowStep", Signature.empty(),
                    b.simple("id", Schema.workflowStep_id)
                )
            )
        );
    }

    public static TestMapping workflowsOnEventTypes(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflowsOnEventTypes,
            workflowsOnEventTypesKind,
            b -> b.root(
                b.complex("_from.CDCWorkflow", Schema.workflowsOnEventTypesWF,
                    b.simple("id", Schema.workflow_id)
                ),
                b.complex("_to.CDCEventType", Schema.workflowsOnEventTypesET,
                    b.simple("id", Schema.eventType_id)
                )
            )
        );
    }


    public static TestMapping workflowsOnTeams(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.workflowsOnTeams,
            workflowsOnTeamsKind,
            b -> b.root(
                b.complex("_from.workflow", Schema.workflowsOnTeamsWF,
                    b.simple("id", Schema.workflow_id)
                ),
                b.complex("_to.team", Schema.workflowsOnTeamsT,
                    b.simple("id", Schema.team_id)
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
                b.simple("description", Schema.booking_description)
            )
        );
    }

    public static TestMapping booking_user(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.booking,
            booking_userKind,
            b -> b.root(
                b.complex("_from.CDCBooking", Signature.empty(),
                    b.simple("id", Schema.booking_id)
                ),
                b.complex("_to.CDCUser", Schema.booking_user,
                    b.simple("id", Schema.user_id)
                )
            )
        );
    }

    public static TestMapping booking_eventType(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.booking,
            booking_eventTypeKind,
            b -> b.root(
                b.complex("_from.CDCBooking", Signature.empty(),
                    b.simple("id", Schema.booking_id)
                ),
                b.complex("_to.CDCEventType", Schema.booking_eventType,
                    b.simple("id", Schema.eventType_id)
                )
            )
        );
    }

    public static TestMapping attendee(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attendee,
            attendeeKind,
            b -> b.root(
                b.simple("id", Schema.attendee_id),
                b.simple("email", Schema.attendee_email)
            )
        );
    }

    public static TestMapping booking_attendee(SchemaCategory schema) {
        return new TestMapping(datasource, schema,
            Schema.attendee,
            booking_attendeeKind,
            b -> b.root(
                b.complex("_from.CDCBooking", Schema.attendee_booking,
                    b.simple("id", Schema.booking_id)
                ),
                b.complex("_to.CDCAttendee", Signature.empty(),
                    b.simple("id", Schema.attendee_id)
                )
            )
        );
    }

}
