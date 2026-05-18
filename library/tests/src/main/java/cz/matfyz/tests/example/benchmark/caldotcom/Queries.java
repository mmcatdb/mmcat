package cz.matfyz.tests.example.benchmark.caldotcom;

import java.util.List;

@SuppressWarnings("unused")
public class Queries {

    // List of valid (non-crashing) ids of queries
    private static final List<Integer> genIds = List.of(
        0, 1, 2, 3, 7, 15, 17, 22, 24, 25, 29, 30, 35, 39, 40, 48, 49, 50, 52, 53, 56, 61, 62, 63, 64, 65
    );
    /** All queries, marked whether generated or hand-made (not that it matters much) */
    private static final String[] genQueries = {

        // region prompt1:Simple

        // 0
        """
SELECT {
    ?role id ?id ;
          name ?name ;
          description ?desc ;
          teamId ?teamId .
}
WHERE {
    ?role 11 ?id ;
          12 ?name ;
          13 ?desc ;
          14/1 ?teamId .
    FILTER(?id = "&11")
}
        """,

        """
SELECT {
    ?workflow name ?wfName ;
           number ?stepNum ;
           action ?action .
}
WHERE {
    ?workflow 192 ?wfName ;
              -204 ?steps .

    ?steps 202 ?stepNum ;
           203 ?action .

    FILTER(?wfName = "&192")
}
        """,

        """
SELECT {
    ?vEmail email ?emailValue ;
            ownerId ?userId .
}
WHERE {
    ?vEmail 82 ?emailValue ;
            83/41 ?userId .

    FILTER(?emailValue = "&82")
}
        """,

        """
SELECT {
    ?user userId ?uId ;
          orgFeatureIds ?featureIds .
}
WHERE {
    ?user 41 ?uId ;
          -61/62/-181/182/162 ?featureNames ;
          -61/62/-181/182/161 ?featureIds .

    FILTER(?uId = "&41")
}
        """,

        """
SELECT {
    ?booking title ?bTitle ;
             attendeeEmails ?attEmails ;
             hostAvailabilityStarts ?hostAvailStarts .
}
WHERE {
    ?booking 232 ?bTitle ;
             -243/242 ?attEmails ;
             234/-93/-116/112 ?hostAvailStarts .

    FILTER(?bTitle = "&232")
}
        """,

        """
SELECT {
    ?team teamName ?tName ;
          memberOOOStarts ?oooStarts ;
          memberOOOEnds ?oooEnds .
}
WHERE {
    ?team 2 ?tName ;
          1 ?tId ;
          -55/54/-124/122 ?oooStarts ;
          -55/54/-124/123 ?oooEnds .

    FILTER(?oooEnds < "&123")
    FILTER(?tId = "&1")
}
        """,

        """
SELECT {
    ?eventType title ?title ;
               hostId ?hostId .
}
WHERE {
    ?eventType 106 ?schedule ;
               102 ?title ;
               104/41 ?hostId .

    ?schedule 91 ?scheduleId .

    FILTER(?scheduleId = "&91")
}
        """,

        """
SELECT {
    ?role id ?roleId ;
        name ?roleName ;
        peerRoleIds ?peerId .
}
WHERE {
    ?role 11 ?roleId ;
        12 ?roleName ;
        14/-14/11 ?peerId .

    FILTER(?roleId = "&11")
}
        """,

        // endregion
        // region prompt2:Joins

        """
SELECT {
    ?user username ?username ;
          teamNames ?teamName ;
          roleNames ?roleName ;
          roleTeams ?roleTeamId ;
          enabledFeatures ?featureName .
}
WHERE {
    # Match specific User by ID
    ?user 41 ?userId ;
          42 ?username .

    # Path: User <- Membership -> Team -> TeamName
    ?user -54/55/2 ?teamName .

    # Path: User <- Membership -> Role -> RoleName
    ?user -54/56/12 ?roleName .
    ?user -54/56/14/1 ?roleTeamId .

    # Path: User <- UserFeatures -> Feature -> FeatureName
    ?user -171/172/162 ?featureName .

    FILTER(?userId = "&41")
}
        """,

        // 10
        """
SELECT {
    ?eventType title ?title ;
               description ?desc ;
               hostUsernames ?hostUser ;
               triggeredWorkflows ?workflowName .
}
WHERE {
    # Match Event Type by ID
    ?eventType 101 ?evtId ;
               102 ?title ;
               103 ?desc .

    # Path: EventType <- EventHost -> User -> Username
    ?eventType -144/142/42 ?hostUser .

    # Path: EventType <- WorkflowsOnEventTypes -> Workflow -> WorkflowName
    ?eventType -212/211/192 ?workflowName .

    FILTER(?evtId = "&101")
}
        """,

        """
SELECT {
    ?booking title ?bookingTitle ;
             attendeeEmails ?email ;
             hostTeamFeatures ?featureName .
}
WHERE {
    # Match Booking by ID
    ?booking 231 ?bookingId ;
             232 ?bookingTitle .

    # Path: Booking <- Attendee -> Email
    ?booking -243/242 ?email .

    # Path: Booking -> EventType -> Team <- TeamFeatures -> Feature -> Name
    # (Deep traversal across 5 objects)
    ?booking 235/105/-181/182/162 ?featureName .

    FILTER(?bookingId = "&231")
}
        """,

        """
SELECT {
    ?team name ?teamName ;
          definedAttributes ?attrName ;
          orgScopeUsers ?scopeUsername .
}
WHERE {
    # Match Team by ID
    ?team 1 ?teamId ;
          2 ?teamName .

    # Path: Team <- Attribute -> AttributeName
    ?team -23/22 ?attrName .

    # Path: Team <- TeamOrgScope -> User -> Username
    ?team -62/61/42 ?scopeUsername .

    FILTER(?teamId = "&1")
}
        """,

        """
SELECT {
    ?workflow name ?wName ;
              usedByTeams ?teamName ;
              stepActions ?action .
}
WHERE {
    # Match Workflow by ID
    ?workflow 191 ?wId ;
              192 ?wName .

    # Path: Workflow <- WorkflowsOnTeams -> Team -> TeamName
    ?workflow -221/222/2 ?teamName .

    # Path: Workflow <- WorkflowStep -> WorkflowStepAction
    ?workflow -204/203 ?action .

    FILTER(?wId = "&191")
}
        """,

        """
SELECT {
    ?team name ?teamName ;
          activeFeatures ?featureName ;
          activeWorkflows ?workflowName .
}
WHERE {
    # Match Team by ID
    ?team 1 ?teamId ;
          2 ?teamName .

    # Path: Team <- TeamFeatures -> Feature -> Name
    ?team -181/182/162 ?featureName .

    # Path: Team <- WorkflowsOnTeams -> Workflow -> Name
    ?team -222/221/192 ?workflowName .

    FILTER(?teamId = "&1")
}
        """,

        """
SELECT {
    ?user username ?handle ;
          oooStarts ?oooStart ;
          scheduleNames ?schedName .
}
WHERE {
    # Match User by ID
    ?user 41 ?userId ;
          42 ?handle .

    # Path: User <- OutOfOffice -> Start
    ?user -124/122 ?oooStart .

    # Path: User <- Schedule -> Name
    ?user -93/92 ?schedName .

    FILTER(?userId = "&41")
}
        """,

        """
SELECT {
    ?eventType title ?etTitle ;
               hostUsernames ?hostUser ;
               assignedUsernames ?assignedUser .
}
WHERE {
    # Match Event Type by ID
    ?eventType 101 ?etId ;
               102 ?etTitle .

    # Path: EventType <- EventHost -> User -> Username
    ?eventType -144/142/42 ?hostUser .

    # Path: EventType <- UserOnEventType -> User -> Username
    ?eventType -152/151/42 ?assignedUser .

    FILTER(?etId = "&101")
}
        """,

        """
SELECT {
    ?booking title ?bTitle ;
             attendeeEmails ?attEmail ;
             hostEmails ?hostEmail .
}
WHERE {
    # Match Booking by ID
    ?booking 231 ?bId ;
             232 ?bTitle .

    # Path: Booking <- Attendee -> Email
    ?booking -243/242 ?attEmail .

    # Path: Booking -> EventType -> User (Owner) <- VerifiedEmail -> Value
    ?booking 235/104/-83/82 ?hostEmail .

    FILTER(?bId = "&231")
}
        """,

        """
SELECT {
    ?role name ?roleName ;
          teamName ?teamName ;
          holders ?username .
}
WHERE {
    # Match Role by ID
    ?role 11 ?roleId ;
          12 ?roleName .

    # Path: Role -> Team -> Name
    ?role 14/2 ?teamName .

    # Path: Role <- Membership -> User -> Username
    ?role -56/54/42 ?username .

    FILTER(?roleId = "&11")
}
        """,

        """
SELECT {
    ?workflow name ?wName ;
              appliedToTeams ?teamName ;
              appliedToEventTypes ?etTitle .
}
WHERE {
    # Match Workflow by ID
    ?workflow 191 ?wId ;
              192 ?wName .

    # Path: Workflow <- WorkflowsOnTeams -> Team -> Name
    ?workflow -221/222/2 ?teamName .

    # Path: Workflow <- WorkflowsOnEventTypes -> EventType -> Title
    ?workflow -211/212/102 ?etTitle .

    FILTER(?wId = "&191")
}
        """,

        // 20
        """
SELECT {
    ?membership accepted ?isAccepted ;
                memberName ?name ;
                customAttributeValues ?attrVal ;
                attrTeam ?attrTeam .
}
WHERE {
    # Match Membership by ID
    ?membership 51 ?mId ;
                52 ?isAccepted .

    # Path: Membership -> User -> Name
    ?membership 54/43 ?name .

    # Path: Membership <- AttributeToUser -> AttributeOption -> Value
    ?membership -72/71/32 ?attrVal ;
                -72/71/33/23/1 ?attrTeam .

    FILTER(?mId = "&51")
}
        """,

        """
SELECT {
    ?team name ?teamName ;
        memberUsernames ?username ;
        memberRoles ?roleId ;
        memberEmails ?email .
}
WHERE {
    ?team 1 ?teamId ;
        2 ?teamName ;
        -55/54/42 ?username ;   # Team <- Membership -> User -> Username
        -55/56/11 ?roleId ;   # Team <- Membership -> Role -> RoleId
        -55/54/-83/82 ?email .

    FILTER(?teamId = "&1")
}
        """,

        """
SELECT {
    ?user username ?handle ;
          bookingTitles ?title ;
          allAttendeeEmails ?email .
}
WHERE {
    ?user 42 ?handle ;
          -234/232 ?title ;          # User <- Booking -> Title
          -234/-243/242 ?email .     # User <- Booking <- Attendee -> Email

    FILTER(?handle = "&42")
}
        """,

        """
SELECT {
    ?feature name ?fName ;
          enabledOnTeams ?teamName ;
          enabledOnUsers ?userName .
}
WHERE {
    ?feature 162 ?fName ;
          -182/181/2 ?teamName ;   # Feature <- TeamFeatures -> Team -> Name
          -172/171/43 ?userName .  # Feature <- UserFeatures -> User -> Name

    FILTER(?fName = "&162")
}
        """,

        """
SELECT {
    ?sched name ?sName ;
           ownerEmail ?email ;
           slotStarts ?start ;
           slotEnds ?end .
}
WHERE {
    ?sched 92 ?sName ;
           93/-83/82 ?email ;

           -116/112 ?start ; # Schedule <- Availability -> Start
           -116/113 ?end .   # Schedule <- Availability -> End

    FILTER(?sName = "&92")
}
        """,

        """
SELECT {
    ?user id ?id ;
          username ?username ;
          verifiedEmails ?emailValue ;
          availabilityStarts ?start .
}
WHERE {
    ?user 41 ?id ;
          42 ?username ;
          -83/82 ?emailValue ;
          -114/112 ?start .
    FILTER(?id = "&41")
}
        """,

        """
SELECT {
    ?team id ?tId ;
          name ?tName ;
          workflowNames ?wName ;
          memberUserIds ?uId .
}
WHERE {
    ?team 1 ?tId ;
          2 ?tName ;
          -194/192 ?wName ;
          -55/54/41 ?uId .
    FILTER(?tId = "&1")
}
        """,

        """
SELECT {
    ?user id ?uId ;
          roles ?rName ;
          assignedAttributeOptionIds ?optId .
}
WHERE {
    ?user 41 ?uId ;
          -54/56/12 ?rName ;
          -54/-72/71/31 ?optId .
    FILTER(?uId = "&41")
}
        """,

        """
SELECT {
    ?team id ?tId ;
          features ?fName ;
          associatedScheduleIds ?sId .
}
WHERE {
    ?team 1 ?tId ;
          -181/172/162 ?fName ;
          -105/106/91 ?sId .
    FILTER(?tId = "&1")
}
        """,

        """
SELECT {
    ?user id ?uId ;
          oooStarts ?oooStart ;
          hostedBookingTitles ?bTitle .
}
WHERE {
    ?user 41 ?uId ;
          -124/122 ?oooStart ;
          -234/232 ?bTitle .
    FILTER(?uId = "&41")
}
        """,

        // 30
        """
SELECT {
    ?schedule id ?sId ;
              ownerName ?uName ;
              starts ?start ;
              ends ?end .
}
WHERE {
    ?schedule 91 ?sId ;
              93/43 ?uName ;
              -116/112 ?start ;
              -116/113 ?end .
    FILTER(?sId = "&91")
}
        """,

        """
SELECT {
    ?attribute id ?aId ;
               attributeName ?aName ;
               teamId ?teamId ;
               assignedUsernames ?uName .
}
WHERE {
    ?attribute 21 ?aId ;
               22 ?aName ;
               23/1 ?teamId ;
               -33/-71/72/54/43 ?uName .
    FILTER(?aId = "&21")
}
        """,

        """
SELECT {
    ?hostGroup id ?hgId ;
               membershipIds ?mId .
}
WHERE {
    ?hostGroup 131 ?hgId ;
               -146/143/51 ?mId .
    FILTER(?hgId = "&131")
}
        """,

        """
SELECT {
    ?user id ?uId ;
          emails ?email ;
          orgScopeTeams ?tName .
}
WHERE {
    ?user 41 ?uId ;
          -83/82 ?email ;
          -61/62/2 ?tName .
    FILTER(?uId = "&41")
}
        """,

        """
SELECT {
    ?role id ?rId ;
          teamId ?teamId ;
          description ?desc ;
          memberUsernames ?uName .
}
WHERE {
    ?role 11 ?rId ;
          14/1 ?teamId ;
          13 ?desc ;
          -56/54/42 ?uName .
    FILTER(?rId = "&11")
}
        """,

        // endregion
        // region prompt3:Filters

        """
SELECT {
    ?user id ?uId ;
          username ?uName ;
          slotsStartingAfter ?startTime .
}
WHERE {
    ?user 41 ?uId ;
          42 ?uName ;
          -114/112 ?startTime .
    FILTER(?uId = "&41")
    FILTER(?startTime >= "&112")
}
        """,

        """
SELECT {
    ?role id ?rId ;
          roleName ?rName ;
          acceptedUsernames ?uName .
}
WHERE {
    ?role 11 ?rId ;
          12 ?rName ;
          14/1 ?teamId ;
          -56 ?membership .
    ?membership 54/42 ?uName ;
                52 ?status .
    FILTER(?rId = "&11")
    FILTER(?teamId = "&1")
    FILTER(?status = "&52")
}
        """,

        """
SELECT {
    ?team id ?tId ;
          name ?tName ;
          features ?fName .
}
WHERE {
    ?team 1 ?tId ;
          2 ?tName ;
          -62/61/41 ?scopeUserId ;
          -181/172/162 ?fName .
    FILTER(?scopeUserId = "&41")
}
        """,

        """
SELECT {
    ?eventType id ?etId ;
               title ?title ;
               expiringHostUsernames ?uName .
}
WHERE {
    ?eventType 101 ?etId ;
               102 ?title ;
               -144/142 ?user .
    ?user 42 ?uName ;
          -114/113 ?endTime .
    FILTER(?etId = "&101")
    FILTER(?endTime <= "&113")
}
        """,

        """
SELECT {
    ?user id ?uId ;
          emails ?email ;
          filteredMembershipIds ?mId .
}
WHERE {
    ?user 41 ?uId ;
          -83/82 ?email ;
          -54 ?membership .
    ?membership 51 ?mId ;
                56/11 ?roleId .
    FILTER(?uId = "&41")
    FILTER(?roleId = "&11")
}
        """,

        // 40
        """
SELECT {
    ?workflow name ?wfName ;
              stepNumbers ?stepNum ;
              actions ?action .
}
WHERE {
    ?workflow 194/1 ?teamId ;
              192 ?wfName ;
              -204/202 ?stepNum ;
              -204/203 ?action .

    FILTER(?teamId = "&1")
}
        """,

        """
SELECT {
    ?membership role ?rName ;
                userName ?uName ;
                emails ?email .
}
WHERE {
    ?membership 55/1 ?teamId ;
                56/12 ?rName ;
                54/43 ?uName ;
                54/-83/82 ?email .

    FILTER(?teamId = "&1")
}
        """,

        """
SELECT {
    ?team name ?teamName ;
          features ?fName ;
          attributes ?attrName .
}
WHERE {
    ?team 1 ?teamId ;
          2 ?teamName ;
          -181/172/162 ?fName ;
          -23/22 ?attrName .

    FILTER(?teamId = "&1")
}
        """,

        """
SELECT {
    ?workflow name ?wfName ;
              eventTitles ?etTitle ;
              ownerIds ?ownerId .
}
WHERE {
    ?workflow 191 ?wfId ;
              192 ?wfName ;
              -211/212/102 ?etTitle ;
              -211/212/104/41 ?ownerId .

    FILTER(?wfId = "&191")
}
        """,

        """
SELECT {
    ?et title ?title ;
        hostGroupIds ?hgId ;
        eligibleMemberNames ?memberName .
}
WHERE {
    ?et 101 ?etId ;
        102 ?title ;
        -132/131 ?hgId ;
        105/-55/54/43 ?memberName .

    FILTER(?etId = "&101")
}
        """,

        """
SELECT {
    ?attribute name ?attrName ;
               teamId ?teamId ;
               options ?optValue ;
               selectedByMembershipIds ?mId .
}
WHERE {
    ?attribute 21 ?attrId ;
               22 ?attrName ;
               23/1 ?teamId ;
               -33/32 ?optValue ;
               -33/-71/72/51 ?mId .

    FILTER(?attrId = "&21")
}
        """,

        """
SELECT {
    ?schedule name ?schedName ;
              eventTypeTitles ?etTitle ;
              recentBookingTitles ?bTitle .
}
WHERE {
    ?schedule 93/41 ?userId ;
              92 ?schedName ;
              -106/102 ?etTitle ;
              -106/-235/232 ?bTitle ;
              -106/-235/236 ?bTime .

    FILTER(?userId = "&41")
    FILTER(?bTime >= "&236")
}
        """,

        """
SELECT {
    ?team name ?teamName ;
          features ?featureName ;
          roleDescriptions ?roleDesc .
}
WHERE {
    ?team 1 ?teamId ;
          2 ?teamName ;
          -181/172/162 ?featureName ;
          -14/13 ?roleDesc .

    FILTER(?teamId = "&1")
}
        """,

        """
SELECT {
    ?et title ?etTitle ;
        bookingTitles ?bTitle ;
        bookingTimes ?bTime .
}
WHERE {
    ?et 101 ?etId ;
        102 ?etTitle ;
        -235/232 ?bTitle ;
        -235/236 ?bTime .

    FILTER(?etId = "&101")
    FILTER(?bTime >= "&236")
    FILTER(?bTime <= "&236")
}
        """,

        """
SELECT {
    ?user name ?uName ;
          oooStarts ?oooStart ;
          availabilityStarts ?availStart .
}
WHERE {
    ?user 41 ?userId ;
          43 ?uName ;
          -124/122 ?oooStart ;
          -114/112 ?availStart .

    FILTER(?userId = "&41")
    FILTER(?oooStart >= "&122")
    FILTER(?availStart >= "&112")
}
        """,

        // 50
        """
SELECT {
    ?team name ?teamName ;
          attributeNames ?attrName ;
          optionValues ?optVal .
}
WHERE {
    ?team 1 ?teamId ;
          2 ?teamName ;
          -23/22 ?attrName ;
          -23/-33/32 ?optVal .

    FILTER(?teamId = "&1")
}
        """,

        """
SELECT {
    ?team name ?tName ;
          bookingTitles ?bTitle ;
          attendees ?email .
}
WHERE {
    ?team 1 ?teamId ;
          2 ?tName ;
          -105/-235/232 ?bTitle ;
          -105/-235/-243/242 ?email .

    FILTER(?teamId = "&1")
}
        """,

        """
SELECT {
    ?wf name ?wfName ;
        ownerIds ?ownerId ;
        teamIds ?teamId .
}
WHERE {
    ?wf 191 ?wfId ;
        192 ?wfName ;
        193/41 ?ownerId ;
        -221/222/1 ?teamId .

    FILTER(?wfId = "&191")
}
        """,

        """
SELECT {
    ?sched name ?sName ;
           eventTitles ?etTitle .
}
WHERE {
    ?sched 91 ?sId ;
           92 ?sName ;
           -106/102 ?etTitle .

    FILTER(?sId = "&91")
}
        """,

        """
SELECT {
    ?user name ?uName ;
          features ?fName ;
          teams ?teamId ;
          assignedValues ?val .
}
WHERE {
    ?user 41 ?userId ;
          43 ?uName ;
          -171/172/162 ?fName ;
          -54/55/1 ?teamId ;
          -54/-72/71/32 ?val .

    FILTER(?userId = "&41")
}
        """,

        """
SELECT {
    ?et title ?etTitle ;
        hostNames ?uName ;
        acceptanceStatus ?accepted .
}
WHERE {
    ?et 101 ?etId ;
        102 ?etTitle ;
        -144/143/54/42 ?uName ;
        -144/143/52 ?accepted .

    FILTER(?etId = "&101")
}
        """,

        """
SELECT {
    ?team name ?tName ;
          pendingMemberIds ?mId ;
          pendingUsernames ?uName .
}
WHERE {
    ?team 1 ?teamId ;
          2 ?tName ;
          -55/51 ?mId ;
          -55/52 ?accepted ;
          -55/54/42 ?uName .

    FILTER(?teamId = "&1")
    FILTER(?accepted = "&52")
}
        """,

        """
SELECT {
    ?wf name ?wfName ;
        etTitles ?etTitle ;
        etDescs ?etDesc .
}
WHERE {
    ?wf 191 ?wfId ;
        192 ?wfName ;
        -211/212/102 ?etTitle ;
        -211/212/103 ?etDesc .

    FILTER(?wfId = "&191")
}
        """,

        """
SELECT {
    ?user name ?uName ;
          bookingTitles ?bTitle ;
          scheduleNames ?sName .
}
WHERE {
    ?user 41 ?userId ;
          43 ?uName ;
          -234/232 ?bTitle ;
          -234/235/106/92 ?sName .

    FILTER(?userId = "&41")
}
        """,

        """
SELECT {
    ?team name ?tName ;
          wfNames ?wfName ;
          stepActions ?action .
}
WHERE {
    ?team 1 ?teamId ;
          2 ?tName ;
          -222/221/192 ?wfName ;
          -222/221/-204/203 ?action .

    FILTER(?teamId = "&1")
}
        """,

        // endregion
        // region hand-made

        // 60
        """
SELECT {
    ?attribute id ?aId ;
               name ?aName ;
               allOptionValues ?optVal ;
               usernames ?username .
}
WHERE {
    ?attribute 21 ?aId ;
               22 ?aName ;
               23/1 ?teamId ;
               -33/32 ?optVal ;
               23/-71/72/54/42 ?username .

    FILTER(?teamId = "&1")
}
        """,



        """
SELECT {
    ?workflow
        id ?id ;
        user ?uName ;
        emails ?email ;
        steps ?step .

}
WHERE {
    ?workflow
        191 ?id ;
        193/42 ?uName ;
        194 ?team ;
        -204/201 ?step .

    ?team
        1 ?teamId ;
        -84/82 ?email .

    FILTER(?teamId = "&1")
}
        """,

        // NOT TOO LARGE RESULT
        """
SELECT {
    ?team
        name ?name ;
        roles ?role ;
        hGroups ?hGroup .

}
WHERE {
    ?team
        2 ?name ;
        -14/12 ?role ;
        -105/-132/131 ?hGroup .
    FILTER(?name = "&2")
}
        """,

        """
SELECT {
    ?user
        name ?name ;
        eTypes ?eTypeName ;
        roles ?roles .
}
WHERE {
    ?user
        42 ?name ;
        -93/-106/102 ?eTypeName ;
        -54/53 ?roles .
    FILTER(?name = "&42")
}
        """,

//         """
// SELECT {
//     ?booking
//         name ?name ;
//         # userName ?uName ;
//         eTypes ?eType .
// }
// WHERE {
//     ?booking
//         232 ?name ;
//         234 ?user .
//     ?user
//         # 42 ?uName ;
//         -93/-106/101 ?eType .

//     FILTER(?name = "&232")
// }

//         """

        """
SELECT {
    ?user
        name ?name ;
        availabilityStarts ?avail ;
        eTypeEmails ?email .
}
WHERE {
    ?user
        42 ?name ;
        -114/112 ?avail ;
        -104/105/-84/82 ?email .

    FILTER(?name = "&42")
}
        """,

        """
SELECT {
    ?user
        name ?name ;
        ownEType ?etName ;
        teamNames ?tName ;
        attributes ?tAttr .
}
WHERE {
    ?user
        42 ?name ;
        -104 ?ownEt .

    ?ownEt
        102 ?etName ;
        105/2 ?tName ;
        105/-23/22 ?tAttr .

    FILTER(?name = "&42")
}
        """,
        // endregion

    };



