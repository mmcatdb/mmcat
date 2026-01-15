package cz.matfyz.tests.example.benchmark.caldotcom;

import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.QueryToInstance;
import cz.matfyz.querying.core.QueryExecution;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.optimizer.CollectorCache;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.tests.querying.FilterQueryFiller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalDotComTests {
    @SuppressWarnings({ "java:s1068" })
    private static final Logger LOGGER = LoggerFactory.getLogger(CalDotComTests.class);

    public static final Datasources datasources = new Datasources();
    private static final CollectorCache cache = new CollectorCache();

    // private static final List<Integer> queryIds = List.of(
    //     0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94, 95, 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109
    // );
    private static final List<Integer> queryIds = List.of(
        0, 1, 2, /*3,*/ 4, 5, 6, 7, 8, /*9, 10,*/ 11, 12, 13, /*14,*/ 15, /*16,*/ 17, 18, 19, /*20,*/ 21, 22, /*23,*/ 24, 25, 26, 27, /* 28, 29, 30, 31, 32, 33, 34, 35,*/ 36, /*37,*/ 38, 39, /*40, 41,*/ 42, /*43, 44, 45, 46,*/ 47, /*48,*/ 49, 50, /*51, 52,*/ 53, /*54, 55,*/ 56, 57, 58, /*59, 60,*/ 61, 62, 63, /*64, 65, 66, 67,*/ 68, /*69,*/ 70, 71, 72, /*73,*/ 74, /*75, 76, 77,*/ 78, /*79,*/ 80, 81, /*82, 83,*/ 84, /*85, 86, 87, 88, 89, 90, 91,*/ 92, 93, /*94,*/ 95, /*96,*/ 97, /*98,*/ 99, 100, /*101, 102, 103,*/ 104, 105 /*, 106, 107, 108, 109*/
    );

    static final List<TestDatasource<?>> testDatasources = List.of(
        datasources.mongoDB(),
        datasources.postgreSQL(),
        datasources.neo4j()
    );


    public static void testFunctional() {

        final var queryFiller = new FilterQueryFiller(
            new ValueGenerator(datasources.schema, List.of(datasources.postgreSQL())));

        final var queries = Stream.of(generatedQueries).map(q -> queryFiller.fillQuery(q)).toList();

        final var allTestDatasources = List.of(
            List.of(datasources.postgreSQL()),
            List.of(datasources.mongoDB()),
            List.of(datasources.neo4j()),
            testDatasources
        );
        Exception[] exceptions = new Exception[queries.size() * allTestDatasources.size()];

        for (int datasourceI = 0; datasourceI < testDatasources.size(); datasourceI++) {
            final var singleTestDatasources = allTestDatasources.get(datasourceI);
            for (int queryI : queryIds) {
                // from QueryToInstance
                final var provider = new DefaultControlWrapperProvider();
                final var kinds = singleTestDatasources.stream()
                    .flatMap(testDatasource -> {
                        provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                        return testDatasource.mappings.stream();
                    }).toList();

                try {
                    final int TRIES = 5;
                    for (int i = 0; i < TRIES; i++) {
                        final var q = queries.get(queryI);
                        final var filledQuery = q.generateQuery();
                        final var queryToInstance = new QueryToInstance(provider, datasources.schema, filledQuery, kinds, cache);

                        queryToInstance.execute();
                    }
                } catch (Exception e) {
                    exceptions[datasourceI * queries.size() + queryI] = e;
                }
            }
        }

        for (int datasourceI = 0; datasourceI < allTestDatasources.size(); datasourceI++) {
            final var dsrcs = allTestDatasources.get(datasourceI);
            System.out.println((dsrcs.size() == 1) ? dsrcs.get(0).datasource().identifier.toString() : "multiple");
            for (int queryI = 0; queryI < queries.size(); queryI++) {
                final var e = exceptions[datasourceI * queries.size() + queryI];
                System.out.println(queryI + ": " + (e == null ? e : e.getMessage()));
            }
        }
        System.out.println("TOTAL " + queryIds.size());
    }

    public static ResultsAndFile systemTest(List<TestDatasource<?>> testDatasources, String fileId) {
        final int REPETITIONS = 5;

        final var queryFiller = new FilterQueryFiller(
            new ValueGenerator(datasources.schema, List.of(datasources.postgreSQL())));

        final var queries = Stream.of(generatedQueries).map(q -> queryFiller.fillQuery(q)).toList();
        System.out.println("Queries? " + queries.size());


        // from QueryToInstance
        final var provider = new DefaultControlWrapperProvider();
        final var kinds = testDatasources.stream()
            .flatMap(testDatasource -> {
                provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                return testDatasource.mappings.stream();
            }).toList();

        final var results = new ArrayList<ResultRow>();

        for (int iteration = 0; iteration < REPETITIONS; iteration++) {
            for (final var idx : queryIds) {
                System.out.println("Query: " + idx + " of " + queries.size());
                final var q = queries.get(idx);
                final var filledQuery = q.generateQuery();
                final var queryToInstance = new QueryToInstance(provider, datasources.schema, filledQuery, kinds, cache);

                final var execution = queryToInstance.execute();

                // final var plan = queryToInstance.getPlan();

                results.add(new ResultRow(idx, execution));
            }
        }

        return new ResultsAndFile(results, exportToCSV(results, fileId));
    }

    public static String exportToCSV(List<ResultRow> rows, String fileId) {
        final var cal = new GregorianCalendar();

        final var filename = "cal.com-benchmark-" +
            fileId + "-" +
            String.format("%04d", cal.get(Calendar.YEAR)) + "-" +
            String.format("%02d", cal.get(Calendar.MONTH) + 1) + "-" +
            String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + "-" +
            cal.getTimeInMillis() +
            ".csv";

        try (final var writer = new BufferedWriter(new FileWriter("../../data/" + filename))) {
            ResultRow.writeCSVHeader(writer);
            for (final var row : rows) {
                row.writeCSVRow(writer);
            }
            LOGGER.info("Written benchmark measurements into file.");
        } catch (IOException e) {
            LOGGER.error("Writing benchmark result error: " + e.getMessage());
        }

        return filename;
    }

    // region generatedQueries

    public static final String[] generatedQueries = {

        // region prompt1:Simple

        """
SELECT {
    ?team id ?id ;
          name ?name .
}
WHERE {
    ?team 1 ?id ;
          2 ?name .
}
        """,

        """
SELECT {
    ?user id ?id ;
          username ?username ;
          verifiedEmail ?emailValue .
}
WHERE {
    ?user 41 ?id ;
          42 ?username ;
          -83/82 ?emailValue .
}
        """,

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
    ?booking id ?id ;
             title ?title ;
             time ?time ;
             userId ?userId .
}
WHERE {
    ?booking 231 ?id ;
             232 ?title ;
             236 ?time ;
             234/41 ?userId .
    FILTER(?userId = "&41")
}
        """,

        """
SELECT {
    ?booking id ?id ;
             title ?title ;
             time ?time ;
             userId ?userId .
}
WHERE {
    ?booking 231 ?id ;
             232 ?title ;
             236 ?time ;
             234/41 ?userId .
    FILTER(?time >= "&236")
}
        """,

        """
SELECT {
    ?user username ?username ;
          name ?name ;
          email ?emailId .
}
WHERE {
    ?user 42 ?username ;
          43 ?name ;
          -83/81 ?emailId .

    FILTER(?username = "&42")
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
    ?step workflowId? ?wid ;
          action ?stepAction ;
          stepNumber ?stepNum .
}
WHERE {
    ?step 204/192 ?wid;
          203 ?stepAction ;
          202 ?stepNum .

    FILTER(?stepAction = "&203")
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
    ?workflow name ?wfName ;
            stepNumbers ?stepNums ;
            stepActions ?stepActions ;
            stepIds ?stepIds .
}
WHERE {
    ?workflow 192 ?wfName ;
            -204/202 ?stepNums ;
            -204/203 ?stepActions ;
            -204/201 ?stepIds .

    FILTER(?wfName = "&192")
}
        """,

        """
SELECT {
    ?booking title ?bTitle ;
             time ?bTime ;
             attendeeEmails ?attEmails .
}
WHERE {
    ?booking 232 ?bTitle ;
             236 ?bTime ;
             -243/242 ?attEmails .

    FILTER(?bTime >= "&236")
}
        """,

        """
SELECT {
    ?user userId ?uId ;
          username ?uName ;
          availabilityStarts ?aStarts ;
          availabilityEnds ?aEnds .
}
WHERE {
    ?user 41 ?uId ;
          42 ?uName ;
          -114/112 ?aStarts ;
          -114/113 ?aEnds .

    FILTER(?aStarts < "&112")
    FILTER(?uId = "&41")
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
    ?user username ?username ;
          name ?name .
}
WHERE {
    ?user 41 ?userId ;
          42 ?username ;
          43 ?name .

    FILTER(?userId = "&41")
}
        """,

        """
SELECT {
    ?team name ?teamName ;
          members ?user .
}
WHERE {
    ?team 1 ?teamId ;
          2 ?teamName ;
          -55/54/42 ?user .

    FILTER(?teamId = "&1")
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
    ?booking description ?desc ;
             attendeeEmails ?email .
}
WHERE {
    ?booking 232 ?title ;
             233 ?desc ;
             -243/242 ?email .

    FILTER(?title = "&232")
}
        """,

        """
SELECT {
    ?workflow id ?wId ;
              steps ?stepAction .
}
WHERE {
    ?workflow 192 ?wName ;
              191 ?wId ;
              -204/203 ?stepAction .

    FILTER(?wName = "&192")
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

        """
SELECT {
    ?attribute id ?aId ;
        name ?aName ;
        team ?aTeamId ;
        options ?optValue .
}
WHERE {
    ?attribute 21 ?aId ;
        22 ?aName ;
        23/1 ?aTeamId ;
        -33/32 ?optValue .

    FILTER(?aId = "&21")
}
        """,

        """
SELECT {
    ?user id ?userId ;
        name ?userName ;
        scheduleNames ?sName .
}
WHERE {
    ?user 41 ?userId ;
        43 ?userName ;
        -93/92 ?sName .

    FILTER(?userId = "&41")
}
        """,

        """
SELECT {
    ?feature id ?fId ;
        featureName ?fName ;
        enabledTeamIds ?teamId .
}
WHERE {
    ?feature 161 ?fId ;
        162 ?fName ;
        -182/181/1 ?teamId .

    FILTER(?fId = "&161")
}
        """,

        """
SELECT {
    ?team id ?tId ;
        teamName ?tName ;
        attributeIds ?attrId .
}
WHERE {
    ?team 1 ?tId ;
        2 ?tName ;
        -23/21 ?attrId .

    FILTER(?tId = "&1")
}
        """,

        """
SELECT {
    ?user id ?uId ;
        username ?username ;
        emails ?emailVal .
}
WHERE {
    ?user 41 ?uId ;
        42 ?username ;
        -83/82 ?emailVal .

    FILTER(?uId = "&41")
}
        """,

        """
SELECT {
    ?booking id ?bId ;
        title ?title ;
        attendeeEmails ?email .
}
WHERE {
    ?booking 231 ?bId ;
        232 ?title ;
        -243/242 ?email .

    FILTER(?bId = "&231")
}
        """,

        """
SELECT {
    ?workflow id ?wfId ;
        name ?wfName ;
        stepNumbers ?stepNum .
}
WHERE {
    ?workflow 191 ?wfId ;
        192 ?wfName ;
        -204/202 ?stepNum .

    FILTER(?wfId = "&191")
}
        """,

        """
SELECT {
    ?eventType id ?id ;
        title ?title ;
        associatedUserIds ?userId .
}
WHERE {
    ?eventType 101 ?id ;
        102 ?title ;
        -152/151/41 ?userId .

    FILTER(?id = "&101")
}
        """,

        """
SELECT {
    ?team id ?teamId ;
        name ?name ;
        memberUserIds ?userId .
}
WHERE {
    ?team 1 ?teamId ;
        2 ?name ;
        -55/54/41 ?userId .

    FILTER(?teamId = "&1")
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
    ?attribute name ?attrName ;
            teamId ?teamId ;
            validOptions ?optionValue .
}
WHERE {
    # Match Attribute by ID
    ?attribute 21 ?attrId ;
            23/1 ?teamId ;
            22 ?attrName .

    # Path: Attribute <- AttributeOption -> Value
    ?attribute -33/32 ?optionValue .

    FILTER(?attrId = "&21")
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

        """
SELECT {
    ?schedule name ?sName ;
              linkedEvents ?etTitle ;
              availabilityStarts ?startTime .
}
WHERE {
    # Match Schedule by ID
    ?schedule 91 ?sId ;
              92 ?sName .

    # Path: Schedule <- EventType -> Title
    ?schedule -106/102 ?etTitle .

    # Path: Schedule <- Availability -> Start
    ?schedule -116/112 ?startTime .

    FILTER(?sId = "&91")
}
        """,

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
    ?feature name ?fName ;
             enabledForUsers ?username ;
             enabledForTeams ?teamName .
}
WHERE {
    # Match Feature by ID
    ?feature 161 ?fId ;
             162 ?fName .

    # Path: Feature <- UserFeatures -> User -> Username
    ?feature -172/171/42 ?username .

    # Path: Feature <- TeamFeatures -> Team -> Name
    ?feature -182/181/2 ?teamName .

    FILTER(?fId = "&161")
}
        """,

        """
SELECT {
    ?team name ?teamName ;
        memberUsernames ?username ;
        memberRoles ?roleId .
}
WHERE {
    ?team 1 ?teamId ;
        2 ?teamName ;
        -55/54/42 ?username ;   # Team <- Membership -> User -> Username
        -55/56/11 ?roleId .   # Team <- Membership -> Role -> RoleId

    FILTER(?teamId = "&1")
}
        """,

        """
SELECT {
    ?et title ?etTitle ;
        workflowNames ?wfName ;
        workflowActions ?stepAction .
}
WHERE {
    ?et 101 ?etId ;
        102 ?etTitle ;
        -212/211/192 ?wfName ;         # EventType <- WorkflowsOnEventTypes -> Workflow -> Name
        -212/211/-204/203 ?stepAction . # EventType <- ... -> Workflow <- WorkflowStep -> Action

    FILTER(?etId = "&101")
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
    ?feat name ?fName ;
          enabledOnTeams ?teamName ;
          enabledOnUsers ?userName .
}
WHERE {
    ?feat 162 ?fName ;
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
    ?eventType id ?eId ;
               title ?title ;
               hostUserIds ?hostUId ;
               workflowNames ?wfName .
}
WHERE {
    ?eventType 101 ?eId ;
               102 ?title ;
               -144/142/41 ?hostUId ;
               -212/211/192 ?wfName .
    FILTER(?eId = "&101")
}
        """,

        """
SELECT {
    ?booking id ?bId ;
             title ?bTitle ;
             time ?bTime ;
             attendeeEmails ?aEmail .
}
WHERE {
    ?booking 231 ?bId ;
             232 ?bTitle ;
             236 ?bTime ;
             -243/242 ?aEmail .
    FILTER(?bId = "&231")
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
    ?workflow id ?wfId ;
              name ?wfName ;
              stepNumbers ?sNum ;
              stepActions ?sAction .
}
WHERE {
    ?workflow 191 ?wfId ;
              192 ?wfName ;
              -204/202 ?sNum ;
              -204/203 ?sAction .
    FILTER(?wfId = "&191")
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

        """
SELECT {
    ?attribute id ?aId ;
               name ?aName ;
               teamId ?teamId ;
               allOptionValues ?optVal .
}
WHERE {
    ?attribute 21 ?aId ;
               22 ?aName ;
               23/1 ?teamId ;
               -33/32 ?optVal .
    FILTER(?aId = "&21")
}
        """,

        """
SELECT {
    ?membership id ?mId ;
                status ?status ;
                roleName ?rName ;
                userTeamNames ?tName .
}
WHERE {
    ?membership 51 ?mId ;
                52 ?status ;
                56/12 ?rName ;
                54/-54/55/2 ?tName .
    FILTER(?mId = "&51")
}
        """,

        """
SELECT {
    ?user id ?uId ;
          enabledFeatures ?fName ;
          teamNames ?tName .
}
WHERE {
    ?user 41 ?uId ;
          -171/172/162 ?fName ;
          -54/55/2 ?tName .
    FILTER(?uId = "&41")
}
        """,

        """
SELECT {
    ?eventType id ?etId ;
               title ?etTitle ;
               bookingTitles ?bTitle ;
               bookingDescriptions ?bDesc .
}
WHERE {
    ?eventType 101 ?etId ;
               102 ?etTitle ;
               -235/232 ?bTitle ;
               -235/233 ?bDesc .
    FILTER(?etId = "&101")
}
        """,

        """
SELECT {
    ?team id ?tId ;
          teamName ?tName ;
          workflowActions ?action .
}
WHERE {
    ?team 1 ?tId ;
          2 ?tName ;
          -194/-204/203 ?action .
    FILTER(?tId = "&1")
}
        """,

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
    ?workflow id ?wfId ;
              workflowName ?wfName ;
              appliedEventTitles ?etTitle ;
              appliedTeamNames ?tName .
}
WHERE {
    ?workflow 191 ?wfId ;
              192 ?wfName ;
              -211/212/102 ?etTitle ;
              -221/222/2 ?tName .
    FILTER(?wfId = "&191")
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
    ?booking id ?bId ;
             hostName ?uName ;
             attendeeEmails ?attEmail .
}
WHERE {
    ?booking 231 ?bId ;
             234/43 ?uName ;
             -243/242 ?attEmail .
    FILTER(?bId = "&231")
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
    ?user id ?uId ;
          username ?uName ;
          oooEnds ?endDate .
}
WHERE {
    ?user 41 ?uId ;
          42 ?uName ;
          -124/123 ?endDate ;
          -54/55/1 ?teamId .
    FILTER(?teamId = "&1")
    FILTER(?endDate > "&123")
}
        """,

        """
SELECT {
    ?booking id ?bId ;
             title ?title ;
             time ?bTime ;
             attendees ?email .
}
WHERE {
    ?booking 231 ?bId ;
             232 ?title ;
             236 ?bTime ;
             235/101 ?etId ;
             -243/242 ?email .
    FILTER(?etId = "&101")
    FILTER(?bTime >= "&236")
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
    ?workflow id ?wfId ;
              name ?wfName ;
              lateStepActions ?action ;
              stepNumbers ?sNum .
}
WHERE {
    ?workflow 191 ?wfId ;
              192 ?wfName ;
              -204 ?step .
    ?step 203 ?action ;
          202 ?sNum .
    FILTER(?wfId = "&191")
    FILTER(?sNum >= "&202")
}
        """,

        """
SELECT {
    ?attribute id ?aId ;
               teamId ?teamId;
               name ?aName ;
               filteredOptionValues ?val .
}
WHERE {
    ?attribute 21 ?aId ;
               23/1 teamId ;
               22 ?aName ;
               -33 ?option .
    ?option 32 ?val ;
            31 ?optId .
    FILTER(?aId = "&21")
    FILTER(?optId > "&31")
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
    FILTER(?tId <= "&1")
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

        """
SELECT {
    ?schedule id ?sId ;
              name ?sName ;
              rangedBookingTitles ?bTitle .
}
WHERE {
    ?schedule 91 ?sId ;
              92 ?sName ;
              -106/-235 ?booking .
    ?booking 232 ?bTitle ;
             236 ?bTime .
    FILTER(?sId = "&91")
    FILTER(?bTime >= "&236")
    FILTER(?bTime <= "&236")
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
    ?booking title ?title ;
             time ?time ;
             attendeeEmails ?email .
}
WHERE {
    ?booking 234/41 ?userId ;
             231 ?bId ;
             232 ?title ;
             236 ?time ;
             -243/242 ?email .

    FILTER(?userId = "&41")
    FILTER(?time >= "&236")
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
    ?et title ?title ;
        users ?userId ;
        starts ?start ;
        ends ?end .
}
WHERE {
    ?et 101 ?etId ;
        102 ?title ;
        -115/114/41 ?userId ;
        -115/112 ?start ;
        -115/113 ?end .

    FILTER(?etId = "&101")
    FILTER(?start >= "&112")
}
        """,

        """
SELECT {
    ?user name ?userName ;
          oooIds ?oooId ;
          oooStarts ?oooStart .
}
WHERE {
    ?user 41 ?uId ;
          43 ?userName ;
          -124/121 ?oooId ;
          -124/122 ?oooStart .

    FILTER(?oooStart <= "&122")
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
    ?user name ?userName ;
          teamNames ?teamName ;
          roleNames ?roleName .
}
WHERE {
    ?user 41 ?userId ;
          43 ?userName ;
          -54/55/2 ?teamName ;
          -54/56/12 ?roleName .

    FILTER(?userId = "&41")
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
    ?wf name ?wfName ;
        actions ?action ;
        steps ?stepNum .
}
WHERE {
    ?wf 191 ?wfId ;
        192 ?wfName ;
        -204/203 ?action ;
        -204/202 ?stepNum .

    FILTER(?wfId = "&191")
}
        """,

        """
SELECT {
    ?team name ?teamName ;
          memberNames ?uName ;
          verifiedEmails ?emailVal .
}
WHERE {
    ?team 1 ?teamId ;
          2 ?teamName ;
          -55/54/43 ?uName ;
          -55/54/-83/82 ?emailVal .

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
    ?et title ?etTitle ;
        hostGroups ?hgId ;
        membershipIds ?mId .
}
WHERE {
    ?et 101 ?etId ;
        102 ?etTitle ;
        -132/131 ?hgId ;
        -144/143/51 ?mId .

    FILTER(?etId = "&101")
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
    ?team name ?tName ;
          scopedUsernames ?username .
}
WHERE {
    ?team 1 ?teamId ;
          2 ?tName ;
          -62/61/42 ?username .

    FILTER(?teamId = "&1")
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
    ?user name ?uName ;
          oooIds ?oooId ;
          ends ?oooEnd .
}
WHERE {
    ?user 43 ?uName ;
          -124/121 ?oooId ;
          -124/123 ?oooEnd .

    FILTER(?oooEnd >= "&123")
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
          allFeatureNames ?fName .
}
WHERE {
    ?user 43 ?uName ;
          -54/55/1 ?teamId ;
          -171/172/162 ?fName .

    FILTER(?teamId = "&1")
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
        """

        // endregion
    };

    public static record ResultRow(
        int queryIdx, // TODO maybe use this directly alongside QueryExecution?
        long planningTimeInMs,
        long underlyingDBMSSelectionTimeInMs,
        long innerSelectionTimeInMs,
        long projectionTimeInMs,
        int resultRowCount,
        int datasourceNodes, // TODO maybe split into PostgreSQLNodes, MongoDSNodes, Neo4jNodes?
        int filterNodes
    ) {
        private static class MutInt { public int v = 0; }
        private static int getDatasourceNodes(QueryExecution execution) {
            var dsNodes = new MutInt();
            GraphUtils.forEachDFS(execution.plan().root, node -> {
                if (node instanceof DatasourceNode) dsNodes.v++;
            });
            return dsNodes.v;
        }
        private static int getFilterNodes(QueryExecution execution) {
            var filterNodes = new MutInt();
            GraphUtils.forEachDFS(execution.plan().root, node -> {
                if (node instanceof FilterNode) filterNodes.v++;
            });
            return filterNodes.v;
        }

        public ResultRow(int queryIdx, QueryExecution execution) {
            this(
                queryIdx,
                execution.planningTimeInMs(),
                execution.underlyingDBMSSelectionTimeInMs(),
                execution.selectionTimeInMs() - execution.underlyingDBMSSelectionTimeInMs(),
                execution.projectionTimeInMs(),
                execution.result().children().size(),
                getDatasourceNodes(execution),
                getFilterNodes(execution)
            );
        }

        public static void writeCSVHeader(BufferedWriter writer) throws IOException {
            writer.write("queryIdx,");
            writer.write("planningMs,");
            writer.write("underlyingSelectionMs,");
            writer.write("innerSelectionMs,");
            writer.write("projectionMs,");
            writer.write("resultRowCount,");
            writer.write("datasourceNodes,");
            writer.write("filterNodes");
            writer.write("\n");
        }

        public void writeCSVRow(BufferedWriter writer) throws IOException {
            writer.write(Integer.toString(queryIdx));
            writer.write(",");
            writer.write(Long.toString(planningTimeInMs()));
            writer.write(",");
            writer.write(Long.toString(underlyingDBMSSelectionTimeInMs()));
            writer.write(",");
            writer.write(Long.toString(innerSelectionTimeInMs()));
            writer.write(",");
            writer.write(Long.toString(projectionTimeInMs()));

            writer.write(",");
            writer.write(Long.toString(resultRowCount()));
            writer.write(",");
            writer.write(Long.toString(datasourceNodes()));
            writer.write(",");
            writer.write(Long.toString(filterNodes()));
            writer.write("\n");
        }
    }
    public static record ResultsAndFile(List<ResultRow> results, String filename) {}

}
