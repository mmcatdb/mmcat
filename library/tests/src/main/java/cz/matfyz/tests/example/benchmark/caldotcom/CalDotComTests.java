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

    public static ResultsAndFile systemTest() {
        final int REPETITIONS = 5;

        final List<TestDatasource<?>> testDatasources = List.of(
            datasources.postgreSQL() // ,
            // datasources.mongoDB(),
            // datasources.neo4j()
        );

        final var queryFiller = new FilterQueryFiller(
            new ValueGenerator(datasources.schema, testDatasources));

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

        // TODO: check validity of directory when launching from server, for now I'm assuming library/server
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

    public static final String[] generatedQueries = new String[] {
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
          description ?desc .
}
WHERE {
    ?role 11 ?id ;
          12 ?name ;
          13 ?desc .
    FILTER(?id = "#11")
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
}
        """,


        // Gemini
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

    FILTER(?username = "#42")
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

    FILTER(?wfName = "#192")
}
        """,

        """
SELECT {
    ?step action ?stepAction ;
          stepNumber ?stepNum .
}
WHERE {
    ?step 203 ?stepAction ;
          202 ?stepNum .

    FILTER(?stepAction = "#203")
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

    FILTER(?emailValue = "#82")
}
        """,

//         """
// SELECT {
//     ?team id ?tId ;
//           name ?tName .
// }
// WHERE {
//     ?team 1 ?tId ;
//           2 ?tName .

//     FILTER(?tId = "#1")
// }
//         """,

//         """
// SELECT {
//     ?user userId ?uId ;
//           orgFeatureNames ?featureNames ;
//           orgFeatureIds ?featureIds .
// }
// WHERE {
//     ?user 41 ?uId ;
//           -61/62/-181/182/162 ?featureNames ;
//           -61/62/-181/182/161 ?featureIds .

//     FILTER(?uId = "#41")
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

//     FILTER(?bTitle = "#232")
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

    FILTER(?wfName = "#192")
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

    FILTER(?bTime >= "#235")
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

    FILTER(?aStarts < "#112")
    FILTER(?uId = "#41")
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

    FILTER(?oooEnds < "#123")
    FILTER(?tId = "#1")
}
        """
    };

}
