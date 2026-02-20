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
import cz.matfyz.evolution.category.GroupProperties;
import cz.matfyz.evolution.category.GroupProperties.MovedMorphism;
import cz.matfyz.evolution.category.JoinProperties;
import cz.matfyz.evolution.category.SplitProperty;
import cz.matfyz.evolution.category.UngroupProperty;
import cz.matfyz.evolution.propagation.EvolutionPropagator;
import cz.matfyz.evolution.propagation.MappingEvolver;
import cz.matfyz.tests.example.benchmark.yelp.Datasources;
import cz.matfyz.tests.example.benchmark.yelp.Schema;
import cz.matfyz.tests.example.common.DatasourceProvider;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrapperpostgresql.PostgreSQLProvider;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for the full EvolutionPropagator pipeline.
 * Unlike YelpEvolutionIntegrationTests (which tests wrapper methods directly),
 * these tests construct real SMO operations, evolve the schema, and use
 * EvolutionPropagator to translate SMOs to database commands via mapping resolution.
 *
 * This validates the complete chain:
 *   SMO → schema.up() → EvolutionPropagator.visit() → wrapper.evolve*() → DB execution → verification
 *
 * Prerequisites:
 * - Docker containers running (docker compose up -d)
 * - Yelp data loaded (cd experiments/benchmarkDataSetup && npx tsx yelp.ts)
 *
 * Run: mvn test -pl tests -Dtest="cz.matfyz.tests.evolution.YelpPropagationIntegrationTests"
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class YelpPropagationIntegrationTests {

    private static final Logger LOGGER = LoggerFactory.getLogger(YelpPropagationIntegrationTests.class);

    private static final DatasourceProvider datasourceProvider = new DatasourceProvider("tests", "benchmark_yelp");
    private static final PostgreSQLProvider pgProvider = datasourceProvider.getPostgreSQLProvider();
    private static final MongoDBProvider mongoProvider = datasourceProvider.getMongoDBProvider();

    /*
     * Yelp Schema key/signature assignments (from SchemaBuilder auto-increment):
     *
     * Keys:
     *   business=1, businessId=2, businessName=3, businessCity=4,
     *   businessState=5, businessIsOpen=6,
     *   user=7, userId=8, userName=9, userYelpingSince=10,
     *   friendship=11, review=12, reviewId=13, reviewStars=14,
     *   reviewDate=15, reviewUseful=16, reviewFunny=17, reviewCool=18
     *
     * Signatures:
     *   business_id=1, business_name=2, business_city=3, business_state=4,
     *   business_isOpen=5,
     *   user_id=6, user_name=7, user_yelpingSince=8,
     *   friendship_user1=9, friendship_user2=10,
     *   review_id=11, review_user=12, review_business=13,
     *   review_stars=14, review_date=15, review_useful=16,
     *   review_funny=17, review_cool=18
     *
     * New objects/morphisms use keys 100+ and signatures 100+ to avoid conflicts.
     * Each test creates its own fresh schema/metadata to avoid shared state issues.
     */

    /** Creates fresh mappings from the Yelp test infrastructure. */
    private static List<Mapping> newPgMappings() {
        return new Datasources().postgreSQL().mappings;
    }

    private static List<Mapping> newMongoMappings() {
        return new Datasources().mongoDB().mappings;
    }

    // ==================== PostgreSQL: GROUP via Propagator ====================

    @Test
    @Order(1)
    void pg_propagator_groupProperties() throws SQLException {
        LOGGER.info("=== PG Propagator: GROUP business_name + business_isOpen -> business_info ===");

        final SchemaCategory pgSchema = Schema.newSchema();
        final MetadataCategory pgMetadata = Schema.newMetadata(pgSchema);

        // Capture original values
        String originalName;
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"name\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            originalName = rs.getString("name");
        }

        final var groupOp = buildGroupOperation(
            Schema.business.key(), 100, "info", 100,
            List.of(
                new MovedMorphismSpec(2, 101, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(5, 102, Schema.businessIsOpen.key(), "is_open")
            ),
            pgMetadata
        );

        groupOp.up(pgSchema, pgMetadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(),
            newPgMappings(),
            pgMetadata
        );
        final List<AbstractStatement> statements = groupOp.accept(propagator);

        LOGGER.info("Generated {} SQL statements for GROUP", statements.size());
        assertFalse(statements.isEmpty(), "Propagator should generate GROUP statements");

        executePgStatements(statements);

        // Verify: JSONB column "info" exists with nested data
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"info\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            final String infoJson = rs.getString("info");
            assertNotNull(infoJson, "JSONB column 'info' should exist");
            assertTrue(infoJson.contains(originalName), "JSONB should contain original name");
        }

        // Verify: original columns are dropped
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM \"business\" LIMIT 1")) {
            assertFalse(hasColumn(rs, "name"), "Column 'name' should be grouped");
            assertFalse(hasColumn(rs, "is_open"), "Column 'is_open' should be grouped");
            assertTrue(hasColumn(rs, "info"), "Column 'info' should exist");
        }

        LOGGER.info("PG Propagator GROUP: OK");
    }

    @Test
    @Order(2)
    void pg_propagator_ungroupProperty() throws SQLException {
        LOGGER.info("=== PG Propagator: UNGROUP business_info -> name, is_open ===");

        final SchemaCategory pgSchema = Schema.newSchema();
        final MetadataCategory pgMetadata = Schema.newMetadata(pgSchema);

        // First apply GROUP to get schema in the right state (same pattern as MongoDB tests)
        final var groupOp = buildGroupOperation(
            Schema.business.key(), 100, "info", 100,
            List.of(
                new MovedMorphismSpec(2, 101, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(5, 102, Schema.businessIsOpen.key(), "is_open")
            ),
            pgMetadata
        );
        groupOp.up(pgSchema, pgMetadata);

        // Now UNGROUP
        final var ungroupOp = buildUngroupOperation(
            Schema.business.key(), 100, "info", 100,
            List.of(
                new MovedMorphismSpec(101, 2, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(102, 5, Schema.businessIsOpen.key(), "is_open")
            ),
            pgMetadata
        );
        ungroupOp.up(pgSchema, pgMetadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(),
            newPgMappings(),
            pgMetadata
        );
        final List<AbstractStatement> statements = ungroupOp.accept(propagator);

        LOGGER.info("Generated {} SQL statements for UNGROUP", statements.size());
        assertFalse(statements.isEmpty(), "Propagator should generate UNGROUP statements");

        executePgStatements(statements);

        // Verify: name and is_open restored
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"name\", \"is_open\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            assertNotNull(rs.getString("name"), "name should be restored");
            assertNotNull(rs.getString("is_open"), "is_open should be restored");
        }

        LOGGER.info("PG Propagator UNGROUP: OK");
    }

    @Test
    @Order(3)
    void pg_propagator_joinProperties() throws SQLException {
        LOGGER.info("=== PG Propagator: JOIN city + state -> location ===");

        final SchemaCategory pgSchema = Schema.newSchema();
        final MetadataCategory pgMetadata = Schema.newMetadata(pgSchema);

        String originalCity, originalState;
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"city\", \"state\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            originalCity = rs.getString("city");
            originalState = rs.getString("state");
        }

        final var joinOp = buildJoinOperation(
            Schema.business.key(),
            List.of(
                new SourcePropertySpec(Schema.businessCity.key(), Schema.business_city.signature(), "city"),
                new SourcePropertySpec(Schema.businessState.key(), Schema.business_state.signature(), "state")
            ),
            new TargetPropertySpec(new Key(200), new BaseSignature(200), "location"),
            ", ",
            false,
            pgMetadata
        );

        joinOp.up(pgSchema, pgMetadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(),
            newPgMappings(),
            pgMetadata
        );
        final List<AbstractStatement> statements = joinOp.accept(propagator);

        LOGGER.info("Generated {} SQL statements for JOIN", statements.size());
        assertFalse(statements.isEmpty(), "Propagator should generate JOIN statements");

        executePgStatements(statements);

        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"location\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            assertEquals(originalCity + ", " + originalState, rs.getString("location"));
        }

        LOGGER.info("PG Propagator JOIN: OK — location = '{}, {}'", originalCity, originalState);
    }

    @Test
    @Order(4)
    void pg_propagator_splitProperty() throws SQLException {
        LOGGER.info("=== PG Propagator: SPLIT city -> city_part, state_part ===");

        final SchemaCategory pgSchema = Schema.newSchema();
        final MetadataCategory pgMetadata = Schema.newMetadata(pgSchema);

        // Prepare: add delimiter to city for a meaningful split
        try (Connection conn = pgProvider.getConnection()) {
            conn.createStatement().execute(
                "UPDATE \"business\" SET \"city\" = \"city\" || '|' || \"state\";"
            );
        }

        final var splitOp = buildSplitOperation(
            Schema.business.key(),
            new SourcePropertySpec(Schema.businessCity.key(), Schema.business_city.signature(), "city"),
            List.of(
                new TargetPropertySpec(new Key(201), new BaseSignature(201), "city_part"),
                new TargetPropertySpec(new Key(202), new BaseSignature(202), "state_part")
            ),
            "|",
            false,
            pgMetadata
        );

        splitOp.up(pgSchema, pgMetadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(),
            newPgMappings(),
            pgMetadata
        );
        final List<AbstractStatement> statements = splitOp.accept(propagator);

        LOGGER.info("Generated {} SQL statements for SPLIT", statements.size());
        assertFalse(statements.isEmpty(), "Propagator should generate SPLIT statements");

        executePgStatements(statements);

        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT \"state\", \"city_part\", \"state_part\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            assertNotNull(rs.getString("city_part"), "city_part should not be null");
            assertEquals(rs.getString("state"), rs.getString("state_part"),
                "state_part should match original state");
        }

        LOGGER.info("PG Propagator SPLIT: OK");
    }

    @Test
    @Order(5)
    void pg_cleanup() throws SQLException {
        LOGGER.info("=== PG Cleanup ===");
        try (Connection conn = pgProvider.getConnection()) {
            conn.createStatement().execute("ALTER TABLE \"business\" DROP COLUMN IF EXISTS \"city_part\";");
            conn.createStatement().execute("ALTER TABLE \"business\" DROP COLUMN IF EXISTS \"state_part\";");
            conn.createStatement().execute("ALTER TABLE \"business\" DROP COLUMN IF EXISTS \"location\";");
            conn.createStatement().execute("UPDATE \"business\" SET \"city\" = SPLIT_PART(\"city\", '|', 1);");
        }
        LOGGER.info("PG Cleanup: OK");
    }

    @Test
    @Order(6)
    void pg_verifyDataIntegrity() throws SQLException {
        LOGGER.info("=== PG Final: Verify 10 businesses with all original columns ===");

        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT COUNT(*) AS cnt FROM \"business\"")) {
            assertTrue(rs.next());
            assertEquals(10, rs.getInt("cnt"));
        }

        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT \"business_id\", \"name\", \"city\", \"state\", \"is_open\" FROM \"business\"")) {
            int count = 0;
            while (rs.next()) {
                assertNotNull(rs.getString("business_id"));
                assertNotNull(rs.getString("name"));
                assertNotNull(rs.getString("city"));
                assertNotNull(rs.getString("state"));
                count++;
            }
            assertEquals(10, count);
        }

        LOGGER.info("PG DataIntegrity: OK");
    }

    // ==================== MongoDB: GROUP via Propagator ====================

    @Test
    @Order(10)
    void mongo_propagator_groupProperties() {
        LOGGER.info("=== Mongo Propagator: GROUP name + is_open -> info ===");

        final MongoDatabase db = mongoProvider.getDatabase();
        final Document originalDoc = db.getCollection("business").find().first();
        assertNotNull(originalDoc);
        final String originalName = originalDoc.getString("name");

        final SchemaCategory mongoSchema = Schema.newSchema();
        final MetadataCategory mongoMetadata = Schema.newMetadata(mongoSchema);

        final var groupOp = buildGroupOperation(
            Schema.business.key(), 100, "info", 100,
            List.of(
                new MovedMorphismSpec(2, 101, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(5, 102, Schema.businessIsOpen.key(), "is_open")
            ),
            mongoMetadata
        );

        groupOp.up(mongoSchema, mongoMetadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(),
            newMongoMappings(),
            mongoMetadata
        );
        final List<AbstractStatement> statements = groupOp.accept(propagator);

        LOGGER.info("Generated {} MongoDB statements for GROUP", statements.size());
        assertFalse(statements.isEmpty(), "Propagator should generate GROUP statements");

        executeMongoStatements(statements);

        final Document updatedDoc = db.getCollection("business").find().first();
        assertNotNull(updatedDoc);
        final Document info = updatedDoc.get("info", Document.class);
        assertNotNull(info, "Nested 'info' should exist");
        assertEquals(originalName, info.getString("name"), "Nested name should match");
        assertFalse(updatedDoc.containsKey("name"), "Top-level name should be removed");

        LOGGER.info("Mongo Propagator GROUP: OK");
    }

    @Test
    @Order(11)
    void mongo_propagator_ungroupProperty() {
        LOGGER.info("=== Mongo Propagator: UNGROUP info -> name, is_open ===");

        final MongoDatabase db = mongoProvider.getDatabase();

        final SchemaCategory mongoSchema = Schema.newSchema();
        final MetadataCategory mongoMetadata = Schema.newMetadata(mongoSchema);

        // First apply GROUP to get schema in the right state
        final var groupOp = buildGroupOperation(
            Schema.business.key(), 100, "info", 100,
            List.of(
                new MovedMorphismSpec(2, 101, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(5, 102, Schema.businessIsOpen.key(), "is_open")
            ),
            mongoMetadata
        );
        groupOp.up(mongoSchema, mongoMetadata);

        // Now UNGROUP
        final var ungroupOp = buildUngroupOperation(
            Schema.business.key(), 100, "info", 100,
            List.of(
                new MovedMorphismSpec(101, 2, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(102, 5, Schema.businessIsOpen.key(), "is_open")
            ),
            mongoMetadata
        );
        ungroupOp.up(mongoSchema, mongoMetadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(),
            newMongoMappings(),
            mongoMetadata
        );
        final List<AbstractStatement> statements = ungroupOp.accept(propagator);

        LOGGER.info("Generated {} MongoDB statements for UNGROUP", statements.size());
        assertFalse(statements.isEmpty(), "Propagator should generate UNGROUP statements");
        executeMongoStatements(statements);

        final Document doc = db.getCollection("business").find().first();
        assertNotNull(doc);
        assertNotNull(doc.getString("name"), "name should be restored");
        assertNotNull(doc.get("is_open"), "is_open should be restored");
        assertFalse(doc.containsKey("info"), "info should be removed");

        LOGGER.info("Mongo Propagator UNGROUP: OK");
    }

    @Test
    @Order(12)
    void mongo_propagator_joinProperties() {
        LOGGER.info("=== Mongo Propagator: JOIN city + state -> location ===");

        final MongoDatabase db = mongoProvider.getDatabase();
        final Document originalDoc = db.getCollection("business").find().first();
        assertNotNull(originalDoc);
        final String originalCity = originalDoc.getString("city");
        final String originalState = originalDoc.getString("state");

        final SchemaCategory mongoSchema = Schema.newSchema();
        final MetadataCategory mongoMetadata = Schema.newMetadata(mongoSchema);

        final var joinOp = buildJoinOperation(
            Schema.business.key(),
            List.of(
                new SourcePropertySpec(Schema.businessCity.key(), Schema.business_city.signature(), "city"),
                new SourcePropertySpec(Schema.businessState.key(), Schema.business_state.signature(), "state")
            ),
            new TargetPropertySpec(new Key(200), new BaseSignature(200), "location"),
            ", ",
            false,
            mongoMetadata
        );

        joinOp.up(mongoSchema, mongoMetadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(),
            newMongoMappings(),
            mongoMetadata
        );
        final List<AbstractStatement> statements = joinOp.accept(propagator);

        LOGGER.info("Generated {} MongoDB statements for JOIN", statements.size());
        executeMongoStatements(statements);

        final Document updatedDoc = db.getCollection("business").find().first();
        assertNotNull(updatedDoc);
        assertEquals(originalCity + ", " + originalState, updatedDoc.getString("location"));

        LOGGER.info("Mongo Propagator JOIN: OK");
    }

    @Test
    @Order(13)
    void mongo_propagator_splitProperty() {
        LOGGER.info("=== Mongo Propagator: SPLIT city -> city_part, state_part ===");

        final MongoDatabase db = mongoProvider.getDatabase();

        db.getCollection("business").updateMany(
            new Document(),
            List.of(new Document("$set", new Document("city",
                new Document("$concat", List.of("$city", "|", "$state")))))
        );

        final SchemaCategory mongoSchema = Schema.newSchema();
        final MetadataCategory mongoMetadata = Schema.newMetadata(mongoSchema);

        final var splitOp = buildSplitOperation(
            Schema.business.key(),
            new SourcePropertySpec(Schema.businessCity.key(), Schema.business_city.signature(), "city"),
            List.of(
                new TargetPropertySpec(new Key(201), new BaseSignature(201), "city_part"),
                new TargetPropertySpec(new Key(202), new BaseSignature(202), "state_part")
            ),
            "|", false,
            mongoMetadata
        );

        splitOp.up(mongoSchema, mongoMetadata);

        final var propagator = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(),
            newMongoMappings(),
            mongoMetadata
        );
        final List<AbstractStatement> statements = splitOp.accept(propagator);

        LOGGER.info("Generated {} MongoDB statements for SPLIT", statements.size());
        assertFalse(statements.isEmpty(), "Propagator should generate SPLIT statements");
        executeMongoStatements(statements);

        final Document doc = db.getCollection("business").find().first();
        assertNotNull(doc);
        assertNotNull(doc.getString("city_part"), "city_part should exist");
        assertEquals(doc.getString("state"), doc.getString("state_part"), "state_part should match state");

        LOGGER.info("Mongo Propagator SPLIT: OK");
    }

    @Test
    @Order(14)
    void mongo_cleanup() {
        LOGGER.info("=== Mongo Cleanup ===");
        final MongoDatabase db = mongoProvider.getDatabase();
        db.getCollection("business").updateMany(
            new Document(),
            List.of(
                new Document("$set", new Document("city", "$city_part")),
                new Document("$unset", List.of("city_part", "state_part", "location"))
            )
        );
        LOGGER.info("Mongo Cleanup: OK");
    }

    @Test
    @Order(15)
    void mongo_verifyDataIntegrity() {
        LOGGER.info("=== Mongo Final: Verify 10 businesses intact ===");

        final MongoDatabase db = mongoProvider.getDatabase();
        assertEquals(10, db.getCollection("business").countDocuments());

        for (final Document doc : db.getCollection("business").find()) {
            assertNotNull(doc.getString("business_id"));
            assertNotNull(doc.getString("name"));
            assertNotNull(doc.getString("city"));
            assertNotNull(doc.getString("state"));
        }

        LOGGER.info("Mongo DataIntegrity: OK");
    }

    // ==================== PostgreSQL: Chained JOIN → MappingEvolver → SPLIT ====================

    @Test
    @Order(20)
    void pg_chained_joinThenSplit() throws SQLException {
        LOGGER.info("=== PG Chained: JOIN city+state → location, MappingEvolver, SPLIT location → city_r, state_r ===");

        // Fresh schema for this test chain
        final SchemaCategory chainSchema = Schema.newSchema();
        final MetadataCategory chainMetadata = Schema.newMetadata(chainSchema);

        // Capture originals
        String originalCity, originalState;
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"city\", \"state\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            originalCity = rs.getString("city");
            originalState = rs.getString("state");
        }

        // --- Step 1: JOIN city + state → location (delete sources) ---

        final var joinOp = buildJoinOperation(
            Schema.business.key(),
            List.of(
                new SourcePropertySpec(Schema.businessCity.key(), Schema.business_city.signature(), "city"),
                new SourcePropertySpec(Schema.businessState.key(), Schema.business_state.signature(), "state")
            ),
            new TargetPropertySpec(new Key(300), new BaseSignature(300), "location"),
            ", ",
            true, // delete source properties
            chainMetadata
        );

        joinOp.up(chainSchema, chainMetadata);

        // Use fresh copies of PG mappings for this chain
        List<Mapping> chainPgMappings = newPgMappings();

        final var joinPropagator = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(),
            chainPgMappings,
            chainMetadata
        );
        final List<AbstractStatement> joinStatements = joinOp.accept(joinPropagator);

        LOGGER.info("JOIN generated {} SQL statements", joinStatements.size());
        assertFalse(joinStatements.isEmpty(), "JOIN should generate statements");
        executePgStatements(joinStatements);

        // Verify JOIN result
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"location\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            assertEquals(originalCity + ", " + originalState, rs.getString("location"));
        }

        // --- Step 2: MappingEvolver updates mappings ---

        final var mappingEvolver = new MappingEvolver(chainPgMappings, chainSchema, chainMetadata);
        joinOp.accept(mappingEvolver);
        final List<Mapping> evolvedMappings = mappingEvolver.getEvolvedMappings();

        LOGGER.info("MappingEvolver updated {} mappings", evolvedMappings.size());

        // Verify the evolved mapping contains "location" and no longer has "city"/"state"
        final Mapping businessMapping = evolvedMappings.stream()
            .filter(m -> m.rootObjex().key().equals(Schema.business.key()))
            .findFirst().orElseThrow();
        LOGGER.info("Evolved business mapping access path: {}", businessMapping.accessPath());

        // --- Step 3: SPLIT location → city_restored, state_restored (using evolved mappings) ---

        final var splitOp = buildSplitOperation(
            Schema.business.key(),
            new SourcePropertySpec(new Key(300), new BaseSignature(300), "location"),
            List.of(
                new TargetPropertySpec(new Key(301), new BaseSignature(301), "city_restored"),
                new TargetPropertySpec(new Key(302), new BaseSignature(302), "state_restored")
            ),
            ", ",
            true, // delete source (location)
            chainMetadata
        );

        splitOp.up(chainSchema, chainMetadata);

        // Use evolved mappings for SPLIT propagation — this is the key test!
        final var splitPropagator = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(),
            evolvedMappings,
            chainMetadata
        );
        final List<AbstractStatement> splitStatements = splitOp.accept(splitPropagator);

        LOGGER.info("SPLIT generated {} SQL statements (using evolved mappings)", splitStatements.size());
        assertFalse(splitStatements.isEmpty(),
            "SPLIT should generate statements when using MappingEvolver-updated mappings");
        executePgStatements(splitStatements);

        // Verify SPLIT result
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT \"city_restored\", \"state_restored\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            assertEquals(originalCity, rs.getString("city_restored"),
                "city_restored should match original city");
            assertEquals(originalState, rs.getString("state_restored"),
                "state_restored should match original state");
        }

        LOGGER.info("PG Chained JOIN→MappingEvolver→SPLIT: OK — city='{}', state='{}'", originalCity, originalState);
    }

    @Test
    @Order(21)
    void pg_chained_cleanup() throws SQLException {
        LOGGER.info("=== PG Chained Cleanup ===");
        // Restore original columns and drop temporary ones
        try (Connection conn = pgProvider.getConnection()) {
            // Restore city and state from the split results
            conn.createStatement().execute(
                "ALTER TABLE \"business\" ADD COLUMN IF NOT EXISTS \"city\" TEXT;"
            );
            conn.createStatement().execute(
                "ALTER TABLE \"business\" ADD COLUMN IF NOT EXISTS \"state\" TEXT;"
            );
            conn.createStatement().execute(
                "UPDATE \"business\" SET \"city\" = \"city_restored\", \"state\" = \"state_restored\";"
            );
            // Drop temporary columns
            conn.createStatement().execute(
                "ALTER TABLE \"business\" DROP COLUMN IF EXISTS \"city_restored\";"
            );
            conn.createStatement().execute(
                "ALTER TABLE \"business\" DROP COLUMN IF EXISTS \"state_restored\";"
            );
            conn.createStatement().execute(
                "ALTER TABLE \"business\" DROP COLUMN IF EXISTS \"location\";"
            );
        }

        // Verify restoration
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT \"city\", \"state\" FROM \"business\" WHERE \"city\" IS NOT NULL LIMIT 1")) {
            assertTrue(rs.next(), "Should have rows with restored city/state");
            assertNotNull(rs.getString("city"));
            assertNotNull(rs.getString("state"));
        }

        LOGGER.info("PG Chained Cleanup: OK");
    }

    // ==================== MongoDB: Chained JOIN → MappingEvolver → SPLIT ====================

    @Test
    @Order(30)
    void mongo_chained_joinThenSplit() {
        LOGGER.info("=== Mongo Chained: JOIN city+state → location, MappingEvolver, SPLIT location → city_r, state_r ===");

        final MongoDatabase db = mongoProvider.getDatabase();
        final SchemaCategory chainSchema = Schema.newSchema();
        final MetadataCategory chainMetadata = Schema.newMetadata(chainSchema);

        // Capture originals
        final Document originalDoc = db.getCollection("business").find().first();
        assertNotNull(originalDoc);
        final String originalCity = originalDoc.getString("city");
        final String originalState = originalDoc.getString("state");

        // --- Step 1: JOIN city + state → location (delete sources) ---

        final var joinOp = buildJoinOperation(
            Schema.business.key(),
            List.of(
                new SourcePropertySpec(Schema.businessCity.key(), Schema.business_city.signature(), "city"),
                new SourcePropertySpec(Schema.businessState.key(), Schema.business_state.signature(), "state")
            ),
            new TargetPropertySpec(new Key(300), new BaseSignature(300), "location"),
            ", ",
            true, // delete source properties
            chainMetadata
        );

        joinOp.up(chainSchema, chainMetadata);

        List<Mapping> chainMongoMappings = newMongoMappings();

        final var joinPropagator = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(),
            chainMongoMappings,
            chainMetadata
        );
        final List<AbstractStatement> joinStatements = joinOp.accept(joinPropagator);

        LOGGER.info("JOIN generated {} MongoDB statements", joinStatements.size());
        assertFalse(joinStatements.isEmpty(), "JOIN should generate statements");
        executeMongoStatements(joinStatements);

        // Verify JOIN result
        final Document afterJoin = db.getCollection("business").find().first();
        assertNotNull(afterJoin);
        assertEquals(originalCity + ", " + originalState, afterJoin.getString("location"));
        assertFalse(afterJoin.containsKey("city"), "city should be deleted after JOIN");
        assertFalse(afterJoin.containsKey("state"), "state should be deleted after JOIN");

        // --- Step 2: MappingEvolver updates mappings ---

        final var mappingEvolver = new MappingEvolver(chainMongoMappings, chainSchema, chainMetadata);
        joinOp.accept(mappingEvolver);
        final List<Mapping> evolvedMappings = mappingEvolver.getEvolvedMappings();

        LOGGER.info("MappingEvolver updated {} mappings", evolvedMappings.size());

        // --- Step 3: SPLIT location → city_restored, state_restored ---

        final var splitOp = buildSplitOperation(
            Schema.business.key(),
            new SourcePropertySpec(new Key(300), new BaseSignature(300), "location"),
            List.of(
                new TargetPropertySpec(new Key(301), new BaseSignature(301), "city_restored"),
                new TargetPropertySpec(new Key(302), new BaseSignature(302), "state_restored")
            ),
            ", ",
            true, // delete source (location)
            chainMetadata
        );

        splitOp.up(chainSchema, chainMetadata);

        final var splitPropagator = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(),
            evolvedMappings,
            chainMetadata
        );
        final List<AbstractStatement> splitStatements = splitOp.accept(splitPropagator);

        LOGGER.info("SPLIT generated {} MongoDB statements (using evolved mappings)", splitStatements.size());
        assertFalse(splitStatements.isEmpty(),
            "SPLIT should generate statements when using MappingEvolver-updated mappings");
        executeMongoStatements(splitStatements);

        // Verify SPLIT result
        final Document afterSplit = db.getCollection("business").find().first();
        assertNotNull(afterSplit);
        assertEquals(originalCity, afterSplit.getString("city_restored"),
            "city_restored should match original city");
        assertEquals(originalState, afterSplit.getString("state_restored"),
            "state_restored should match original state");
        assertFalse(afterSplit.containsKey("location"), "location should be deleted after SPLIT");

        LOGGER.info("Mongo Chained JOIN→MappingEvolver→SPLIT: OK — city='{}', state='{}'",
            originalCity, originalState);
    }

    @Test
    @Order(31)
    void mongo_chained_cleanup() {
        LOGGER.info("=== Mongo Chained Cleanup ===");
        final MongoDatabase db = mongoProvider.getDatabase();
        // Restore city/state from split results, remove temp fields
        db.getCollection("business").updateMany(
            new Document(),
            List.of(
                new Document("$set", new Document("city", "$city_restored")
                    .append("state", "$state_restored")),
                new Document("$unset", List.of("city_restored", "state_restored", "location"))
            )
        );

        // Verify restoration
        final Document doc = db.getCollection("business").find().first();
        assertNotNull(doc);
        assertNotNull(doc.getString("city"), "city should be restored");
        assertNotNull(doc.getString("state"), "state should be restored");

        LOGGER.info("Mongo Chained Cleanup: OK");
    }

    // ==================== PostgreSQL: Chained GROUP → MappingEvolver → UNGROUP ====================

    @Test
    @Order(40)
    void pg_chained_groupThenUngroup() throws SQLException {
        LOGGER.info("=== PG Chained: GROUP → MappingEvolver → UNGROUP ===");

        final SchemaCategory chainSchema = Schema.newSchema();
        final MetadataCategory chainMetadata = Schema.newMetadata(chainSchema);

        // Capture originals
        String originalName, originalIsOpen;
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"name\", \"is_open\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            originalName = rs.getString("name");
            originalIsOpen = rs.getString("is_open");
        }

        // --- Step 1: GROUP name + is_open → info ---

        final var groupOp = buildGroupOperation(
            Schema.business.key(), 400, "info", 400,
            List.of(
                new MovedMorphismSpec(2, 401, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(5, 402, Schema.businessIsOpen.key(), "is_open")
            ),
            chainMetadata
        );

        groupOp.up(chainSchema, chainMetadata);

        List<Mapping> chainPgMappings = newPgMappings();

        final var groupPropagator = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(),
            chainPgMappings,
            chainMetadata
        );
        final List<AbstractStatement> groupStatements = groupOp.accept(groupPropagator);

        LOGGER.info("GROUP generated {} SQL statements", groupStatements.size());
        assertFalse(groupStatements.isEmpty());
        executePgStatements(groupStatements);

        // Verify GROUP result
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"info\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            assertNotNull(rs.getString("info"), "JSONB 'info' should exist");
            assertTrue(rs.getString("info").contains(originalName));
        }

        // --- Step 2: MappingEvolver updates mappings ---

        final var mappingEvolver = new MappingEvolver(chainPgMappings, chainSchema, chainMetadata);
        groupOp.accept(mappingEvolver);
        final List<Mapping> evolvedMappings = mappingEvolver.getEvolvedMappings();

        LOGGER.info("MappingEvolver updated mappings after GROUP");

        // --- Step 3: UNGROUP info → name, is_open (using evolved mappings) ---

        final var ungroupOp = buildUngroupOperation(
            Schema.business.key(), 400, "info", 400,
            List.of(
                new MovedMorphismSpec(401, 2, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(402, 5, Schema.businessIsOpen.key(), "is_open")
            ),
            chainMetadata
        );

        ungroupOp.up(chainSchema, chainMetadata);

        final var ungroupPropagator = new EvolutionPropagator(
            new cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper(),
            evolvedMappings,
            chainMetadata
        );
        final List<AbstractStatement> ungroupStatements = ungroupOp.accept(ungroupPropagator);

        LOGGER.info("UNGROUP generated {} SQL statements (using evolved mappings)", ungroupStatements.size());
        assertFalse(ungroupStatements.isEmpty(),
            "UNGROUP should generate statements when using MappingEvolver-updated mappings");
        executePgStatements(ungroupStatements);

        // Verify UNGROUP result — name and is_open restored
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery("SELECT \"name\", \"is_open\" FROM \"business\" LIMIT 1")) {
            assertTrue(rs.next());
            assertEquals(originalName, rs.getString("name"), "name should be restored");
            assertEquals(originalIsOpen, rs.getString("is_open"), "is_open should be restored");
        }

        // Cleanup — drop info column if leftover
        try (Connection conn = pgProvider.getConnection()) {
            conn.createStatement().execute("ALTER TABLE \"business\" DROP COLUMN IF EXISTS \"info\";");
        }

        LOGGER.info("PG Chained GROUP→MappingEvolver→UNGROUP: OK");
    }

    // ==================== MongoDB: Chained GROUP → MappingEvolver → UNGROUP ====================

    @Test
    @Order(50)
    void mongo_chained_groupThenUngroup() {
        LOGGER.info("=== Mongo Chained: GROUP → MappingEvolver → UNGROUP ===");

        final MongoDatabase db = mongoProvider.getDatabase();
        final SchemaCategory chainSchema = Schema.newSchema();
        final MetadataCategory chainMetadata = Schema.newMetadata(chainSchema);

        // Capture originals
        final Document originalDoc = db.getCollection("business").find().first();
        assertNotNull(originalDoc);
        final String originalName = originalDoc.getString("name");

        // --- Step 1: GROUP name + is_open → info ---

        final var groupOp = buildGroupOperation(
            Schema.business.key(), 400, "info", 400,
            List.of(
                new MovedMorphismSpec(2, 401, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(5, 402, Schema.businessIsOpen.key(), "is_open")
            ),
            chainMetadata
        );

        groupOp.up(chainSchema, chainMetadata);

        List<Mapping> chainMongoMappings = newMongoMappings();

        final var groupPropagator = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(),
            chainMongoMappings,
            chainMetadata
        );
        final List<AbstractStatement> groupStatements = groupOp.accept(groupPropagator);

        LOGGER.info("GROUP generated {} MongoDB statements", groupStatements.size());
        assertFalse(groupStatements.isEmpty());
        executeMongoStatements(groupStatements);

        // Verify GROUP result
        final Document afterGroup = db.getCollection("business").find().first();
        assertNotNull(afterGroup);
        final Document info = afterGroup.get("info", Document.class);
        assertNotNull(info, "Nested 'info' should exist");
        assertEquals(originalName, info.getString("name"));

        // --- Step 2: MappingEvolver updates mappings ---

        final var mappingEvolver = new MappingEvolver(chainMongoMappings, chainSchema, chainMetadata);
        groupOp.accept(mappingEvolver);
        final List<Mapping> evolvedMappings = mappingEvolver.getEvolvedMappings();

        LOGGER.info("MappingEvolver updated mappings after GROUP");

        // --- Step 3: UNGROUP info → name, is_open ---

        final var ungroupOp = buildUngroupOperation(
            Schema.business.key(), 400, "info", 400,
            List.of(
                new MovedMorphismSpec(401, 2, Schema.businessName.key(), "name"),
                new MovedMorphismSpec(402, 5, Schema.businessIsOpen.key(), "is_open")
            ),
            chainMetadata
        );

        ungroupOp.up(chainSchema, chainMetadata);

        final var ungroupPropagator = new EvolutionPropagator(
            new cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper(),
            evolvedMappings,
            chainMetadata
        );
        final List<AbstractStatement> ungroupStatements = ungroupOp.accept(ungroupPropagator);

        LOGGER.info("UNGROUP generated {} MongoDB statements (using evolved mappings)", ungroupStatements.size());
        assertFalse(ungroupStatements.isEmpty(),
            "UNGROUP should generate statements when using MappingEvolver-updated mappings");
        executeMongoStatements(ungroupStatements);

        // Verify UNGROUP result
        final Document afterUngroup = db.getCollection("business").find().first();
        assertNotNull(afterUngroup);
        assertEquals(originalName, afterUngroup.getString("name"), "name should be restored");
        assertNotNull(afterUngroup.get("is_open"), "is_open should be restored");
        assertFalse(afterUngroup.containsKey("info"), "info should be removed");

        LOGGER.info("Mongo Chained GROUP→MappingEvolver→UNGROUP: OK");
    }

    // ==================== Final Data Integrity ====================

    @Test
    @Order(99)
    void final_verifyAllDataIntact() throws SQLException {
        LOGGER.info("=== Final: Verify all data intact after chained operations ===");

        // PostgreSQL
        try (Connection conn = pgProvider.getConnection();
             ResultSet rs = conn.createStatement().executeQuery(
                 "SELECT COUNT(*) AS cnt FROM \"business\"")) {
            assertTrue(rs.next());
            assertEquals(10, rs.getInt("cnt"), "PG should still have 10 businesses");
        }

        // MongoDB
        final MongoDatabase db = mongoProvider.getDatabase();
        assertEquals(10, db.getCollection("business").countDocuments(),
            "MongoDB should still have 10 businesses");

        LOGGER.info("Final DataIntegrity: OK — all 10 businesses intact in both databases");
    }

    // ==================== SMO Builder Helpers ====================

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
        final var parentToGroupMorphism = new SerializedMorphism(
            parentToGroupSig, parentKey, groupKey, Min.ONE, Set.of()
        );
        final var parentToGroupMeta = new SerializedMetadataMorphism(parentToGroupSig, groupLabel);

        // Set metadata so EvolutionPropagator can resolve the group label
        meta.setObjex(groupKey, new MetadataObjex(groupLabel, MetadataObjex.Position.createDefault()));

        final var movedMorphisms = specs.stream().map(spec -> {
            final var oldSig = new BaseSignature(spec.oldSig);
            final var newSig = new BaseSignature(spec.newSig);
            // Set metadata for new morphisms so EvolutionPropagator can resolve them after MappingEvolver
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
        final var parentToGroupMorphism = new SerializedMorphism(
            parentToGroupSig, parentKey, groupKey, Min.ONE, Set.of()
        );
        final var parentToGroupMeta = new SerializedMetadataMorphism(parentToGroupSig, groupLabel);

        // Set metadata so EvolutionPropagator can resolve the group label
        meta.setObjex(groupKey, new MetadataObjex(groupLabel, MetadataObjex.Position.createDefault()));

        final var movedMorphisms = specs.stream().map(spec -> {
            final var oldSig = new BaseSignature(spec.oldSig);
            final var newSig = new BaseSignature(spec.newSig);
            // Set metadata for old morphisms so EvolutionPropagator can resolve them
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

        // Set metadata so EvolutionPropagator can resolve the target label
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
            // Set metadata for each target property
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
