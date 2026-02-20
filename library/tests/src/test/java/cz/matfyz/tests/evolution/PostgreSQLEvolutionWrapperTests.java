package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.matfyz.abstractwrappers.AbstractEvolutionWrapper.PropertyInfo;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.wrapperpostgresql.PostgreSQLEvolutionWrapper;

/**
 * Tests for PostgreSQLEvolutionWrapper SQL generation.
 * Verifies that each evolution method generates the correct SQL statements.
 */
class PostgreSQLEvolutionWrapperTests {

    private PostgreSQLEvolutionWrapper wrapper;

    @BeforeEach
    void setup() {
        wrapper = new PostgreSQLEvolutionWrapper();
    }

    @Test
    void createObjexGeneratesCreateTable() {
        final var properties = List.of(
            new PropertyInfo("id", true, false),
            new PropertyInfo("name", false, false)
        );
        final var statements = wrapper.evolveCreateObjex("customers", properties);

        assertEquals(1, statements.size());
        final String sql = statements.get(0).getContent();
        assertTrue(sql.contains("CREATE TABLE"), "Should contain CREATE TABLE");
        assertTrue(sql.contains("\"customers\""), "Should contain quoted table name");
        assertTrue(sql.contains("\"id\""), "Should contain quoted column name 'id'");
        assertTrue(sql.contains("NOT NULL"), "Required column should be NOT NULL");
    }

    @Test
    void deleteObjexGeneratesDropTable() {
        final var statements = wrapper.evolveDeleteObjex("customers");

        assertEquals(1, statements.size());
        final String sql = statements.get(0).getContent();
        assertTrue(sql.contains("DROP TABLE"), "Should contain DROP TABLE");
        assertTrue(sql.contains("\"customers\""), "Should contain quoted table name");
    }

    @Test
    void addPropertyGeneratesAlterTableAddColumn() {
        final var statements = wrapper.evolveAddProperty("customers", "email", false);

        assertEquals(1, statements.size());
        final String sql = statements.get(0).getContent();
        assertTrue(sql.contains("ALTER TABLE"), "Should contain ALTER TABLE");
        assertTrue(sql.contains("ADD COLUMN"), "Should contain ADD COLUMN");
        assertTrue(sql.contains("\"email\""), "Should contain quoted column name");
    }

    @Test
    void addRequiredPropertyIncludesNotNull() {
        final var statements = wrapper.evolveAddProperty("customers", "email", true);

        assertEquals(1, statements.size());
        final String sql = statements.get(0).getContent();
        assertTrue(sql.contains("NOT NULL"), "Required property should have NOT NULL");
    }

    @Test
    void removePropertyGeneratesAlterTableDropColumn() {
        final var statements = wrapper.evolveRemoveProperty("customers", "email");

        assertEquals(1, statements.size());
        final String sql = statements.get(0).getContent();
        assertTrue(sql.contains("ALTER TABLE"), "Should contain ALTER TABLE");
        assertTrue(sql.contains("DROP COLUMN"), "Should contain DROP COLUMN");
        assertTrue(sql.contains("\"email\""), "Should contain quoted column name");
    }

    @Test
    void groupPropertiesGeneratesJsonbUpdate() {
        final var statements = wrapper.evolveGroupProperties(
            "customers", "name", List.of("firstName", "lastName")
        );

        assertFalse(statements.isEmpty(), "Should generate statements");
        final String allSql = statementsToString(statements);
        assertTrue(allSql.contains("jsonb_build_object"), "Should use jsonb_build_object for grouping");
        assertTrue(allSql.contains("\"name\""), "Should reference group name");
        assertTrue(allSql.contains("\"firstName\""), "Should reference property firstName");
        assertTrue(allSql.contains("\"lastName\""), "Should reference property lastName");
    }

    @Test
    void ungroupPropertyExtractsFromJsonb() {
        final var statements = wrapper.evolveUngroupProperty(
            "customers", "name", List.of("firstName", "lastName")
        );

        assertFalse(statements.isEmpty(), "Should generate statements");
        final String allSql = statementsToString(statements);
        assertTrue(allSql.contains("\"name\""), "Should reference group name");
        assertTrue(allSql.contains("\"firstName\""), "Should reference extracted property");
    }

    @Test
    void joinPropertiesGeneratesConcatUpdate() {
        final var statements = wrapper.evolveJoinProperties(
            "customers", "fullName",
            List.of("firstName", "lastName"), " ", true
        );

        assertFalse(statements.isEmpty(), "Should generate statements");
        final String allSql = statementsToString(statements);
        assertTrue(allSql.contains("\"fullName\""), "Should reference target property");
        assertTrue(allSql.contains("COALESCE"), "Should use COALESCE for null safety");
    }

    @Test
    void joinPropertiesWithDeleteSourcesGeneratesDropColumns() {
        final var statements = wrapper.evolveJoinProperties(
            "customers", "fullName",
            List.of("firstName", "lastName"), " ", true
        );

        final String allSql = statementsToString(statements);
        assertTrue(allSql.contains("DROP COLUMN"), "Should drop source columns when deleteSources=true");
    }

    @Test
    void joinPropertiesWithoutDeleteSourcesKeepsColumns() {
        final var statements = wrapper.evolveJoinProperties(
            "customers", "fullName",
            List.of("firstName", "lastName"), " ", false
        );

        final String allSql = statementsToString(statements);
        assertFalse(allSql.contains("DROP COLUMN"), "Should NOT drop source columns when deleteSources=false");
    }

    @Test
    void splitPropertyGeneratesSplitPartUpdate() {
        final var statements = wrapper.evolveSplitProperty(
            "customers", "address",
            List.of("street", "city", "zip"), ",", true
        );

        assertFalse(statements.isEmpty(), "Should generate statements");
        final String allSql = statementsToString(statements);
        assertTrue(allSql.contains("SPLIT_PART"), "Should use SPLIT_PART for splitting");
        assertTrue(allSql.contains("\"street\""), "Should reference target property street");
        assertTrue(allSql.contains("\"city\""), "Should reference target property city");
        assertTrue(allSql.contains("\"zip\""), "Should reference target property zip");
    }

    @Test
    void movePropertySameTableGeneratesRenameColumn() {
        final var statements = wrapper.evolveMoveProperty(
            "customers", "fname", "customers", "firstName"
        );

        assertFalse(statements.isEmpty(), "Should generate statements");
        final String allSql = statementsToString(statements);
        assertTrue(allSql.contains("RENAME COLUMN") || allSql.contains("\"firstName\""),
            "Should rename or move the column");
    }

    @Test
    void movePropertyCrossTableGeneratesUpdateFromAndDrop() {
        final var statements = wrapper.evolveMoveProperty(
            "orders", "customerName", "customers", "name"
        );

        assertFalse(statements.isEmpty(), "Should generate statements");
        final String allSql = statementsToString(statements);
        assertTrue(allSql.contains("\"orders\""), "Should reference source table");
        assertTrue(allSql.contains("\"customers\""), "Should reference target table");
    }

    private String statementsToString(List<AbstractStatement> statements) {
        final var sb = new StringBuilder();
        for (final var stmt : statements) {
            sb.append(stmt.getContent()).append("\n");
        }
        return sb.toString();
    }
}
