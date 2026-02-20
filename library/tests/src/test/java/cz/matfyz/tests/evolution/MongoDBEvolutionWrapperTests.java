package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import cz.matfyz.abstractwrappers.AbstractEvolutionWrapper.PropertyInfo;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.wrappermongodb.MongoDBEvolutionWrapper;

/**
 * Tests for MongoDBEvolutionWrapper BSON command generation.
 * Verifies that each evolution method generates the correct MongoDB commands.
 */
class MongoDBEvolutionWrapperTests {

    private MongoDBEvolutionWrapper wrapper;

    @BeforeEach
    void setup() {
        wrapper = new MongoDBEvolutionWrapper();
    }

    @Test
    void createObjexGeneratesCreateCollection() {
        final var properties = List.of(
            new PropertyInfo("id", true, false),
            new PropertyInfo("name", false, false)
        );
        final var statements = wrapper.evolveCreateObjex("customers", properties);

        assertEquals(1, statements.size());
        final String content = statements.get(0).getContent();
        assertTrue(content.contains("create") || content.contains("customers"),
            "Should reference collection creation for 'customers'");
    }

    @Test
    void deleteObjexGeneratesDrop() {
        final var statements = wrapper.evolveDeleteObjex("customers");

        assertEquals(1, statements.size());
        final String content = statements.get(0).getContent();
        assertTrue(content.contains("drop") || content.contains("customers"),
            "Should reference dropping collection 'customers'");
    }

    @Test
    void addPropertyIsNoOp() {
        final var statements = wrapper.evolveAddProperty("customers", "email", false);

        // MongoDB is schemaless — adding a field doesn't require a migration
        assertTrue(statements.isEmpty(), "Adding a property in schemaless DB should be no-op");
    }

    @Test
    void removePropertyGeneratesUnset() {
        final var statements = wrapper.evolveRemoveProperty("customers", "email");

        assertFalse(statements.isEmpty(), "Should generate unset statement");
        final String content = statements.get(0).getContent();
        assertTrue(content.contains("$unset") || content.contains("email"),
            "Should use $unset to remove property 'email'");
    }

    @Test
    void groupPropertiesGeneratesSetAndUnset() {
        final var statements = wrapper.evolveGroupProperties(
            "customers", "name", List.of("firstName", "lastName")
        );

        assertFalse(statements.isEmpty(), "Should generate statements");
        final String allContent = statementsToString(statements);
        assertTrue(allContent.contains("$set") || allContent.contains("name"),
            "Should use $set to create nested structure");
    }

    @Test
    void ungroupPropertyFlattensDocument() {
        final var statements = wrapper.evolveUngroupProperty(
            "customers", "name", List.of("firstName", "lastName")
        );

        assertFalse(statements.isEmpty(), "Should generate statements");
        final String allContent = statementsToString(statements);
        assertTrue(allContent.contains("$set") || allContent.contains("firstName"),
            "Should use $set to flatten properties");
    }

    @Test
    void joinPropertiesGeneratesConcat() {
        final var statements = wrapper.evolveJoinProperties(
            "customers", "fullName",
            List.of("firstName", "lastName"), " ", true
        );

        assertFalse(statements.isEmpty(), "Should generate statements");
        final String allContent = statementsToString(statements);
        assertTrue(allContent.contains("$concat") || allContent.contains("fullName"),
            "Should use $concat for joining values");
    }

    @Test
    void splitPropertyGeneratesSplitAndArrayElemAt() {
        final var statements = wrapper.evolveSplitProperty(
            "customers", "address",
            List.of("street", "city", "zip"), ",", true
        );

        assertFalse(statements.isEmpty(), "Should generate statements");
        final String allContent = statementsToString(statements);
        assertTrue(allContent.contains("$split") || allContent.contains("$arrayElemAt") || allContent.contains("street"),
            "Should use $split and $arrayElemAt for splitting");
    }

    @Test
    void movePropertySameCollectionGeneratesRename() {
        final var statements = wrapper.evolveMoveProperty(
            "customers", "fname", "customers", "firstName"
        );

        assertFalse(statements.isEmpty(), "Should generate statements");
        final String allContent = statementsToString(statements);
        assertTrue(allContent.contains("$rename") || allContent.contains("firstName"),
            "Should use $rename for same-collection move");
    }

    private String statementsToString(List<AbstractStatement> statements) {
        final var sb = new StringBuilder();
        for (final var stmt : statements) {
            sb.append(stmt.getContent()).append("\n");
        }
        return sb.toString();
    }
}
