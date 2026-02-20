package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.*;

import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.tests.example.common.DatasourceProvider;
import cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for cross-database evolution propagation.
 * Executes actual SQL and MongoDB commands against real databases
 * with Yelp benchmark data (10 businesses, 100 users, 300 reviews).
 *
 * Prerequisites:
 * - Docker containers running (docker compose up -d)
 * - Yelp data loaded (cd experiments/benchmarkDataSetup && npx tsx yelp.ts)
 *
 * Run: mvn test -pl tests -Dtest="cz.matfyz.tests.evolution.YelpEvolutionIntegrationTests"
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class YelpEvolutionIntegrationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(YelpEvolutionIntegrationTests.class);

    private static final DatasourceProvider datasourceProvider = new DatasourceProvider("tests", "benchmark_yelp");
    private static final PostgreSQLProvider pgProvider = datasourceProvider.getPostgreSQLProvider();
    private static final MongoDBProvider mongoProvider = datasourceProvider.getMongoDBProvider();

    private static final PostgreSQLEvolutionWrapper pgWrapper = new PostgreSQLEvolutionWrapper();
    private static final MongoDBEvolutionWrapper mongoWrapper = new MongoDBEvolutionWrapper();

    // ==================== PostgreSQL Tests ====================

    @Test
    @Order(1)
    void pg_addProperty() throws SQLException {
        LOGGER.info("=== PostgreSQL: Add property 'website' to business ===");

        final var statements = pgWrapper.evolveAddProperty("business", "website", false);
        executePgStatements(statements);

        // Verify: column exists with NULL values
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"website\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next(), "Should have at least one row");
            assertNull(rs.getString("website"), "New column should have NULL values");
        }

        LOGGER.info("PostgreSQL AddProperty: OK");
    }

    @Test
    @Order(2)
    void pg_removeProperty() throws SQLException {
        LOGGER.info("=== PostgreSQL: Remove property 'website' from business ===");

        final var statements = pgWrapper.evolveRemoveProperty("business", "website");
        executePgStatements(statements);

        // Verify: column no longer exists
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM \"business\" LIMIT 1")) {
            assertFalse(hasColumn(rs, "website"), "Column 'website' should be removed");
        }

        LOGGER.info("PostgreSQL RemoveProperty: OK");
    }

    @Test
    @Order(3)
    void pg_joinProperties() throws SQLException {
        LOGGER.info("=== PostgreSQL: Join city + state -> location ===");

        // First, capture original values for verification
        String originalCity;
        String originalState;
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"city\", \"state\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            originalCity = rs.getString("city");
            originalState = rs.getString("state");
        }

        final var statements = pgWrapper.evolveJoinProperties(
            "business", "location",
            List.of("city", "state"), ", ",
            false // keep source columns for now
        );
        executePgStatements(statements);

        // Verify: location column contains "city, state"
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"location\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            final String location = rs.getString("location");
            assertNotNull(location, "Location should not be null");
            assertEquals(originalCity + ", " + originalState, location,
                "Location should be 'city, state'");
        }

        LOGGER.info("PostgreSQL JoinProperties: OK — location = '%s, %s'", originalCity, originalState);
    }

    @Test
    @Order(4)
    void pg_splitProperty() throws SQLException {
        LOGGER.info("=== PostgreSQL: Split location -> city2, state2 ===");

        final var statements = pgWrapper.evolveSplitProperty(
            "business", "location",
            List.of("city2", "state2"), ", ",
            true // delete source column
        );
        executePgStatements(statements);

        // Verify: city2 and state2 match original city and state
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT \"city\", \"state\", \"city2\", \"state2\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            assertEquals(rs.getString("city"), rs.getString("city2"),
                "city2 should match original city");
            assertEquals(rs.getString("state"), rs.getString("state2"),
                "state2 should match original state");
        }

        // Verify: location column is dropped
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM \"business\" LIMIT 1")) {
            assertFalse(hasColumn(rs, "location"), "Column 'location' should be removed after split");
        }

        LOGGER.info("PostgreSQL SplitProperty: OK");
    }

    @Test
    @Order(5)
    void pg_cleanupSplitColumns() throws SQLException {
        LOGGER.info("=== PostgreSQL: Cleanup — drop city2, state2 ===");

        final var stmts1 = pgWrapper.evolveRemoveProperty("business", "city2");
        final var stmts2 = pgWrapper.evolveRemoveProperty("business", "state2");
        executePgStatements(stmts1);
        executePgStatements(stmts2);

        LOGGER.info("PostgreSQL Cleanup: OK");
    }

    @Test
    @Order(6)
    void pg_groupProperties() throws SQLException {
        LOGGER.info("=== PostgreSQL: Group name + is_open -> info (JSONB) ===");

        // Capture original values
        String originalName;
        String originalIsOpen;
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"name\", \"is_open\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            originalName = rs.getString("name");
            originalIsOpen = rs.getString("is_open");
        }

        final var statements = pgWrapper.evolveGroupProperties(
            "business", "info",
            List.of("name", "is_open")
        );
        executePgStatements(statements);

        // Verify: info JSONB column contains name and is_open
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"info\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            final String infoJson = rs.getString("info");
            assertNotNull(infoJson, "JSONB column 'info' should not be null");
            assertTrue(infoJson.contains(originalName),
                "JSONB should contain original name: " + originalName);
        }

        // Verify: name and is_open columns are dropped
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM \"business\" LIMIT 1")) {
            assertFalse(hasColumn(rs, "name"), "Column 'name' should be grouped into 'info'");
            assertFalse(hasColumn(rs, "is_open"), "Column 'is_open' should be grouped into 'info'");
            assertTrue(hasColumn(rs, "info"), "Column 'info' should exist");
        }

        LOGGER.info("PostgreSQL GroupProperties: OK — grouped into JSONB");
    }

    @Test
    @Order(7)
    void pg_ungroupProperty() throws SQLException {
        LOGGER.info("=== PostgreSQL: Ungroup info -> name, is_open ===");

        final var statements = pgWrapper.evolveUngroupProperty(
            "business", "info",
            List.of("name", "is_open")
        );
        executePgStatements(statements);

        // Verify: name and is_open columns are restored
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT \"name\", \"is_open\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            assertNotNull(rs.getString("name"), "name should be restored");
            assertNotNull(rs.getString("is_open"), "is_open should be restored");
        }

        // Verify: info column is dropped
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM \"business\" LIMIT 1")) {
            assertFalse(hasColumn(rs, "info"), "Column 'info' should be removed after ungroup");
            assertTrue(hasColumn(rs, "name"), "Column 'name' should be restored");
            assertTrue(hasColumn(rs, "is_open"), "Column 'is_open' should be restored");
        }

        LOGGER.info("PostgreSQL UngroupProperty: OK — restored from JSONB");
    }

    @Test
    @Order(8)
    void pg_verifyDataIntegrity() throws SQLException {
        LOGGER.info("=== PostgreSQL: Verify data integrity — 10 businesses still intact ===");

        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) AS cnt FROM \"business\"")) {
            assertTrue(rs.next());
            assertEquals(10, rs.getInt("cnt"), "Should still have 10 businesses");
        }

        // Verify all original columns exist with non-null data
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT \"business_id\", \"name\", \"city\", \"state\", \"is_open\" FROM \"business\"")) {
            int count = 0;
            while (rs.next()) {
                assertNotNull(rs.getString("business_id"), "business_id should not be null");
                assertNotNull(rs.getString("name"), "name should not be null");
                assertNotNull(rs.getString("city"), "city should not be null");
                assertNotNull(rs.getString("state"), "state should not be null");
                count++;
            }
            assertEquals(10, count, "All 10 rows should have complete data");
        }

        LOGGER.info("PostgreSQL DataIntegrity: OK — all 10 businesses intact");
    }

    // ==================== MongoDB Tests ====================

    @Test
    @Order(20)
    void mongo_addProperty() {
        LOGGER.info("=== MongoDB: Add property 'website' to business (no-op for schemaless) ===");

        final var statements = mongoWrapper.evolveAddProperty("business", "website", false);
        // MongoDB is schemaless — evolveAddProperty returns empty list
        assertTrue(statements.isEmpty(), "MongoDB AddProperty should be no-op");

        LOGGER.info("MongoDB AddProperty: OK (no-op)");
    }

    @Test
    @Order(21)
    void mongo_joinProperties() {
        LOGGER.info("=== MongoDB: Join city + state -> location ===");

        // Capture original values
        final MongoDatabase db = mongoProvider.getDatabase();
        final Document originalDoc = db.getCollection("business").find().first();
        assertNotNull(originalDoc);
        final String originalCity = originalDoc.getString("city");
        final String originalState = originalDoc.getString("state");

        final var statements = mongoWrapper.evolveJoinProperties(
            "business", "location",
            List.of("city", "state"), ", ",
            false // keep source fields
        );
        executeMongoStatements(statements);

        // Verify: location field contains "city, state"
        final Document updatedDoc = db.getCollection("business").find().first();
        assertNotNull(updatedDoc);
        final String location = updatedDoc.getString("location");
        assertNotNull(location, "location field should exist");
        assertEquals(originalCity + ", " + originalState, location,
            "location should be 'city, state'");

        LOGGER.info("MongoDB JoinProperties: OK — location = '{}, {}'", originalCity, originalState);
    }

    @Test
    @Order(22)
    void mongo_splitProperty() {
        LOGGER.info("=== MongoDB: Split location -> city2, state2 ===");

        final MongoDatabase db = mongoProvider.getDatabase();

        final var statements = mongoWrapper.evolveSplitProperty(
            "business", "location",
            List.of("city2", "state2"), ", ",
            true // delete source
        );
        executeMongoStatements(statements);

        // Verify: city2 and state2 match original city and state
        final Document doc = db.getCollection("business").find().first();
        assertNotNull(doc);
        assertEquals(doc.getString("city"), doc.getString("city2"),
            "city2 should match original city");
        assertEquals(doc.getString("state"), doc.getString("state2"),
            "state2 should match original state");
        assertNull(doc.getString("location"), "location should be removed after split");

        LOGGER.info("MongoDB SplitProperty: OK");
    }

    @Test
    @Order(23)
    void mongo_cleanupSplitFields() {
        LOGGER.info("=== MongoDB: Cleanup — remove city2, state2 ===");

        final var stmts1 = mongoWrapper.evolveRemoveProperty("business", "city2");
        final var stmts2 = mongoWrapper.evolveRemoveProperty("business", "state2");
        executeMongoStatements(stmts1);
        executeMongoStatements(stmts2);

        final MongoDatabase db = mongoProvider.getDatabase();
        final Document doc = db.getCollection("business").find().first();
        assertNotNull(doc);
        assertFalse(doc.containsKey("city2"), "city2 should be removed");
        assertFalse(doc.containsKey("state2"), "state2 should be removed");

        LOGGER.info("MongoDB Cleanup: OK");
    }

    @Test
    @Order(24)
    void mongo_groupProperties() {
        LOGGER.info("=== MongoDB: Group name + is_open -> info (nested document) ===");

        final MongoDatabase db = mongoProvider.getDatabase();

        // Capture original values
        final Document originalDoc = db.getCollection("business").find().first();
        assertNotNull(originalDoc);
        final String originalName = originalDoc.getString("name");

        final var statements = mongoWrapper.evolveGroupProperties(
            "business", "info",
            List.of("name", "is_open")
        );
        executeMongoStatements(statements);

        // Verify: info nested document contains name and is_open
        final Document updatedDoc = db.getCollection("business").find().first();
        assertNotNull(updatedDoc);
        final Document info = updatedDoc.get("info", Document.class);
        assertNotNull(info, "Nested document 'info' should exist");
        assertEquals(originalName, info.getString("name"),
            "Nested name should match original");
        assertNotNull(info.get("is_open"), "Nested is_open should exist");

        // Original top-level fields should be removed
        assertFalse(updatedDoc.containsKey("name"),
            "Top-level 'name' should be removed after GROUP");
        assertFalse(updatedDoc.containsKey("is_open"),
            "Top-level 'is_open' should be removed after GROUP");

        LOGGER.info("MongoDB GroupProperties: OK — nested document created");
    }

    @Test
    @Order(25)
    void mongo_ungroupProperty() {
        LOGGER.info("=== MongoDB: Ungroup info -> name, is_open ===");

        final MongoDatabase db = mongoProvider.getDatabase();

        final var statements = mongoWrapper.evolveUngroupProperty(
            "business", "info",
            List.of("name", "is_open")
        );
        executeMongoStatements(statements);

        // Verify: name and is_open are back at top level
        final Document doc = db.getCollection("business").find().first();
        assertNotNull(doc);
        assertNotNull(doc.getString("name"), "name should be restored");
        assertNotNull(doc.get("is_open"), "is_open should be restored");
        assertFalse(doc.containsKey("info"), "info should be removed after UNGROUP");

        LOGGER.info("MongoDB UngroupProperty: OK — restored from nested document");
    }

    @Test
    @Order(26)
    void mongo_verifyDataIntegrity() {
        LOGGER.info("=== MongoDB: Verify data integrity — 10 businesses still intact ===");

        final MongoDatabase db = mongoProvider.getDatabase();
        final long count = db.getCollection("business").countDocuments();
        assertEquals(10, count, "Should still have 10 businesses");

        // Verify all documents have the expected fields
        for (final Document doc : db.getCollection("business").find()) {
            assertNotNull(doc.getString("business_id"), "business_id should not be null");
            assertNotNull(doc.getString("name"), "name should not be null");
            assertNotNull(doc.getString("city"), "city should not be null");
            assertNotNull(doc.getString("state"), "state should not be null");
        }

        LOGGER.info("MongoDB DataIntegrity: OK — all 10 businesses intact");
    }

    // ==================== Helpers ====================

    private void executePgStatements(List<AbstractStatement> statements) {
        try (Connection conn = pgProvider.getConnection()) {
            for (final var statement : statements) {
                LOGGER.info("Executing SQL: {}", statement.getContent());
                try (var preparedStatement = conn.prepareStatement(statement.getContent())) {
                    preparedStatement.execute();
                }
            }
        } catch (SQLException e) {
            fail("PostgreSQL statement execution failed: " + e.getMessage(), e);
        }
    }

    private void executeMongoStatements(List<AbstractStatement> statements) {
        final MongoDatabase db = mongoProvider.getDatabase();
        for (final var statement : statements) {
            LOGGER.info("Executing MongoDB: {}", statement.getContent());
            if (statement instanceof cz.matfyz.wrappermongodb.MongoDBCommandStatement commandStatement) {
                db.runCommand(commandStatement.getCommand());
            }
        }
    }

    private boolean hasColumn(ResultSet rs, String columnName) throws SQLException {
        final ResultSetMetaData meta = rs.getMetaData();
        for (int i = 1; i <= meta.getColumnCount(); i++) {
            if (meta.getColumnName(i).equalsIgnoreCase(columnName)) {
                return true;
            }
        }
        return false;
    }
}
