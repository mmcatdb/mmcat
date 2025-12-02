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
            datasources.postgreSQL(),
            datasources.mongoDB(),
            datasources.neo4j()
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
                writer.write(row.queryIdx);
                writer.write(",");
                writer.write(Long.toString(row.executionMs));
                writer.write("\n");
            }
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
    ?eventType id ?id ;
               title ?title ;
               parentIds ?parentId .
}
WHERE {
    ?eventType 101 ?id ;
               102 ?title ;
               -107/101 ?parentId .
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



        """
SELECT {
    ?team id ?teamId ;
          name ?teamName ;
          eventTypes ?etype .

    ?etype id ?etypeId ;
           title ?etypeTitle ;
           schedule ?schedule .

    ?schedule id ?scheduleId ;
              name ?scheduleName .
}
WHERE {
    ?team 1 ?teamId ;
          2 ?teamName ;
          -105 ?etype .

    ?etype 101 ?etypeId ;
           102 ?etypeTitle ;
           106 ?schedule .

    ?schedule 91 ?scheduleId ;
              92 ?scheduleName .
}
        """,

        """
SELECT {
    ?user id ?userId ;
          username ?username ;
          memberships ?m .

    ?m id ?mId ;
       accepted ?mAccepted ;
       team ?team ;
       role ?role .

    ?team id ?teamId ;
          name ?teamName .

    ?role id ?roleId ;
          name ?roleName .
}
WHERE {
    ?user 41 ?userId ;
          42 ?username ;
          -54 ?m .

    ?m 51 ?mId ;
       52 ?mAccepted ;
       55 ?team ;
       56 ?role .

    ?team 1 ?teamId ;
          2 ?teamName .

    ?role 11 ?roleId ;
          12 ?roleName .
}
        """,
    };

}
