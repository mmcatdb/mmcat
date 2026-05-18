package cz.matfyz.tests.example.benchmark.caldotcom;

import cz.matfyz.abstractwrappers.BaseControlWrapper.DefaultControlWrapperProvider;
import cz.matfyz.core.utils.GraphUtils;
import cz.matfyz.querying.QueryToInstance;
import cz.matfyz.querying.core.QueryExecution;
import cz.matfyz.querying.core.querytree.DatasourceNode;
import cz.matfyz.querying.core.querytree.FilterNode;
import cz.matfyz.querying.optimizer.CollectorCache;
import cz.matfyz.querying.optimizer.QueryOptimizer;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.tests.querying.FilterQueryFiller;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CalDotComTests {
    @SuppressWarnings({ "java:s1068" })
    private static final Logger LOGGER = LoggerFactory.getLogger(CalDotComTests.class);

    public static final Datasources datasources = new Datasources();

    static final List<TestDatasource<?>> testDatasources = List.of(
        datasources.mongoDB(),
        datasources.postgreSQL(),
        datasources.neo4j()
    );


    public static void testFunctional() {

        final var queryFiller = new FilterQueryFiller(
            new ValueGenerator(datasources.schema, List.of(datasources.postgreSQL())));

        final var queries = Stream.of(Queries.queries()).map(q -> queryFiller.fillQuery(q)).toList();

        final var allTestDatasources = List.of(
            List.of(datasources.postgreSQL()),
            List.of(datasources.mongoDB()),
            List.of(datasources.neo4j()),
            testDatasources
        );
        Exception[] exceptions = new Exception[queries.size() * allTestDatasources.size()];

        for (int datasourceI = 0; datasourceI < testDatasources.size(); datasourceI++) {
            final var singleTestDatasources = allTestDatasources.get(datasourceI);
            final var cache = new CollectorCache();

            for (int queryI : Queries.ids()) {
                // from QueryToInstance
                final var provider = new DefaultControlWrapperProvider();
                final var kinds = singleTestDatasources.stream()
                    .flatMap(testDatasource -> {
                        provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                        return testDatasource.mappings.stream();
                    }).toList();

                try {
                    final int REPETITIONS = 5;
                    for (int i = 0; i < REPETITIONS; i++) {
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
        System.out.println("TOTAL " + Queries.ids().size());
    }

    public static ResultsAndFile systemTest(List<TestDatasource<?>> testDatasources, int repetitions, String fileId) {

        final var queryFiller = new FilterQueryFiller(
            new ValueGenerator(datasources.schema, List.of(datasources.postgreSQL())));

        final var queries = Stream.of(Queries.queries()).map(q -> queryFiller.fillQuery(q)).toList();

        final var cache = new CollectorCache();

        // from QueryToInstance
        final var provider = new DefaultControlWrapperProvider();
        final var kinds = testDatasources.stream()
            .flatMap(testDatasource -> {
                provider.setControlWrapper(testDatasource.datasource(), testDatasource.wrapper);
                return testDatasource.mappings.stream();
            }).toList();

        final var results = new ArrayList<ResultRow>();

        for (int iteration = 0; iteration < repetitions; iteration++) {
            for (final var idx : Queries.ids()) {
                try {
                    LOGGER.info("Query: " + idx + " of " + queries.size() + " (rep. " + iteration + ")");

                    if (!QueryOptimizer.predicatePushdown &&
                        (
                            (Queries.unoptIgnoredIdsPSQL().contains(idx) && testDatasources.contains(datasources.postgreSQL())) ||
                            (Queries.unoptIgnoredIdsMONGO().contains(idx) && testDatasources.contains(datasources.mongoDB())) ||
                            (Queries.unoptIgnoredIdsNEO().contains(idx) && testDatasources.contains(datasources.neo4j()))
                        )
                    ) {
                        throw new Exception("ignored-out-of-memory");
                    }

                    final var q = queries.get(idx);
                    final var filledQuery = q.generateQuery();
                    final var queryToInstance = new QueryToInstance(provider, datasources.schema, filledQuery, kinds, cache);

                    final var execution = queryToInstance.execute();

                    // final var plan = queryToInstance.getPlan();

                    results.add(new ResultRow(idx, execution));
                } catch (java.lang.OutOfMemoryError e) {
                    System.gc(); // Just to be sure
                    results.add(new ResultRow(idx, "out-of-memory"));
                } catch (Exception e) {
                    results.add(new ResultRow(idx, e.getMessage()));
                }
            }
        }

        return new ResultsAndFile(results, exportToCSV(results, fileId));
    }

    public static String exportToCSV(List<ResultRow> rows, String fileId) {
        // final var cal = new GregorianCalendar();

        final var filename = "cal.com-benchmark-" +
            fileId +
            // "-" +
            // String.format("%04d", cal.get(Calendar.YEAR)) + "-" +
            // String.format("%02d", cal.get(Calendar.MONTH) + 1) + "-" +
            // String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + "-" +
            // cal.getTimeInMillis() +
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



    public static record ResultRow(
        int queryIdx, // TODO maybe use this directly alongside QueryExecution?
        String error,
        double planningTimeInMs,
        double underlyingDBMSSelectionTimeInMs,
        double innerSelectionTimeInMs,
        double projectionTimeInMs,
        int resultRowCount,
        int kinds,
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
        private static int getKindCount(QueryExecution execution) {
            var kinds = new MutInt();
            GraphUtils.forEachDFS(execution.plan().root, node -> {
                if (node instanceof DatasourceNode dsNode) kinds.v += dsNode.kinds.size();
            });
            return kinds.v;
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
                null,
                execution.planningTimeInMs(),
                execution.underlyingDBMSSelectionTimeInMs(),
                Math.max(execution.selectionTimeInMs() - execution.underlyingDBMSSelectionTimeInMs(), 0), // prevent rounding errors to negative numbers (does not look nice)
                execution.projectionTimeInMs(),
                execution.result().children().size(),
                getKindCount(execution),
                getDatasourceNodes(execution),
                getFilterNodes(execution)
            );
        }

        public ResultRow(int queryIdx, String errorMsg) {
            this(
                queryIdx,
                errorMsg,
                0,
                0,
                0,
                0,
                0,
                0,
                0,
                0
            );
        }

        public static void writeCSVHeader(BufferedWriter writer) throws IOException {
            writer.write("queryIdx,");
            writer.write("error,");
            writer.write("planningMs,");
            writer.write("underlyingSelectionMs,");
            writer.write("innerSelectionMs,");
            writer.write("projectionMs,");
            writer.write("resultRowCount,");
            writer.write("numberOfKinds,");
            writer.write("datasourceNodes,");
            writer.write("filterNodes");
            writer.write("\n");
        }

        public void writeCSVRow(BufferedWriter writer) throws IOException {
            writer.write(Integer.toString(queryIdx));
            writer.write(",");
            writer.write(error() == null ? "" : error());
            writer.write(",");
            writer.write(Double.toString(planningTimeInMs()));
            writer.write(",");
            writer.write(Double.toString(underlyingDBMSSelectionTimeInMs()));
            writer.write(",");
            writer.write(Double.toString(innerSelectionTimeInMs()));
            writer.write(",");
            writer.write(Double.toString(projectionTimeInMs()));

            writer.write(",");
            writer.write(Integer.toString(resultRowCount()));
            writer.write(",");
            writer.write(Integer.toString(kinds()));
            writer.write(",");
            writer.write(Integer.toString(datasourceNodes()));
            writer.write(",");
            writer.write(Integer.toString(filterNodes()));
            writer.write("\n");
        }
    }
    public static record ResultsAndFile(List<ResultRow> results, String filename) {}

}
