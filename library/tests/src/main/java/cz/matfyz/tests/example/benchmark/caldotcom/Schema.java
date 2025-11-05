package cz.matfyz.tests.example.benchmark.caldotcom;

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

        attendee =              builder.objex("attendee"),
        attendeeId =            builder.objex("attendeeId"),
        attendeeEmail =         builder.objex("attendeeEmail");





    public static final BuilderMorphism
        teamToId =                  builder.morphism(team, teamId),
        teamToName =                builder.morphism(team, teamName),
        teamToParent =              builder.morphism(team, team),

        roleToId =                  builder.morphism(role, roleId),
        roleToName =                builder.morphism(role, roleName),
        roleToDescription =         builder.morphism(role, roleDescription),
        roleToTeam =                builder.morphism(role, team),

        attributeToId =             builder.morphism(attribute, attributeId),
        attributeToName =           builder.morphism(attribute, attributeName),
        attributeToTeam =           builder.morphism(attribute, team),

        attributeOptionToId =       builder.morphism(attributeOption, attributeOptionId),
        attributeOptionToValue =    builder.morphism(attributeOption, attributeOptionValue),
        attributeOptionToAttribute = builder.morphism(attributeOption, attribute),



        userToId =                  builder.morphism(user, userId),
        userToUsername =            builder.morphism(user, userUsername),
        userToName =                builder.morphism(user, userName),

        membershipToId =            builder.morphism(membership, membershipId),
        membershipToAccepted =      builder.morphism(membership, membershipAccepted),
        membershipToRole =          builder.morphism(membership, membershipRole),
        membershipToUser =          builder.morphism(membership, user),
        membershipToTeam =          builder.morphism(membership, team),
        membershipToCustomRole =    builder.morphism(membership, role),

        teamOrgScopeU =             builder.morphism(teamOrgScope, user),
        teamOrgScopeT =             builder.morphism(teamOrgScope, team),

        attributeToUserA =          builder.morphism(attributeToUser, attributeOption),
        attributeToUserU =          builder.morphism(attributeToUser, membership),



        // although unused, its not bound to membership probably because one of them can be null
        verifiedEmailToId =         builder.morphism(verifiedEmail, verifiedEmailId),
        verifiedEmailToValue =      builder.morphism(verifiedEmail, verifiedEmailValue),
        verifiedEmailToUser =       builder.morphism(verifiedEmail, user),
        verifiedEmailToTeam =       builder.morphism(verifiedEmail, team),

        scheduleToId =              builder.morphism(schedule, scheduleId),
        scheduleToName =            builder.morphism(schedule, scheduleName),
        scheduleToUser =            builder.morphism(schedule, user),

        eventTypeToId =             builder.morphism(eventType, eventTypeId),
        eventTypeToTitle =          builder.morphism(eventType, eventTypeTitle),
        eventTypeToDescription =    builder.morphism(eventType, eventTypeDescription),
        eventTypeToOwner =          builder.morphism(eventType, user),
        eventTypeToTeam =           builder.morphism(eventType, team),
        eventTypeToParent =         builder.morphism(eventType, eventType),
        eventTypeToSchedule =       builder.morphism(eventType, schedule),

        availabilityToId =          builder.morphism(availability, availabilityId),
        availabilityToStart =       builder.morphism(availability, availabilityStart),
        availabilityToEnd =         builder.morphism(availability, availabilityEnd),
        availabilityToUser =        builder.morphism(availability, user),
        availabilityToEventType =   builder.morphism(availability, eventType),
        availabilityToSchedule =    builder.morphism(availability, schedule),

        outOfOfficeToId =           builder.morphism(outOfOffice, outOfOfficeId),
        outOfOfficeToStart =        builder.morphism(outOfOffice, outOfOfficeStart),
        outOfOfficeToEnd =          builder.morphism(outOfOffice, outOfOfficeEnd),
        outOfOfficeToUser =         builder.morphism(outOfOffice, user),
        outOfOfficeToNewUser =      builder.morphism(outOfOffice, user),



        hostGroupToId =             builder.morphism(hostGroup, hostGroupId),
        hostGroupToEventType =      builder.morphism(hostGroup, eventType),

        eventHostToUser =           builder.morphism(eventHost, user),
        eventHostToMembership =     builder.morphism(eventHost, membership),
        eventHostToEventType =      builder.morphism(eventHost, eventType),
        eventHostToSchedule =       builder.morphism(eventHost, schedule),
        eventHostToHostGroup =      builder.morphism(eventHost, hostGroup),

        userOnEventTypeU =          builder.morphism(userOnEventType, user),
        userOnEventTypeET =         builder.morphism(userOnEventType, eventType),



        featureToId =               builder.morphism(feature, featureId),
        featureToName =             builder.morphism(feature, featureName),

        userFeaturesU =             builder.morphism(userFeatures, user),
        userFeaturesF =             builder.morphism(userFeatures, feature),

        teamFeaturesT =             builder.morphism(teamFeatures, team),
        teamFeaturesF =             builder.morphism(teamFeatures, feature),



        workflowToId =              builder.morphism(workflow, workflowId),
        workflowToName =            builder.morphism(workflow, workflowName),
        workflowToUser =            builder.morphism(workflow, user),
        workflowToTeam =            builder.morphism(workflow, team),

        workflowStepToId =          builder.morphism(workflowStep, workflowStepId),
        workflowStepToNumber =      builder.morphism(workflowStep, workflowStepNumber),
        workflowStepToAction =      builder.morphism(workflowStep, workflowStepAction),
        workflowStepToWorkflow =    builder.morphism(workflowStep, workflow),

        workflowsOnEventTypesWF =   builder.morphism(workflowsOnEventTypes, workflow),
        workflowsOnEventTypesET =   builder.morphism(workflowsOnEventTypes, eventType),

        workflowsOnTeamsWF =        builder.morphism(workflowsOnTeams, workflow),
        workflowsOnTeamsT =         builder.morphism(workflowsOnTeams, team),



        bookingToId =               builder.morphism(booking, bookingId),
        bookingToTitle =            builder.morphism(booking, bookingTitle),
        bookingToDescription =      builder.morphism(booking, bookingDescription),
        bookingToUser =             builder.morphism(booking, user),
        bookingToEventType =        builder.morphism(booking, eventType),

        attendeeToId =              builder.morphism(attendee, attendeeId),
        attendeeToEmail =           builder.morphism(attendee, attendeeEmail),
        attendeeToBooking =         builder.morphism(attendee, booking);





    static {

        builder
            .ids(team, teamToId)
            .ids(role, roleToId)
            .ids(attribute, attributeToId)
            .ids(attributeOption, attributeOptionToId)

            .ids(user, userToId)
            .ids(membership, membershipToId)
            .ids(teamOrgScope, teamOrgScopeT, teamOrgScopeU)
            .ids(attributeToUser, attributeToUserA, attributeToUserU)

            .ids(verifiedEmail, verifiedEmailToId)
            .ids(schedule, scheduleToId)
            .ids(eventType, eventTypeToId)
            .ids(availability, availabilityToId)
            .ids(outOfOffice, outOfOfficeToId)

            .ids(userOnEventType, userOnEventTypeU, userOnEventTypeET)
            .ids(eventHost, eventHostToUser, eventHostToEventType)
            .ids(hostGroup, hostGroupToId)

            .ids(feature, featureToId)
            .ids(userFeatures, userFeaturesU, userFeaturesF)
            .ids(teamFeatures, teamFeaturesT, teamFeaturesF)

            .ids(workflow, workflowToId)
            .ids(workflowStep, workflowStepToId)
            .ids(workflowsOnEventTypes, workflowsOnEventTypesWF, workflowsOnEventTypesET)
            .ids(workflowsOnTeams, workflowsOnTeamsWF, workflowsOnTeamsT)

            .ids(booking, bookingToId)
            .ids(attendee, attendeeToId);

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
