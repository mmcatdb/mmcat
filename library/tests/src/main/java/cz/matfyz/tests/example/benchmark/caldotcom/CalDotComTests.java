package cz.matfyz.tests.example.benchmark.caldotcom;

import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.querying.QueryToInstance;
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

    private static final Datasources datasources = new Datasources();
    private static final CollectorCache cache = new CollectorCache();

    public static record ResultRow(int queryIdx, long executionMs, String additionalInfo) {}
    public static record ResultsAndFile(List<ResultRow> results, String filename) {}

    public static void testFunctional() {
        final List<TestDatasource<?>> testDatasources = List.of(
            datasources.mongoDB(),
            datasources.postgreSQL(),
            datasources.neo4j()
        );

        final var queryFiller = new FilterQueryFiller(
            new ValueGenerator(datasources.schema, List.of(datasources.postgreSQL())));

        // TODO Variant
        // final var queries = Stream.of(generatedQueries).map(q -> queryFiller.fillQuery(q)).toList();
        final var queries = Stream.of(generatedQueriesManyJoins).map(q -> queryFiller.fillQuery(q)).toList();

        Exception[] exceptions = new Exception[queries.size() * testDatasources.size()];

        for (int datasourceI = 0; datasourceI < testDatasources.size(); datasourceI++) {
            for (int queryI = 0; queryI < queries.size(); queryI++) {
                final var singleTestDatasources = List.of(testDatasources.get(datasourceI));

                // from QueryToInstance
                final var provider = new DefaultControlWrapperProvider();
                final var kinds = singleTestDatasources.stream()
                    .flatMap(testDatasource -> {
                        provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                        return testDatasource.mappings.stream();
                    }).toList();

                try {
                    final var q = queries.get(queryI);
                    final var filledQuery = q.generateQuery();
                    final var queryToInstance = new QueryToInstance(provider, datasources.schema, filledQuery, kinds, cache);

                    queryToInstance.execute();

                    // final var plan = queryToInstance.getPlan();
                } catch (Exception e) {
                    exceptions[datasourceI * queries.size() + queryI] = e;
                }
            }
        }

        for (int datasourceI = 0; datasourceI < testDatasources.size(); datasourceI++) {
            System.out.println(testDatasources.get(datasourceI).datasource().identifier.toString());
            for (int queryI = 0; queryI < queries.size(); queryI++) {
                final var e = exceptions[datasourceI * queries.size() + queryI];
                System.out.println(queryI + ": " + (e == null ? e : e.getMessage()));
            }
        }
    }

    public static ResultsAndFile systemTest() {
        final int REPETITIONS = 1;

        final List<TestDatasource<?>> testDatasources = List.of(
            // datasources.postgreSQL() // ,
            datasources.mongoDB() // ,
            // datasources.neo4j()
        );

        final var queryFiller = new FilterQueryFiller(
            new ValueGenerator(datasources.schema, List.of(datasources.postgreSQL())));

        final var queries = Stream.of(generatedQueries).map(q -> queryFiller.fillQuery(q)).toList();



        // from QueryToInstance
        final var provider = new DefaultControlWrapperProvider();
        final var kinds = testDatasources.stream()
            .flatMap(testDatasource -> {
                provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                return testDatasource.mappings.stream();
            }).toList();

        final var results = new ArrayList<ResultRow>();

        for (int iteration = 0; iteration < REPETITIONS; iteration++) {
            for (int idx = 0; idx < queries.size(); idx++) {
                final var q = queries.get(idx);
                final var filledQuery = q.generateQuery();
                final var queryToInstance = new QueryToInstance(provider, datasources.schema, filledQuery, kinds, cache);

                final var execution = queryToInstance.execute();

                // final var plan = queryToInstance.getPlan();

                results.add(new ResultRow(idx, execution.evaluationTimeInMs(), ""));

            }
        }

        final var cal = new GregorianCalendar();
        final var filename = "cal.com-benchmark-" +
            String.format("%04d", cal.get(Calendar.YEAR)) + "-" +
            String.format("%02d", cal.get(Calendar.MONTH) + 1) + "-" +
            String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + "-" +
            cal.getTimeInMillis() +
            ".csv";

        try (final var writer = new BufferedWriter(new FileWriter("../../data/" + filename))) {
            writer.write("queryIdx,executionMs\n");
            for (final var row : results) {
                writer.write(Integer.toString(row.queryIdx));
                writer.write(",");
                writer.write(Long.toString(row.executionMs));
                writer.write("\n");
            }
            LOGGER.info("Written benchmark contents into file.");
        } catch (IOException e) {
            LOGGER.error("Writing benchmark result error: " + e.getMessage());
        }

        return new ResultsAndFile(results, filename);
    }

    // region generatedQueries

    public static final String[] generatedQueries = {
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
             235 ?time ;
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
             235 ?time ;
             234/41 ?userId .
    FILTER(?time >= "&235")
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

// REMOVED FOR JOINING THROUGH ARRAYS IN MONGO
//         """
// SELECT {
//     ?user userId ?uId ;
//           orgFeatureIds ?featureIds .
// }
// WHERE {
//     ?user 41 ?uId ;
//           -61/62/-181/182/162 ?featureNames ;
//           -61/62/-181/182/161 ?featureIds .

//     FILTER(?uId = "&41")
// }
//         """,

//         """
// SELECT {
//     ?booking title ?bTitle ;
//              attendeeEmails ?attEmails ;
//              hostAvailabilityStarts ?hostAvailStarts .
// }
// WHERE {
//     ?booking 232 ?bTitle ;
//              -243/242 ?attEmails ;
//              234/-93/-116/112 ?hostAvailStarts .

//     FILTER(?bTitle = "&232")
// }
//         """,

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
             235 ?bTime ;
             -243/242 ?attEmails .

    FILTER(?bTime >= "&235")
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

//         """
// SELECT {
//     ?team teamName ?tName ;
//           memberOOOStarts ?oooStarts ;
//           memberOOOEnds ?oooEnds .
// }
// WHERE {
//     ?team 2 ?tName ;
//           1 ?tId ;
//           -55/54/-124/122 ?oooStarts ;
//           -55/54/-124/123 ?oooEnds .

//     FILTER(?oooEnds < "&123")
//     FILTER(?tId = "&1")
// }
//         """,

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

//         """
// SELECT {
//     ?team name ?teamName ;
//           members ?user .
// }
// WHERE {
//     ?team 1 ?teamId ;
//           2 ?teamName ;
//           -55/54/42 ?user .

//     FILTER(?teamId = "&1")
// }
//         """,

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
        """
    };

    public static final String[] generatedQueriesManyJoins = {
        """
SELECT {
    ?user username ?username ;
          teamNames ?teamName ;
          roleNames ?roleName ;
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
        """
    };

    public static final String[] generatedQueriesManyFilters = {
        """

        """,

        """

        """,

        """

        """,

        """

        """,

        """

        """,

        """

        """,

        """

        """,

        """

        """,

        """

        """,

        """

        """
    };

    // endregion
}
