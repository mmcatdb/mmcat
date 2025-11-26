package cz.matfyz.tests.example.benchmark.caldotcom;

import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;
import cz.matfyz.core.schema.SchemaCategory;

public abstract class Schema {

    public static final String schemaLabel = "Benchmark Yelp Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();



    public static final BuilderObjex
        team =                  builder.objex("team"),
        teamId =                builder.objex("teamId"),
        teamName =              builder.objex("teamName"),

        teamToParent =            builder.objex("teamToParent"),

        role =                  builder.objex("role"),
        roleId =                builder.objex("roleId"),
        roleName =              builder.objex("roleName"),
        roleDescription =       builder.objex("roleDescription"),

        attribute =             builder.objex("attribute"),
        attributeId =           builder.objex("attributeId"),
        attributeName =         builder.objex("attributeName"),

        attributeOption =       builder.objex("attributeOption"),
        attributeOptionId =     builder.objex("attributeOptionId"),
        attributeOptionValue =  builder.objex("attributeOptionValue"),



        user =                  builder.objex("user"),
        userId =                builder.objex("userId"),
        userUsername =          builder.objex("userUsername"),
        userName =              builder.objex("userName"),

        membership =            builder.objex("membership"),
        membershipId =          builder.objex("membershipId"),
        membershipAccepted =    builder.objex("membershipAccepted"),
        membershipRole =        builder.objex("membershipRole"),

        teamOrgScope =          builder.objex("teamOrgScope"),

        attributeToUser =       builder.objex("attributeToUser"),



        // has a 1:n relationship to user
        verifiedEmail =         builder.objex("verifiedEmail"),
        verifiedEmailId =       builder.objex("verifiedEmailId"),
        verifiedEmailValue =    builder.objex("verifiedEmailValue"),

        schedule =              builder.objex("schedule"),
        scheduleId =            builder.objex("scheduleId"),
        scheduleName =          builder.objex("scheduleName"),

        eventType =             builder.objex("eventType"),
        eventTypeId =           builder.objex("eventTypeId"),
        eventTypeTitle =        builder.objex("eventTypeTitle"),
        eventTypeDescription =  builder.objex("eventTypeDescription"),

        eventTypeToParent =       builder.objex("eventTypeToParent"),

        availability =          builder.objex("availability"),
        availabilityId =        builder.objex("availabilityId"),
        availabilityStart =     builder.objex("availabilityStart"),
        availabilityEnd =       builder.objex("availabilityEnd"),

        // redirects among users
        outOfOffice =           builder.objex("outOfOffice"),
        outOfOfficeId =         builder.objex("outOfOfficeId"),
        outOfOfficeStart =      builder.objex("outOfOfficeStart"),
        outOfOfficeEnd =        builder.objex("outOfOfficeEnd"),



        // not just hosting, but (presumably) access & control
        userOnEventType =       builder.objex("userOnEventType"),

        hostGroup =             builder.objex("hostGroup"),
        hostGroupId =           builder.objex("hostGroupId"),

        // also user on event type, but only in hosting
        eventHost =             builder.objex("eventHost"),
        eventHostId =           builder.objex("eventHostId"),



        // some functionality, limited count, managed globally
        feature =               builder.objex("feature"),
        featureId =             builder.objex("featureId"),
        featureName =           builder.objex("featureName"),

        userFeatures =          builder.objex("userFeatures"),

        teamFeatures =          builder.objex("teamFeatures"),



        workflow =              builder.objex("workflow"),
        workflowId =            builder.objex("workflowId"),
        workflowName =          builder.objex("workflowName"),

        workflowStep =          builder.objex("workflowStep"),
        workflowStepId =        builder.objex("workflowStepId"),
        workflowStepNumber =    builder.objex("workflowStepNumber"),
        workflowStepAction =    builder.objex("workflowStepAction"),

        workflowsOnEventTypes = builder.objex("workflowsOnEventTypes"),

        // which workflow is active on which team
        workflowsOnTeams =      builder.objex("workflowsOnTeams"),



        booking =               builder.objex("booking"),
        bookingId =             builder.objex("bookingId"),
        bookingTitle =          builder.objex("bookingTitle"),
        bookingDescription =    builder.objex("bookingDescription"),
        bookingTime =           builder.objex("bookingTime"),

        attendee =              builder.objex("attendee"),
        attendeeId =            builder.objex("attendeeId"),
        attendeeEmail =         builder.objex("attendeeEmail");





