package cz.matfyz.tests.example.caldotcom;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaBuilder;
import cz.matfyz.core.schema.SchemaBuilder.BuilderMorphism;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObjex;
import cz.matfyz.core.schema.SchemaCategory;

public abstract class Schema {

    public static final String schemaLabel = "Benchmark Yelp Schema";

    private static final SchemaBuilder builder = new SchemaBuilder();

    // Keys

    public static final BuilderObjex eventType =            builder.objex("eventType");
    public static final BuilderObjex eventTypeId =          builder.objex("eventTypeId");
    public static final BuilderObjex eventTypeTitle =       builder.objex("eventTypeTitle");
    public static final BuilderObjex eventTypeDescription = builder.objex("eventTypeDescription");

    public static final BuilderObjex user =                 builder.objex("user");
    public static final BuilderObjex userId =               builder.objex("userId");
    public static final BuilderObjex userUsername =         builder.objex("userUsername");
    public static final BuilderObjex userName =             builder.objex("userName");

    public static final BuilderObjex userOnEventType =      builder.objex("userOnEventType");

    public static final BuilderObjex team =                 builder.objex("team");
    public static final BuilderObjex teamId =               builder.objex("teamId");
    public static final BuilderObjex teamName =             builder.objex("teamName");

    public static final BuilderObjex teamOrgScope =         builder.objex("teamOrgScope");

    public static final BuilderObjex membership =           builder.objex("membership");
    public static final BuilderObjex membershipId =         builder.objex("membershipId");
    public static final BuilderObjex membershipAccepted =   builder.objex("membershipAccepted");
    public static final BuilderObjex membershipRole =       builder.objex("membershipRole");

    public static final BuilderObjex booking =              builder.objex("booking");
    public static final BuilderObjex bookingId =            builder.objex("bookingId");
    public static final BuilderObjex bookingTitle =         builder.objex("bookingTitle");
    public static final BuilderObjex bookingDescription =   builder.objex("bookingDescription");

    public static final BuilderObjex attendee =             builder.objex("attendee");
    public static final BuilderObjex attendeeId =           builder.objex("attendeeId");
    public static final BuilderObjex attendeeEmail =        builder.objex("attendeeEmail");

    public static final BuilderObjex workflow =             builder.objex("workflow");
    public static final BuilderObjex workflowId =           builder.objex("workflowId");
    public static final BuilderObjex workflowName =         builder.objex("workflowName");

    public static final BuilderObjex workflowsOnEventTypes = builder.objex("workflowsOnEventTypes");

    // public static final BuilderObjex workflowsOnTeams =     builder.objex("workflowsOnTeams");

    public static final BuilderObjex role =                 builder.objex("role");
    public static final BuilderObjex roleId =               builder.objex("roleId");
    public static final BuilderObjex roleName =             builder.objex("roleName");
    public static final BuilderObjex roleDescription =      builder.objex("roleDescription");


    // Morphisms

    public static final BuilderMorphism eventTypeToId =         builder.morphism(eventType, eventTypeId);
    public static final BuilderMorphism eventTypeToTitle =      builder.morphism(eventType, eventTypeTitle);
    public static final BuilderMorphism eventTypeToDescription = builder.morphism(eventType, eventTypeDescription);
    public static final BuilderMorphism eventTypeToOwner =      builder.morphism(eventType, user);
    public static final BuilderMorphism eventTypeToTeam =       builder.morphism(eventType, team);
    public static final BuilderMorphism eventTypeToParent =     builder.morphism(eventType, eventType);

    public static final BuilderMorphism userToId =              builder.morphism(user, userId);
    public static final BuilderMorphism userToUsername =        builder.morphism(user, userUsername);
    public static final BuilderMorphism userToName =            builder.morphism(user, userName);

    public static final BuilderMorphism userOnEventTypeU =      builder.morphism(userOnEventType, user);
    public static final BuilderMorphism userOnEventTypeET =     builder.morphism(userOnEventType, eventType);

    public static final BuilderMorphism teamToId =              builder.morphism(team, teamId);
    public static final BuilderMorphism teamToName =            builder.morphism(team, teamName);
    public static final BuilderMorphism teamToParent =          builder.morphism(team, team);

    public static final BuilderMorphism teamOrgScopeU =         builder.morphism(teamOrgScope, user);
    public static final BuilderMorphism teamOrgScopeT =         builder.morphism(teamOrgScope, team);

    public static final BuilderMorphism membershipToId =        builder.morphism(membership, membershipId);
    public static final BuilderMorphism membershipToAccepted =  builder.morphism(membership, membershipAccepted);
    public static final BuilderMorphism membershipToRole =      builder.morphism(membership, membershipRole);
    public static final BuilderMorphism membershipToUser =      builder.morphism(membership, user);
    public static final BuilderMorphism membershipToTeam =      builder.morphism(membership, team);
    public static final BuilderMorphism membershipToCustomRole = builder.morphism(membership, role);


    public static final BuilderMorphism attendeeToId =          builder.morphism(attendee, attendeeId);
    public static final BuilderMorphism attendeeToEmail =       builder.morphism(attendee, attendeeEmail);
    public static final BuilderMorphism attendeeToBooking =     builder.morphism(attendee, booking);

    public static final BuilderMorphism bookingToId =           builder.morphism(booking, bookingId);
    public static final BuilderMorphism bookingToTitle =        builder.morphism(booking, bookingTitle);
    public static final BuilderMorphism bookingToDescription =  builder.morphism(booking, bookingDescription);
    public static final BuilderMorphism bookingToUser =         builder.morphism(booking, user);
    public static final BuilderMorphism bookingToEventType =    builder.morphism(booking, eventType);

    public static final BuilderMorphism workflowsOnEventTypesWF = builder.morphism(workflowsOnEventTypes, workflow);
    public static final BuilderMorphism workflowsOnEventTypesET = builder.morphism(workflowsOnEventTypes, eventType);

    // public static final BuilderMorphism workflowsOnTeamsWF =    builder.morphism(workflowsOnTeams, workflow);
    // public static final BuilderMorphism workflowsOnTeamsT =     builder.morphism(workflowsOnTeams, team);

    public static final BuilderMorphism workflowToId =          builder.morphism(workflow, workflowId);
    public static final BuilderMorphism workflowToName =        builder.morphism(workflow, workflowName);
    public static final BuilderMorphism workflowToUser =        builder.morphism(workflow, user);
    public static final BuilderMorphism workflowToTeam =        builder.morphism(workflow, team);

    public static final BuilderMorphism roleToId =              builder.morphism(role, roleId);
    public static final BuilderMorphism roleToName =            builder.morphism(role, roleName);
    public static final BuilderMorphism roleToDescription =     builder.morphism(role, roleDescription);
    public static final BuilderMorphism roleToTeam =            builder.morphism(role, team);

    // Ids

    static {

        builder
            .ids(eventType, eventTypeToId)
            .ids(user, userToId)
            .ids(team, teamToId)
            .ids(membership, membershipToId)
            .ids(attendee, attendeeToId)
            .ids(booking, bookingToId)
            .ids(workflow, workflowToId)
            .ids(role, roleToId)

            .ids(userOnEventType, userOnEventTypeU, userOnEventTypeET)
            .ids(teamOrgScope, teamOrgScopeT, teamOrgScopeU)
            .ids(workflowsOnEventTypes, workflowsOnEventTypesWF, workflowsOnEventTypesET)
            .ids(workflowsOnTeams, workflowsOnTeamsWF, workflowsOnTeamsT);

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
