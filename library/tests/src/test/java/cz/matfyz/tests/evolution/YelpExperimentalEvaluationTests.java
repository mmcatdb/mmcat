package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.*;

import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataMorphism;
import cz.matfyz.core.metadata.MetadataObjex;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataMorphism;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadataObjex;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;
import cz.matfyz.evolution.category.*;
import cz.matfyz.evolution.category.GroupProperties.MovedMorphism;
import cz.matfyz.evolution.instance.GraphBasedOptimizer;
import cz.matfyz.evolution.instance.OperationOptimizer;
import cz.matfyz.evolution.instance.OperationOptimizer.OptimizationComparison;
import cz.matfyz.evolution.propagation.EvolutionPropagator;
import cz.matfyz.evolution.propagation.MappingEvolver;
import cz.matfyz.tests.example.benchmark.yelp.Datasources;
import cz.matfyz.tests.example.benchmark.yelp.Schema;
import cz.matfyz.tests.example.common.DatasourceProvider;
import cz.matfyz.tests.evolution.ExperimentHelper.TimedResult;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Experimental evaluation for thesis Chapter 5.
 * Runs 5 experiments (E1-E5) at a given data scale, writes results to CSV.
 *
 * Not meant to be run directly — use {@link YelpScaledExperimentRunner}.
 */
class YelpExperimentalEvaluationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(YelpExperimentalEvaluationTests.class);

    private static final DatasourceProvider datasourceProvider = new DatasourceProvider("tests", "benchmark_yelp");
    private static final PostgreSQLProvider pgProvider = datasourceProvider.getPostgreSQLProvider();
    private static final MongoDBProvider mongoProvider = datasourceProvider.getMongoDBProvider();

    private final double scaleFactor;
    private final long expectedBusinesses;
    private final long expectedUsers;
    private final long expectedReviews;

    // Collected results for CSV writing
    private final List<String[]> e1Results = new ArrayList<>();
    private final List<String[]> e3Results = new ArrayList<>();

    YelpExperimentalEvaluationTests(double scaleFactor) {
        this.scaleFactor = scaleFactor;
        this.expectedBusinesses = Math.round(10 * scaleFactor);
        this.expectedUsers = Math.round(100 * scaleFactor);
        this.expectedReviews = Math.round(300 * scaleFactor);
    }

    /** Runs all 5 experiments sequentially. */
    void runAll() throws Exception {
        LOGGER.info("========== Running experiments at {}x scale ==========", scaleFactor);
        LOGGER.info("Expected: {} businesses, {} users, {} reviews", expectedBusinesses, expectedUsers, expectedReviews);

        verifyDataLoaded();
        runE1_PropagationPerformance();
        runE2_OptimizerComparison();
        runE3_DataIntegrity();
        runE4_JoinSplitCorrectness();
        runE5_IncrementalVsReExport();

        LOGGER.info("========== All experiments complete at {}x scale ==========", scaleFactor);
    }

    // ==================== Data Verification ====================

    private void verifyDataLoaded() throws SQLException {
        assertEquals(expectedBusinesses, ExperimentHelper.countPgRows(pgProvider, "business"),
            "PG business count mismatch at " + scaleFactor + "x");
        assertEquals(expectedUsers, ExperimentHelper.countPgRows(pgProvider, "yelp_user"),
            "PG user count mismatch at " + scaleFactor + "x");
        assertEquals(expectedReviews, ExperimentHelper.countPgRows(pgProvider, "review"),
            "PG review count mismatch at " + scaleFactor + "x");

        assertEquals(expectedBusinesses, ExperimentHelper.countMongoRows(mongoProvider, "business"),
            "Mongo business count mismatch at " + scaleFactor + "x");

        LOGGER.info("Data verification OK: {} businesses, {} users, {} reviews",
            expectedBusinesses, expectedUsers, expectedReviews);
    }

    // ==================== E1: Basic Propagation Performance ====================

    private void runE1_PropagationPerformance() throws Exception {
        LOGGER.info("--- E1: Propagation Performance ---");

        // Ensure clean DB state (guard against dirty state from previous failed runs)
        ensurePgCleanState();

        // Test each SMO type on both datasources
        // Each operation is followed by a proper reverse-cleanup

        e1_measureOperation("GROUP", "postgresql", this::e1_pgGroup);
        e1_reverseGroupPg();
        e1_measureOperation("UNGROUP", "postgresql", this::e1_pgUngroup);
        // UNGROUP restores original state, no cleanup needed
        e1_measureOperation("JOIN", "postgresql", this::e1_pgJoin);
        e1_cleanupPg("location");
        e1_measureOperation("SPLIT", "postgresql", this::e1_pgSplit);
        e1_cleanupPg("city_part", "state_part");
        e1_restorePgCityColumn();

        e1_measureOperation("GROUP", "mongodb", this::e1_mongoGroup);
        e1_reverseGroupMongo();
        e1_measureOperation("UNGROUP", "mongodb", this::e1_mongoUngroup);
        // UNGROUP restores original state, no cleanup needed
        e1_measureOperation("JOIN", "mongodb", this::e1_mongoJoin);
        e1_cleanupMongo("location");
        e1_measureOperation("SPLIT", "mongodb", this::e1_mongoSplit);
        e1_cleanupMongo("city_part", "state_part");
        e1_restoreMongoCityField();

        // Write CSV
        for (final String[] row : e1Results) {
            ExperimentHelper.appendCsvRow("e1_propagation_performance.csv", row);
        }

        LOGGER.info("E1 complete: {} measurements recorded", e1Results.size());
    }

    @FunctionalInterface
    private interface OperationMeasurement {
        int[] run() throws Exception; // returns [statementsCount]
    }

    private void e1_measureOperation(String smoType, String datasource, OperationMeasurement measurement) throws Exception {
        final long startGen = System.nanoTime();
        final int[] result = measurement.run();
        final long totalMs = (System.nanoTime() - startGen) / 1_000_000;

        final int stmtCount = result[0];
        e1Results.add(new String[]{
            String.valueOf(scaleFactor), smoType, datasource,
            String.valueOf(stmtCount), String.valueOf(totalMs)
        });

        LOGGER.info("E1 | {}x | {} | {} | {} stmts | {}ms",
            scaleFactor, smoType, datasource, stmtCount, totalMs);
    }

    private int[] e1_pgGroup() throws SQLException {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var op = buildGroupOperation(Schema.business.key(), 100, "info", 100,
            List.of(
                new MovedMorphismSpec(2, 101, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(5, 102, Schema.businessIsOpen.key(), "is_open")
            ), metadata);
        op.up(schema, metadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(), newPgMappings(), metadata);
        final List<AbstractStatement> stmts = op.accept(propagator);
        executePgStatements(stmts);
        return new int[]{stmts.size()};
    }

    private int[] e1_pgUngroup() throws SQLException {
        // First GROUP to set up state
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var groupOp = buildGroupOperation(Schema.business.key(), 100, "info", 100,
            List.of(
                new MovedMorphismSpec(2, 101, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(5, 102, Schema.businessIsOpen.key(), "is_open")
            ), metadata);
        groupOp.up(schema, metadata);

        // Execute GROUP on DB first
        final var groupProp = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(), newPgMappings(), metadata);
        executePgStatements(groupOp.accept(groupProp));

        // Now measure UNGROUP
        final var ungroupOp = buildUngroupOperation(Schema.business.key(), 100, "info", 100,
            List.of(
                new MovedMorphismSpec(101, 2, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(102, 5, Schema.businessIsOpen.key(), "is_open")
            ), metadata);
        ungroupOp.up(schema, metadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(), newPgMappings(), metadata);
        final List<AbstractStatement> stmts = ungroupOp.accept(propagator);
        executePgStatements(stmts);
        return new int[]{stmts.size()};
    }

    private int[] e1_pgJoin() throws SQLException {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var op = buildJoinOperation(Schema.business.key(),
            List.of(
                new SourcePropertySpec(Schema.businessCity.key(), Schema.business_city.signature(), "city"),
                new SourcePropertySpec(Schema.businessState.key(), Schema.business_state.signature(), "state")
            ),
            new TargetPropertySpec(new Key(200), new BaseSignature(200), "location"),
            ", ", false, metadata);
        op.up(schema, metadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(), newPgMappings(), metadata);
        final List<AbstractStatement> stmts = op.accept(propagator);
        executePgStatements(stmts);
        return new int[]{stmts.size()};
    }

    private int[] e1_pgSplit() throws SQLException {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Prepare: add delimiter to city
        try (Connection conn = pgProvider.getConnection()) {
            conn.createStatement().execute("UPDATE \"business\" SET \"city\" = \"city\" || '|' || \"state\";");
        }

        final var op = buildSplitOperation(Schema.business.key(),
            new SourcePropertySpec(Schema.businessCity.key(), Schema.business_city.signature(), "city"),
            List.of(
                new TargetPropertySpec(new Key(201), new BaseSignature(201), "city_part"),
                new TargetPropertySpec(new Key(202), new BaseSignature(202), "state_part")
            ),
            "|", false, metadata);
        op.up(schema, metadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(), newPgMappings(), metadata);
        final List<AbstractStatement> stmts = op.accept(propagator);
        executePgStatements(stmts);
        return new int[]{stmts.size()};
    }

    private int[] e1_mongoGroup() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var op = buildGroupOperation(Schema.business.key(), 100, "info", 100,
            List.of(
                new MovedMorphismSpec(2, 101, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(5, 102, Schema.businessIsOpen.key(), "is_open")
            ), metadata);
        op.up(schema, metadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(), newMongoMappings(), metadata);
        final List<AbstractStatement> stmts = op.accept(propagator);
        executeMongoStatements(stmts);
        return new int[]{stmts.size()};
    }

    private int[] e1_mongoUngroup() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var groupOp = buildGroupOperation(Schema.business.key(), 100, "info", 100,
            List.of(
                new MovedMorphismSpec(2, 101, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(5, 102, Schema.businessIsOpen.key(), "is_open")
            ), metadata);
        groupOp.up(schema, metadata);

        final var groupProp = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(), newMongoMappings(), metadata);
        executeMongoStatements(groupOp.accept(groupProp));

        final var ungroupOp = buildUngroupOperation(Schema.business.key(), 100, "info", 100,
            List.of(
                new MovedMorphismSpec(101, 2, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(102, 5, Schema.businessIsOpen.key(), "is_open")
            ), metadata);
        ungroupOp.up(schema, metadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(), newMongoMappings(), metadata);
        final List<AbstractStatement> stmts = ungroupOp.accept(propagator);
        executeMongoStatements(stmts);
        return new int[]{stmts.size()};
    }

    private int[] e1_mongoJoin() {
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var op = buildJoinOperation(Schema.business.key(),
            List.of(
                new SourcePropertySpec(Schema.businessCity.key(), Schema.business_city.signature(), "city"),
                new SourcePropertySpec(Schema.businessState.key(), Schema.business_state.signature(), "state")
            ),
            new TargetPropertySpec(new Key(200), new BaseSignature(200), "location"),
            ", ", false, metadata);
        op.up(schema, metadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(), newMongoMappings(), metadata);
        final List<AbstractStatement> stmts = op.accept(propagator);
        executeMongoStatements(stmts);
        return new int[]{stmts.size()};
    }

    private int[] e1_mongoSplit() {
        final MongoDatabase db = mongoProvider.getDatabase();
        // Prepare: concatenate city|state
        db.getCollection("business").updateMany(
            new Document(),
            List.of(new Document("$set", new Document("city",
                new Document("$concat", List.of("$city", "|", "$state")))))
        );

        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var op = buildSplitOperation(Schema.business.key(),
            new SourcePropertySpec(Schema.businessCity.key(), Schema.business_city.signature(), "city"),
            List.of(
                new TargetPropertySpec(new Key(201), new BaseSignature(201), "city_part"),
                new TargetPropertySpec(new Key(202), new BaseSignature(202), "state_part")
            ),
            "|", false, metadata);
        op.up(schema, metadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(), newMongoMappings(), metadata);
        final List<AbstractStatement> stmts = op.accept(propagator);
        executeMongoStatements(stmts);
        return new int[]{stmts.size()};
    }

    /** Checks if PG business table has expected columns; reloads data if not. */
    private void ensurePgCleanState() throws Exception {
        try (Connection conn = pgProvider.getConnection()) {
            final ResultSet rs = conn.createStatement().executeQuery(
                "SELECT column_name FROM information_schema.columns " +
                "WHERE table_name='business' AND column_name='name'");
            if (!rs.next()) {
                LOGGER.warn("Database in dirty state (missing 'name' column), reloading data...");
                ExperimentHelper.reloadYelpData(scaleFactor);
            }
        }
    }

    private void e1_cleanupPg(String... columns) throws SQLException {
        ExperimentHelper.resetPgColumns(pgProvider, "business", columns);
    }

    /** Reverse GROUP on PG: extract name/is_open from JSONB info, drop info. */
    private void e1_reverseGroupPg() throws SQLException {
        try (Connection conn = pgProvider.getConnection()) {
            conn.createStatement().execute("ALTER TABLE \"business\" ADD COLUMN IF NOT EXISTS \"name\" TEXT;");
            conn.createStatement().execute("ALTER TABLE \"business\" ADD COLUMN IF NOT EXISTS \"is_open\" TEXT;");
            conn.createStatement().execute("UPDATE \"business\" SET \"name\" = \"info\"->>'name', \"is_open\" = \"info\"->>'is_open';");
            conn.createStatement().execute("ALTER TABLE \"business\" DROP COLUMN IF EXISTS \"info\";");
        }
    }

    /** Reverse GROUP on MongoDB: flatten info back to top-level fields. */
    private void e1_reverseGroupMongo() {
        final MongoDatabase db = mongoProvider.getDatabase();
        db.getCollection("business").updateMany(
            new Document(),
            List.of(
                new Document("$set", new Document("name", "$info.name")
                    .append("is_open", "$info.is_open")),
                new Document("$unset", List.of("info"))
            )
        );
    }

    private void e1_restorePgCityColumn() throws SQLException {
        try (Connection conn = pgProvider.getConnection()) {
            conn.createStatement().execute("UPDATE \"business\" SET \"city\" = SPLIT_PART(\"city\", '|', 1);");
        }
    }

    private void e1_cleanupMongo(String... fields) {
        ExperimentHelper.resetMongoFields(mongoProvider, "business", fields);
    }

    private void e1_restoreMongoCityField() {
        final MongoDatabase db = mongoProvider.getDatabase();
        db.getCollection("business").updateMany(
            new Document(),
            List.of(new Document("$set", new Document("city",
                new Document("$arrayElemAt", List.of(
                    new Document("$split", List.of("$city", "|")), 0)))))
        );
    }

    // ==================== E2: Optimizer Comparison ====================

    private void runE2_OptimizerComparison() throws IOException {
        LOGGER.info("--- E2: Optimizer Comparison ---");

        // Scenario: Create+Delete pair mixed with real operations
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        // Build operations: 2 real (GROUP + its child SMOs) + Create+Delete pair
        final var tempObjex = EvolutionOperations.createObjex(500, "TempEntity");
        final var tempMorphism = EvolutionOperations.createMorphism(500, 1, 500, "tempMorphism");
        final var deleteMorphism = EvolutionOperations.deleteMorphism(500, 1, 500, "tempMorphism");
        final var deleteTempObjex = EvolutionOperations.deleteObjex(500, "TempEntity");

        final var realOp1 = EvolutionOperations.createObjex(501, "RealEntity");
        final var realOp2 = EvolutionOperations.createMorphism(501, 1, 501, "realMorphism");

        final List<SMO> allOps = List.of(
            (SMO) tempObjex, tempMorphism, realOp1, realOp2, deleteMorphism, deleteTempObjex
        );

        final OperationOptimizer optimizer = new GraphBasedOptimizer();
        final OptimizationComparison comparison = optimizer.compareWithBaseline(allOps);

        LOGGER.info("E2 | {}x | {}", scaleFactor, comparison.summary());

        ExperimentHelper.appendCsvRow("e2_optimizer_comparison.csv",
            String.valueOf(scaleFactor),
            "create_delete_pair",
            String.valueOf(comparison.baseline().originalCount()),
            String.valueOf(comparison.optimized().optimizedCount()),
            String.valueOf(comparison.optimized().cancelledCount()),
            String.valueOf(comparison.optimized().reorderedCount()),
            String.format("%.3f", comparison.reductionRatio()),
            "Create+Delete TempEntity + 2 real ops"
        );

        assertTrue(comparison.operationsSaved() > 0, "Optimizer should eliminate Create+Delete pair");

        LOGGER.info("E2 complete");
    }

    // ==================== E3: Data Integrity ====================

    private void runE3_DataIntegrity() throws Exception {
        LOGGER.info("--- E3: Data Integrity ---");

        // GROUP on PG
        e3_verifyIntegrity("GROUP", "postgresql", () -> {
            try {
                e1_pgGroup();
                final long nulls = ExperimentHelper.countPgNulls(pgProvider, "business", "info");
                e1_reverseGroupPg(); // Must restore name/is_open, not just drop info
                return nulls;
            } catch (SQLException e) { throw new RuntimeException(e); }
        });

        // JOIN on PG
        e3_verifyIntegrity("JOIN", "postgresql", () -> {
            try {
                e1_pgJoin();
                final long nulls = ExperimentHelper.countPgNulls(pgProvider, "business", "location");
                e1_cleanupPg("location");
                return nulls;
            } catch (SQLException e) { throw new RuntimeException(e); }
        });

        // GROUP on MongoDB
        e3_verifyIntegrity("GROUP", "mongodb", () -> {
            e1_mongoGroup();
            final MongoDatabase db = mongoProvider.getDatabase();
            long nulls = 0;
            for (final Document doc : db.getCollection("business").find()) {
                if (doc.get("info") == null) nulls++;
            }
            e1_reverseGroupMongo(); // Must restore name/is_open, not just unset info
            return nulls;
        });

        // JOIN on MongoDB
        e3_verifyIntegrity("JOIN", "mongodb", () -> {
            e1_mongoJoin();
            final MongoDatabase db = mongoProvider.getDatabase();
            long nulls = 0;
            for (final Document doc : db.getCollection("business").find()) {
                if (doc.get("location") == null) nulls++;
            }
            e1_cleanupMongo("location");
            return nulls;
        });

        // Write CSV
        for (final String[] row : e3Results) {
            ExperimentHelper.appendCsvRow("e3_data_integrity.csv", row);
        }

        LOGGER.info("E3 complete: {} checks", e3Results.size());
    }

    @FunctionalInterface
    private interface IntegrityCheck {
        long run(); // returns null count in result column
    }

    private void e3_verifyIntegrity(String smoType, String datasource, IntegrityCheck check) throws Exception {
        final long rowsBefore = datasource.equals("postgresql")
            ? ExperimentHelper.countPgRows(pgProvider, "business")
            : ExperimentHelper.countMongoRows(mongoProvider, "business");

        final long nullCount = check.run();

        final long rowsAfter = datasource.equals("postgresql")
            ? ExperimentHelper.countPgRows(pgProvider, "business")
            : ExperimentHelper.countMongoRows(mongoProvider, "business");

        final boolean passed = rowsBefore == rowsAfter && nullCount == 0;

        e3Results.add(new String[]{
            String.valueOf(scaleFactor), smoType, datasource,
            String.valueOf(rowsBefore), String.valueOf(rowsAfter),
            String.valueOf(nullCount), String.valueOf(passed)
        });

        assertEquals(rowsBefore, rowsAfter,
            "E3 | " + smoType + " on " + datasource + " changed row count!");
        assertEquals(0, nullCount,
            "E3 | " + smoType + " on " + datasource + " produced NULL values!");

        LOGGER.info("E3 | {}x | {} | {} | rows {}->{} | nulls {} | {}",
            scaleFactor, smoType, datasource, rowsBefore, rowsAfter, nullCount, passed ? "PASS" : "FAIL");
    }

    // ==================== E4: JOIN/SPLIT Round-trip Correctness ====================

    private void runE4_JoinSplitCorrectness() throws Exception {
        LOGGER.info("--- E4: JOIN/SPLIT Round-trip ---");

        e4_pgRoundTrip();
        e4_mongoRoundTrip();

        LOGGER.info("E4 complete");
    }

    private void e4_pgRoundTrip() throws Exception {
        // Capture sample values
        final List<String[]> samples = new ArrayList<>();
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT \"city\", \"state\" FROM \"business\" ORDER BY \"business_id\" LIMIT 5")) {
            while (rs.next()) {
                samples.add(new String[]{rs.getString("city"), rs.getString("state")});
            }
        }

        // JOIN city+state -> location
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var joinOp = buildJoinOperation(Schema.business.key(),
            List.of(
                new SourcePropertySpec(Schema.businessCity.key(), Schema.business_city.signature(), "city"),
                new SourcePropertySpec(Schema.businessState.key(), Schema.business_state.signature(), "state")
            ),
            new TargetPropertySpec(new Key(300), new BaseSignature(300), "location"),
            ", ", true, metadata);
        joinOp.up(schema, metadata);

        List<Mapping> pgMappings = newPgMappings();
        final var joinProp = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(), pgMappings, metadata);
        executePgStatements(joinOp.accept(joinProp));

        // MappingEvolver
        final var mappingEvolver = new MappingEvolver(pgMappings, schema, metadata);
        joinOp.accept(mappingEvolver);
        final List<Mapping> evolvedMappings = mappingEvolver.getEvolvedMappings();

        // SPLIT location -> city_r, state_r
        final var splitOp = buildSplitOperation(Schema.business.key(),
            new SourcePropertySpec(new Key(300), new BaseSignature(300), "location"),
            List.of(
                new TargetPropertySpec(new Key(301), new BaseSignature(301), "city_restored"),
                new TargetPropertySpec(new Key(302), new BaseSignature(302), "state_restored")
            ),
            ", ", true, metadata);
        splitOp.up(schema, metadata);

        final var splitProp = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(), evolvedMappings, metadata);
        executePgStatements(splitOp.accept(splitProp));

        // Verify round-trip
        int tested = 0, passed = 0;
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT \"city_restored\", \"state_restored\" FROM \"business\" ORDER BY \"business_id\" LIMIT 5")) {
            for (final String[] sample : samples) {
                assertTrue(rs.next());
                tested++;
                if (sample[0].equals(rs.getString("city_restored"))
                    && sample[1].equals(rs.getString("state_restored"))) {
                    passed++;
                }
            }
        }

        ExperimentHelper.appendCsvRow("e4_join_split_correctness.csv",
            String.valueOf(scaleFactor), "postgresql",
            String.valueOf(tested), String.valueOf(passed), String.valueOf(tested == passed));

        assertEquals(tested, passed, "E4 | PG round-trip failed for some samples");
        LOGGER.info("E4 | {}x | postgresql | {}/{} samples correct", scaleFactor, passed, tested);

        // Cleanup
        try (Connection conn = pgProvider.getConnection()) {
            conn.createStatement().execute("ALTER TABLE \"business\" ADD COLUMN IF NOT EXISTS \"city\" TEXT;");
            conn.createStatement().execute("ALTER TABLE \"business\" ADD COLUMN IF NOT EXISTS \"state\" TEXT;");
            conn.createStatement().execute("UPDATE \"business\" SET \"city\" = \"city_restored\", \"state\" = \"state_restored\";");
            conn.createStatement().execute("ALTER TABLE \"business\" DROP COLUMN IF EXISTS \"city_restored\";");
            conn.createStatement().execute("ALTER TABLE \"business\" DROP COLUMN IF EXISTS \"state_restored\";");
            conn.createStatement().execute("ALTER TABLE \"business\" DROP COLUMN IF EXISTS \"location\";");
        }
    }

    private void e4_mongoRoundTrip() throws Exception {
        final MongoDatabase db = mongoProvider.getDatabase();

        // Capture sample values
        final List<String[]> samples = new ArrayList<>();
        for (final Document doc : db.getCollection("business").find().limit(5)) {
            samples.add(new String[]{doc.getString("city"), doc.getString("state")});
        }

        // JOIN
        final SchemaCategory schema = Schema.newSchema();
        final MetadataCategory metadata = Schema.newMetadata(schema);

        final var joinOp = buildJoinOperation(Schema.business.key(),
            List.of(
                new SourcePropertySpec(Schema.businessCity.key(), Schema.business_city.signature(), "city"),
                new SourcePropertySpec(Schema.businessState.key(), Schema.business_state.signature(), "state")
            ),
            new TargetPropertySpec(new Key(300), new BaseSignature(300), "location"),
            ", ", true, metadata);
        joinOp.up(schema, metadata);

        List<Mapping> mongoMappings = newMongoMappings();
        final var joinProp = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(), mongoMappings, metadata);
        executeMongoStatements(joinOp.accept(joinProp));

        // MappingEvolver
        final var mappingEvolver = new MappingEvolver(mongoMappings, schema, metadata);
        joinOp.accept(mappingEvolver);
        final List<Mapping> evolvedMappings = mappingEvolver.getEvolvedMappings();

        // SPLIT
        final var splitOp = buildSplitOperation(Schema.business.key(),
            new SourcePropertySpec(new Key(300), new BaseSignature(300), "location"),
            List.of(
                new TargetPropertySpec(new Key(301), new BaseSignature(301), "city_restored"),
                new TargetPropertySpec(new Key(302), new BaseSignature(302), "state_restored")
            ),
            ", ", true, metadata);
        splitOp.up(schema, metadata);

        final var splitProp = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(), evolvedMappings, metadata);
        executeMongoStatements(splitOp.accept(splitProp));

        // Verify round-trip
        int tested = 0, passed = 0;
        for (final Document doc : db.getCollection("business").find().limit(5)) {
            final String[] sample = samples.get(tested);
            tested++;
            if (sample[0].equals(doc.getString("city_restored"))
                && sample[1].equals(doc.getString("state_restored"))) {
                passed++;
            }
        }

        ExperimentHelper.appendCsvRow("e4_join_split_correctness.csv",
            String.valueOf(scaleFactor), "mongodb",
            String.valueOf(tested), String.valueOf(passed), String.valueOf(tested == passed));

        assertEquals(tested, passed, "E4 | MongoDB round-trip failed for some samples");
        LOGGER.info("E4 | {}x | mongodb | {}/{} samples correct", scaleFactor, passed, tested);

        // Cleanup
        db.getCollection("business").updateMany(
            new Document(),
            List.of(
                new Document("$set", new Document("city", "$city_restored")
                    .append("state", "$state_restored")),
                new Document("$unset", List.of("city_restored", "state_restored", "location"))
            )
        );
    }

    // ==================== E5: Incremental vs Re-export ====================

    private void runE5_IncrementalVsReExport() throws Exception {
        LOGGER.info("--- E5: Incremental vs Re-export ---");

        // Measure incremental GROUP on PG
        e5_compare("GROUP", "postgresql");
        e1_cleanupPg("info");

        // Measure incremental GROUP on MongoDB
        e5_compare("GROUP", "mongodb");
        e1_cleanupMongo("info");

        // Measure incremental JOIN on PG
        e5_compare("JOIN", "postgresql");
        e1_cleanupPg("location");

        // Measure incremental JOIN on MongoDB
        e5_compare("JOIN", "mongodb");
        e1_cleanupMongo("location");

        LOGGER.info("E5 complete");
    }

    private void e5_compare(String smoType, String datasource) throws Exception {
        // --- Incremental approach: measure directly ---
        final long incrStart = System.nanoTime();
        int incrStmts;
        if (smoType.equals("GROUP")) {
            incrStmts = datasource.equals("postgresql") ? e1_pgGroup()[0] : e1_mongoGroup()[0];
        } else {
            incrStmts = datasource.equals("postgresql") ? e1_pgJoin()[0] : e1_mongoJoin()[0];
        }
        final long incrMs = (System.nanoTime() - incrStart) / 1_000_000;

        // --- Re-export approach: analytical estimate ---
        // Re-export = DROP all tables + CREATE all tables + INSERT all rows
        final long totalRows = ExperimentHelper.countPgRows(pgProvider, "business")
            + ExperimentHelper.countPgRows(pgProvider, "yelp_user")
            + ExperimentHelper.countPgRows(pgProvider, "review");
        final int reExportStmts = (int) (3 * 2 + totalRows); // 3 tables * (DROP + CREATE) + all INSERTs

        // Estimate re-export time by measuring data reload
        final long reExportStart = System.nanoTime();
        ExperimentHelper.reloadYelpData(scaleFactor);
        final long reExportMs = (System.nanoTime() - reExportStart) / 1_000_000;

        final double speedup = reExportMs > 0 ? (double) reExportMs / incrMs : 0;

        ExperimentHelper.appendCsvRow("e5_incremental_vs_reexport.csv",
            String.valueOf(scaleFactor), smoType, datasource,
            String.valueOf(incrStmts), String.valueOf(incrMs),
            String.valueOf(reExportStmts), String.valueOf(reExportMs),
            String.format("%.2f", speedup)
        );

        LOGGER.info("E5 | {}x | {} | {} | incr: {} stmts/{}ms | reexport: {} stmts/{}ms | speedup: {}x",
            scaleFactor, smoType, datasource, incrStmts, incrMs, reExportStmts, reExportMs,
            String.format("%.1f", speedup));
    }

    // ==================== Mapping Helpers ====================

    private static List<Mapping> newPgMappings() {
        return new Datasources().postgreSQL().mappings;
    }

    private static List<Mapping> newMongoMappings() {
        return new Datasources().mongoDB().mappings;
    }

    // ==================== SMO Builder Helpers ====================
    // (Duplicated from YelpPropagationIntegrationTests for independence)

    record MovedMorphismSpec(int oldSig, int newSig, Key propertyKey, String label) {}
    record SourcePropertySpec(Key objexKey, BaseSignature morphismSig, String label) {}
    record TargetPropertySpec(Key objexKey, BaseSignature morphismSig, String label) {}

    private GroupProperties buildGroupOperation(
        Key parentKey, int groupKeyInt, String groupLabel, int parentToGroupSigInt,
        List<MovedMorphismSpec> specs, MetadataCategory meta
    ) {
        final var groupKey = new Key(groupKeyInt);
        final var groupObjex = new SerializedObjex(groupKey, ObjexIds.fromIds(SignatureId.empty()));
        final var groupObjexMeta = new SerializedMetadataObjex(groupKey, groupLabel, null);
        final var parentToGroupSig = new BaseSignature(parentToGroupSigInt);
        final var parentToGroupMorphism = new SerializedMorphism(parentToGroupSig, parentKey, groupKey, Min.ONE, Set.of());
        final var parentToGroupMeta = new SerializedMetadataMorphism(parentToGroupSig, groupLabel);

        meta.setObjex(groupKey, new MetadataObjex(groupLabel, MetadataObjex.Position.createDefault()));

        final var movedMorphisms = specs.stream().map(spec -> {
            final var oldSig = new BaseSignature(spec.oldSig);
            final var newSig = new BaseSignature(spec.newSig);
            meta.setMorphism(newSig, new MetadataMorphism(spec.label));
            return new MovedMorphism(
                new SerializedMorphism(oldSig, parentKey, spec.propertyKey, Min.ZERO, Set.of()),
                new SerializedMetadataMorphism(oldSig, spec.label),
                new SerializedMorphism(newSig, groupKey, spec.propertyKey, Min.ZERO, Set.of()),
                new SerializedMetadataMorphism(newSig, spec.label)
            );
        }).toList();

        return new GroupProperties(parentKey, groupObjex, groupObjexMeta,
            parentToGroupMorphism, parentToGroupMeta, movedMorphisms);
    }

    private UngroupProperty buildUngroupOperation(
        Key parentKey, int groupKeyInt, String groupLabel, int parentToGroupSigInt,
        List<MovedMorphismSpec> specs, MetadataCategory meta
    ) {
        final var groupKey = new Key(groupKeyInt);
        final var groupObjex = new SerializedObjex(groupKey, ObjexIds.fromIds(SignatureId.empty()));
        final var groupObjexMeta = new SerializedMetadataObjex(groupKey, groupLabel, null);
        final var parentToGroupSig = new BaseSignature(parentToGroupSigInt);
        final var parentToGroupMorphism = new SerializedMorphism(parentToGroupSig, parentKey, groupKey, Min.ONE, Set.of());
        final var parentToGroupMeta = new SerializedMetadataMorphism(parentToGroupSig, groupLabel);

        meta.setObjex(groupKey, new MetadataObjex(groupLabel, MetadataObjex.Position.createDefault()));

        final var movedMorphisms = specs.stream().map(spec -> {
            final var oldSig = new BaseSignature(spec.oldSig);
            final var newSig = new BaseSignature(spec.newSig);
            meta.setMorphism(oldSig, new MetadataMorphism(spec.label));
            return new UngroupProperty.MovedMorphism(
                new SerializedMorphism(oldSig, groupKey, spec.propertyKey, Min.ZERO, Set.of()),
                new SerializedMetadataMorphism(oldSig, spec.label),
                new SerializedMorphism(newSig, parentKey, spec.propertyKey, Min.ZERO, Set.of()),
                new SerializedMetadataMorphism(newSig, spec.label)
            );
        }).toList();

        return new UngroupProperty(parentKey, groupObjex, groupObjexMeta,
            parentToGroupMorphism, parentToGroupMeta, movedMorphisms);
    }

    private JoinProperties buildJoinOperation(
        Key parentKey, List<SourcePropertySpec> sourceSpecs,
        TargetPropertySpec targetSpec, String delimiter, boolean deleteSources,
        MetadataCategory meta
    ) {
        final var sourceProperties = sourceSpecs.stream().map(spec ->
            new JoinProperties.SourceProperty(
                new SerializedObjex(spec.objexKey, ObjexIds.fromIds(SignatureId.empty())),
                new SerializedMetadataObjex(spec.objexKey, spec.label, null),
                new SerializedMorphism(spec.morphismSig, parentKey, spec.objexKey, Min.ZERO, Set.of()),
                new SerializedMetadataMorphism(spec.morphismSig, spec.label)
            )
        ).toList();

        final var targetProperty = new JoinProperties.TargetProperty(
            new SerializedObjex(targetSpec.objexKey, ObjexIds.fromIds(SignatureId.empty())),
            new SerializedMetadataObjex(targetSpec.objexKey, targetSpec.label, null),
            new SerializedMorphism(targetSpec.morphismSig, parentKey, targetSpec.objexKey, Min.ZERO, Set.of()),
            new SerializedMetadataMorphism(targetSpec.morphismSig, targetSpec.label)
        );

        meta.setObjex(targetSpec.objexKey, new MetadataObjex(targetSpec.label, MetadataObjex.Position.createDefault()));

        return new JoinProperties(parentKey, sourceProperties, targetProperty, delimiter, deleteSources);
    }

    private SplitProperty buildSplitOperation(
        Key parentKey, SourcePropertySpec sourceSpec,
        List<TargetPropertySpec> targetSpecs, String delimiter, boolean deleteSource,
        MetadataCategory meta
    ) {
        final var sourceProperty = new SplitProperty.SourceProperty(
            new SerializedObjex(sourceSpec.objexKey, ObjexIds.fromIds(SignatureId.empty())),
            new SerializedMetadataObjex(sourceSpec.objexKey, sourceSpec.label, null),
            new SerializedMorphism(sourceSpec.morphismSig, parentKey, sourceSpec.objexKey, Min.ZERO, Set.of()),
            new SerializedMetadataMorphism(sourceSpec.morphismSig, sourceSpec.label)
        );

        final var targetProperties = targetSpecs.stream().map(spec -> {
            meta.setObjex(spec.objexKey, new MetadataObjex(spec.label, MetadataObjex.Position.createDefault()));
            return new SplitProperty.TargetProperty(
                new SerializedObjex(spec.objexKey, ObjexIds.fromIds(SignatureId.empty())),
                new SerializedMetadataObjex(spec.objexKey, spec.label, null),
                new SerializedMorphism(spec.morphismSig, parentKey, spec.objexKey, Min.ZERO, Set.of()),
                new SerializedMetadataMorphism(spec.morphismSig, spec.label)
            );
        }).toList();

        return new SplitProperty(parentKey, sourceProperty, targetProperties, delimiter, deleteSource);
    }

    // ==================== Execution Helpers ====================

    private void executePgStatements(List<AbstractStatement> statements) {
        try (Connection conn = pgProvider.getConnection()) {
            for (final var statement : statements) {
                try (var ps = conn.prepareStatement(statement.getContent())) {
                    ps.execute();
                }
            }
        } catch (SQLException e) {
            fail("PostgreSQL execution failed: " + e.getMessage(), e);
        }
    }

    private void executeMongoStatements(List<AbstractStatement> statements) {
        final MongoDatabase db = mongoProvider.getDatabase();
        for (final var statement : statements) {
            if (statement instanceof cz.matfyz.wrappermongodb.MongoDBCommandStatement commandStatement) {
                db.runCommand(commandStatement.getCommand());
            }
        }
    }
}