    public static final BuilderMorphism
        team_id =                   builder.morphism(team, teamId, 1),
        team_name =                 builder.morphism(team, teamName, 2),

        teamToParent_child =        builder.morphism(teamToParent, team, 3),
        teamToParent_parent =       builder.morphism(teamToParent, team, 4),

        role_id =                   builder.morphism(role, roleId, 11),
        role_name =                 builder.morphism(role, roleName, 12),
        role_description =          builder.morphism(role, roleDescription, 13),
        role_team =                 builder.morphism(role, team, 14),

        attribute_id =              builder.morphism(attribute, attributeId, 21),
        attribute_name =            builder.morphism(attribute, attributeName, 22),
        attribute_team =            builder.morphism(attribute, team, 23),

        attributeOption_id =        builder.morphism(attributeOption, attributeOptionId, 31),
        attributeOption_value =     builder.morphism(attributeOption, attributeOptionValue, 32),
        attributeOption_attribute = builder.morphism(attributeOption, attribute, 33),



        user_id =                   builder.morphism(user, userId, 41),
        user_username =             builder.morphism(user, userUsername, 42),
        user_name =                 builder.morphism(user, userName, 43),

        membership_id =             builder.morphism(membership, membershipId, 51),
        membership_accepted =       builder.morphism(membership, membershipAccepted, 52),
        membership_role =           builder.morphism(membership, membershipRole, 53),
        membership_user =           builder.morphism(membership, user, 54),
        membership_team =           builder.morphism(membership, team, 55),
        membership_customRole =     builder.morphism(membership, role, 56),

        teamOrgScope_u =            builder.morphism(teamOrgScope, user, 61),
        teamOrgScope_t =            builder.morphism(teamOrgScope, team, 62),

        attributeToUser_ao =        builder.morphism(attributeToUser, attributeOption, 71),
        attributeToUser_m =         builder.morphism(attributeToUser, membership, 72),



        // although unused, its not bound to membership probably because one of them can be null
        verifiedEmail_id =          builder.morphism(verifiedEmail, verifiedEmailId, 81),
        verifiedEmail_value =       builder.morphism(verifiedEmail, verifiedEmailValue, 82),
        verifiedEmail_user =        builder.morphism(verifiedEmail, user, 83),
        verifiedEmail_team =        builder.morphism(verifiedEmail, team, 84),

        schedule_id =               builder.morphism(schedule, scheduleId, 91),
        schedule_name =             builder.morphism(schedule, scheduleName, 92),
        schedule_user =             builder.morphism(schedule, user, 93),

        eventType_id =              builder.morphism(eventType, eventTypeId, 101),
        eventType_title =           builder.morphism(eventType, eventTypeTitle, 102),
        eventType_description =     builder.morphism(eventType, eventTypeDescription, 103),
        eventType_owner =           builder.morphism(eventType, user, 104),
        eventType_team =            builder.morphism(eventType, team, 105),
        eventType_schedule =        builder.morphism(eventType, schedule, 106),

        eventTypeToParent_child =     builder.morphism(eventTypeToParent, eventType, 107),
        eventTypeToParent_parent =    builder.morphism(eventTypeToParent, eventType, 108),

        availability_id =           builder.morphism(availability, availabilityId, 111),
        availability_start =        builder.morphism(availability, availabilityStart, 112),
        availability_end =          builder.morphism(availability, availabilityEnd, 113),
        availability_user =         builder.morphism(availability, user, 114),
        availability_eventType =    builder.morphism(availability, eventType, 115),
        availability_schedule =     builder.morphism(availability, schedule, 116),

        outOfOffice_id =            builder.morphism(outOfOffice, outOfOfficeId, 121),
        outOfOffice_start =         builder.morphism(outOfOffice, outOfOfficeStart, 122),
        outOfOffice_end =           builder.morphism(outOfOffice, outOfOfficeEnd, 123),
        outOfOffice_user =          builder.morphism(outOfOffice, user, 124),
        outOfOffice_newUser =       builder.morphism(outOfOffice, user, 125),



        hostGroup_id =              builder.morphism(hostGroup, hostGroupId, 131),
        hostGroup_eventType =       builder.morphism(hostGroup, eventType, 132),

