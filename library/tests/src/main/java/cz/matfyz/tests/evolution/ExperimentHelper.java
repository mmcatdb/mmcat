package cz.matfyz.tests.evolution;

import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for experimental evaluation.
 * Provides data reload, timing, CSV writing, and database helpers.
 */
public class ExperimentHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentHelper.class);

    // ==================== Timing ====================

    public record TimedResult<T>(T value, long elapsedMs) {}

    public static <T> TimedResult<T> timed(Supplier<T> action) {
        final long start = System.nanoTime();
        final T result = action.get();
        final long elapsed = (System.nanoTime() - start) / 1_000_000;
        return new TimedResult<>(result, elapsed);
    }

    public static long timedVoid(Runnable action) {
        final long start = System.nanoTime();
        action.run();
        return (System.nanoTime() - start) / 1_000_000;
    }

    // ==================== Data Reload ====================

    /**
     * Reloads Yelp benchmark data at the given scale factor by running yelp.ts.
     * Scale 1.0 = 10 businesses, 100 users, 300 reviews.
     */
    public static void reloadYelpData(double scaleFactor) throws Exception {
        final Path projectRoot = Path.of(System.getProperty("user.dir")).resolve("../..").normalize();
        final Path setupDir = projectRoot.resolve("experiments/benchmarkDataSetup");

        LOGGER.info("Reloading Yelp data at {}x scale from {}", scaleFactor, setupDir);

        // Use bash -lc to ensure nvm/Node 20 is available (import.meta.dirname requires Node 20+)
        final String command = String.format(
            "source ~/.nvm/nvm.sh && nvm use 20 > /dev/null 2>&1 && npx tsx yelp.ts %s",
            scaleFactor
        );
        final ProcessBuilder pb = new ProcessBuilder("bash", "-c", command);
        pb.directory(setupDir.toFile());
        pb.redirectErrorStream(true);

        final Process process = pb.start();
        // Consume output to prevent blocking (don't use inheritIO — it corrupts Surefire streams)
        try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                LOGGER.debug("yelp.ts: {}", line);
            }
        }
        final int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("yelp.ts data setup failed with exit code: " + exitCode);
        }

        LOGGER.info("Yelp data reloaded at {}x scale", scaleFactor);
    }

    // ==================== Row Counting ====================

    public static long countPgRows(PostgreSQLProvider pgProvider, String tableName) throws SQLException {
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT COUNT(*) AS cnt FROM \"" + tableName + "\"")) {
            rs.next();
            return rs.getLong("cnt");
        }
    }

    public static long countMongoRows(MongoDBProvider mongoProvider, String collectionName) {
        return mongoProvider.getDatabase().getCollection(collectionName).countDocuments();
    }

    public static long countPgNulls(PostgreSQLProvider pgProvider, String tableName, String columnName) throws SQLException {
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT COUNT(*) AS cnt FROM \"" + tableName + "\" WHERE \"" + columnName + "\" IS NULL")) {
            rs.next();
            return rs.getLong("cnt");
        }
    }

    // ==================== Database Cleanup ====================

    public static void resetPgColumns(PostgreSQLProvider pgProvider, String tableName, String... columns) throws SQLException {
        try (Connection conn = pgProvider.getConnection()) {
            for (final String col : columns) {
                conn.createStatement().execute(
                    "ALTER TABLE \"" + tableName + "\" DROP COLUMN IF EXISTS \"" + col + "\";"
                );
            }
        }
    }

    public static void resetMongoFields(MongoDBProvider mongoProvider, String collectionName, String... fields) {
        if (fields.length > 0) {
            final var db = mongoProvider.getDatabase();
            final var unsetDoc = new org.bson.Document();
            for (final String field : fields) {
                unsetDoc.append(field, "");
            }
            db.getCollection(collectionName).updateMany(
                new org.bson.Document(),
                new org.bson.Document("$unset", unsetDoc)
            );
        }
    }

    // ==================== CSV Writing ====================

    private static Path resolveResultsDir() throws IOException {
        final Path projectRoot = Path.of(System.getProperty("user.dir")).resolve("../..").normalize();
        final Path resultsDir = projectRoot.resolve("experiments/results");
        Files.createDirectories(resultsDir);
        return resultsDir;
    }

    /**
     * Writes a CSV header, overwriting any existing file.
     */
    public static void writeCsvHeader(String filename, String... headers) throws IOException {
        final Path file = resolveResultsDir().resolve(filename);
        try (var writer = Files.newBufferedWriter(file)) {
            writer.write(String.join(",", headers));
            writer.newLine();
        }
    }

    /**
     * Appends a row to an existing CSV file.
     */
    public static void appendCsvRow(String filename, String... values) throws IOException {
        final Path file = resolveResultsDir().resolve(filename);
        try (var writer = Files.newBufferedWriter(file, StandardOpenOption.CREATE, StandardOpenOption.APPEND)) {
            writer.write(String.join(",", values));
            writer.newLine();
        }
    }
}