    // List of valid (non-crashing) ids of queries
    private static final List<Integer> bIds = List.of(
        0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
        10, 11, 12, 13, 14, 15, 16, 17, 18, 19
    );
    private static final List<Integer> bIgnIdsPSQL = List.of(15, 18);
    private static final List<Integer> bIgnIdsMONGO = List.of(18);
    private static final List<Integer> bIgnIdsNEO = List.of(15, 18);
    /** Benchmark queries, split into 2 groups.
     * 1. single-datasource (for mongodb), few kinds
     * 2. multi-datasource (for mongodb), many kinds
     */
    private static final String[] bQueries = {
        // region Small (0..9)
         """
SELECT {
    ?role id ?id ;
          name ?name ;
          description ?desc ;
          teamId ?teamId .
}
WHERE {
    ?role 11 ?id ;
          12 ?name ;
          13 ?desc ;
          14/1 ?teamId .
    FILTER(?id = "&11")
}
        """,

        """
SELECT {
    ?workflow name ?wfName ;
           number ?stepNum ;
           action ?action .
}
WHERE {
    ?workflow 192 ?wfName ;
              -204 ?steps .

    ?steps 202 ?stepNum ;
           203 ?action .

    FILTER(?action = "&203")
}
        """,

        """
SELECT {
    ?vEmail email ?emailValue ;
            ownerId ?userId .
}
WHERE {
    ?vEmail 82 ?emailValue ;
            83/41 ?userId .

    FILTER(?emailValue = "&82")
}
        """,

        """
SELECT {
    ?user id ?id ;
          username ?username ;
          verifiedEmails ?emailValue ;
          availabilityStarts ?start .
}
WHERE {
    ?user 41 ?id ;
          42 ?username ;
          -83/82 ?emailValue ;
          -114/112 ?start .
    FILTER(?id = "&41")
}
        """,

        """
SELECT {
    ?user id ?uId ;
          username ?uName ;
          slotsStartingAfter ?startTime .
}
WHERE {
    ?user 41 ?uId ;
          42 ?uName ;
          -114/112 ?startTime .
    FILTER(?uId = "&41")
    FILTER(?startTime >= "&112")
}
        """,

        """
SELECT {
    ?user id ?uId ;
          emails ?email ;
          filteredMembershipIds ?mId .
}
WHERE {
    ?user 41 ?uId ;
          -83/82 ?email ;
          -54 ?membership .
    ?membership 51 ?mId ;
                56/11 ?roleId .
    FILTER(?uId = "&41")
    FILTER(?roleId = "&11")
}
        """,

        """
SELECT {
    ?workflow name ?wfName ;
              stepNumbers ?stepNum ;
              actions ?action .
}
WHERE {
    ?workflow 194/1 ?teamId ;
              192 ?wfName ;
              -204/202 ?stepNum ;
              -204/203 ?action .

    FILTER(?teamId = "&1")
}
        """,

        """
SELECT {
    ?user name ?uName ;
          oooStarts ?oooStart ;
          availabilityStarts ?availStart .
}
WHERE {
    ?user 41 ?userId ;
          43 ?uName ;
          -124/122 ?oooStart ;
          -114/112 ?availStart .

    FILTER(?userId = "&41")
    FILTER(?oooStart >= "&122")
    FILTER(?availStart >= "&112")
}
        """,

        """
SELECT {
    ?team name ?teamName ;
          attributeNames ?attrName ;
          optionValues ?optVal .
}
WHERE {
    ?team 1 ?teamId ;
          2 ?teamName ;
          -23/22 ?attrName ;
          -23/-33/32 ?optVal .

    FILTER(?teamId = "&1")
}
        """,

        """
SELECT {
    ?wf name ?wfName ;
        ownerIds ?ownerId ;
        teamIds ?teamId .
}
WHERE {
    ?wf 191 ?wfId ;
        192 ?wfName ;
        193/41 ?ownerId ;
        -221/222/1 ?teamId .

    FILTER(?wfId = "&191")
}
        """,

        // endregion
        // region Large (10..19)
        """
SELECT {
    ?eventType title ?title ;
               hostId ?hostId .
}
WHERE {
    ?eventType 106 ?schedule ;
               102 ?title ;
               104/41 ?hostId .

    ?schedule 91 ?scheduleId .

    FILTER(?title = "&102")
}
        """,

        """
SELECT {
    ?user username ?handle ;
          oooStarts ?oooStart ;
          scheduleNames ?schedName .
}
WHERE {
    # Match User by ID
    ?user 41 ?userId ;
          42 ?handle .

    # Path: User <- OutOfOffice -> Start
    ?user -124/122 ?oooStart .

    # Path: User <- Schedule -> Name
    ?user -93/92 ?schedName .

    FILTER(?handle = "&42")
}
        """,

        """
SELECT {
    ?booking title ?bTitle ;
             attendeeEmails ?attEmail ;
             hostEmails ?hostEmail .
}
WHERE {
    # Match Booking by ID
    ?booking 231 ?bId ;
             232 ?bTitle .

    # Path: Booking <- Attendee -> Email
    ?booking -243/242 ?attEmail .

    # Path: Booking -> EventType -> User (Owner) <- VerifiedEmail -> Value
    ?booking 235/104/-83/82 ?hostEmail .

    FILTER(?bTitle = "&232")
}
        """,

        """
SELECT {
    ?user username ?handle ;
          bookingTitles ?title ;
          allAttendeeEmails ?email .
}
WHERE {
    ?user 42 ?handle ;
          -234/232 ?title ;          # User <- Booking -> Title
          -234/-243/242 ?email .     # User <- Booking <- Attendee -> Email

    FILTER(?handle = "&42")
}
        """,

        """
SELECT {
    ?sched name ?sName ;
           ownerEmail ?email ;
           slotStarts ?start ;
           slotEnds ?end .
}
WHERE {
    ?sched 92 ?sName ;
           93/-83/82 ?email ;

           -116/112 ?start ; # Schedule <- Availability -> Start
           -116/113 ?end .   # Schedule <- Availability -> End

    FILTER(?sName = "&92")
}
        """,

        """
SELECT {
    ?workflow
        id ?id ;
        user ?uName ;
        emails ?email ;
        steps ?step .

}
WHERE {
    ?workflow
        191 ?id ;
        193/42 ?uName ;
        194 ?team ;
        -204/201 ?step .

    ?team
        1 ?teamId ;
        -84/82 ?email .

    FILTER(?uName = "&42")
}
        """,

        """
SELECT {
    ?team
        name ?name ;
        roles ?role ;
        hGroups ?hGroup .

}
WHERE {
    ?team
        2 ?name ;
        -14/12 ?role ;
        -105/-132/131 ?hGroup .
    FILTER(?name = "&2")
}
        """,

        """
SELECT {
    ?user
        name ?name ;
        eTypes ?eTypeName ;
        roles ?roles .
}
WHERE {
    ?user
        42 ?name ;
        -93/-106/102 ?eTypeName ;
        -54/53 ?roles .
    FILTER(?name = "&42")
}
        """,

        """
SELECT {
    ?user
        name ?name ;
        availabilityStarts ?avail ;
        eTypeTeam ?tName ;
        eTypeEmails ?email .
}
WHERE {
    ?user
        42 ?name ;
        -114/112 ?avail ;
        -104/105 ?team .

    ?team
        2 ?tName ;
        -84/82 ?email .

    FILTER(?name = "&42")
}
        """,

        """
SELECT {
    ?user
        name ?name ;
        ownEType ?etName ;
        teamNames ?tName ;
        attributes ?tAttr .
}
WHERE {
    ?user
        42 ?name ;
        -104 ?ownEt .

    ?ownEt
        102 ?etName ;
        105/2 ?tName ;
        105/-23/22 ?tAttr .

    FILTER(?name = "&42")
}
        """,

        // endregion
    };


    public static List<Integer> ids() { return bIds; }
    /** Which queries crash on unoptimized runtime */
    public static List<Integer> unoptIgnoredIdsPSQL() { return bIgnIdsPSQL; }
    public static List<Integer> unoptIgnoredIdsMONGO() { return bIgnIdsMONGO; }
    public static List<Integer> unoptIgnoredIdsNEO() { return bIgnIdsNEO; }
    public static String[] queries() { return bQueries; }

    // public static List<Integer> ids() { return genIds; }
    // public static String[] queries() { return genQueries; }
}