        eventHost_id =              builder.morphism(eventHost, eventHostId, 141),
        eventHost_user =            builder.morphism(eventHost, user, 142),
        eventHost_membership =      builder.morphism(eventHost, membership, 143),
        eventHost_eventType =       builder.morphism(eventHost, eventType, 144),
        eventHost_schedule =        builder.morphism(eventHost, schedule, 145),
        eventHost_hostGroup =       builder.morphism(eventHost, hostGroup, 146),

        userOnEventTypeU =          builder.morphism(userOnEventType, user, 151),
        userOnEventTypeET =         builder.morphism(userOnEventType, eventType, 152),



        feature_id =                builder.morphism(feature, featureId, 161),
        feature_name =              builder.morphism(feature, featureName, 162),

        userFeaturesU =             builder.morphism(userFeatures, user, 171),
        userFeaturesF =             builder.morphism(userFeatures, feature, 172),

        teamFeaturesT =             builder.morphism(teamFeatures, team, 181),
        teamFeaturesF =             builder.morphism(teamFeatures, feature, 182),



        workflow_id =               builder.morphism(workflow, workflowId, 191),
        workflow_name =             builder.morphism(workflow, workflowName, 192),
        workflow_user =             builder.morphism(workflow, user, 193),
        workflow_team =             builder.morphism(workflow, team, 194),

        workflowStep_id =           builder.morphism(workflowStep, workflowStepId, 201),
        workflowStep_number =       builder.morphism(workflowStep, workflowStepNumber, 202),
        workflowStep_action =       builder.morphism(workflowStep, workflowStepAction, 203),
        workflowStep_workflow =     builder.morphism(workflowStep, workflow, 204),

        workflowsOnEventTypesWF =   builder.morphism(workflowsOnEventTypes, workflow, 211),
        workflowsOnEventTypesET =   builder.morphism(workflowsOnEventTypes, eventType, 212),

        workflowsOnTeamsWF =        builder.morphism(workflowsOnTeams, workflow, 221),
        workflowsOnTeamsT =         builder.morphism(workflowsOnTeams, team, 222),



        booking_id =                builder.morphism(booking, bookingId, 231),
        booking_title =             builder.morphism(booking, bookingTitle, 232),
        booking_description =       builder.morphism(booking, bookingDescription, 233),
        booking_user =              builder.morphism(booking, user, 234),
        booking_eventType =         builder.morphism(booking, eventType, 235),
        booking_time =              builder.morphism(booking, bookingTime, 235),

        attendee_id =               builder.morphism(attendee, attendeeId, 241),
        attendee_email =            builder.morphism(attendee, attendeeEmail, 242),
        attendee_booking =          builder.morphism(attendee, booking, 243);




    public static final Signature
        team_parent = teamToParent_child.dual().concatenate(teamToParent_parent.signature()),
        eventType_parent = eventTypeToParent_child.dual().concatenate(eventTypeToParent_parent.signature());


    static {

        builder
            .ids(team, team_id)
            .ids(role, role_id)
            .ids(attribute, attribute_id)
            .ids(attributeOption, attributeOption_id)

            .ids(user, user_id)
            .ids(membership, membership_id)
            .ids(teamOrgScope, teamOrgScope_t, teamOrgScope_u)
            .ids(attributeToUser, attributeToUser_ao, attributeToUser_m)

            .ids(verifiedEmail, verifiedEmail_id)
            .ids(schedule, schedule_id)
            .ids(eventType, eventType_id)
            .ids(availability, availability_id)
            .ids(outOfOffice, outOfOffice_id)

            .ids(userOnEventType, userOnEventTypeU, userOnEventTypeET)
            .ids(eventHost, eventHost_user, eventHost_eventType)
            .ids(hostGroup, hostGroup_id)

            .ids(feature, feature_id)
            .ids(userFeatures, userFeaturesU, userFeaturesF)
            .ids(teamFeatures, teamFeaturesT, teamFeaturesF)

            .ids(workflow, workflow_id)
            .ids(workflowStep, workflowStep_id)
            .ids(workflowsOnEventTypes, workflowsOnEventTypesWF, workflowsOnEventTypesET)
            .ids(workflowsOnTeams, workflowsOnTeamsWF, workflowsOnTeamsT)

            .ids(booking, booking_id)
            .ids(attendee, attendee_id);

    }

    /**
     * Create new full schema category.
     */
    public static SchemaCategory newSchema() {
        return builder.build();
    }

    public static MetadataCategory newMetadata(SchemaCategory schema) {
        return builder.buildMetadata(schema);
    }

    private Schema() {}

}
