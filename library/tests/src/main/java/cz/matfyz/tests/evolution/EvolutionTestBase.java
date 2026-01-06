package cz.matfyz.tests.evolution;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceBuilder;
import cz.matfyz.core.instance.InstanceBuilder.InstanceAdder;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceObjex;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.IterableUtils;
import cz.matfyz.evolution.category.SMO;
import cz.matfyz.evolution.instance.InstanceEvolver;

/**
 * Base class for testing instance evolution operations.
 * Follows the pattern of MTCAlgorithmTestBase for consistency.
 *
 * Test flow:
 * 1. Build initial instance from initial data
 * 2. Apply SMO operations (which update both schema via up() and instance via
 * accept())
 * 3. Build expected instance from expected data (using the evolved schema)
 * 4. Deep compare: objexes, morphisms, rows, and mappings
 */
public class EvolutionTestBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvolutionTestBase.class);

    private SchemaCategory schema;
    private InstanceAdder initialInstanceAdder;
    private final List<SMO> operations = new ArrayList<>();
    private InstanceAdder expectedInstanceAdder;

    /**
     * Sets the schema category to use for the test.
     */
    public EvolutionTestBase schema(SchemaCategory schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Sets the initial instance data before evolution.
     */
    public EvolutionTestBase initialInstance(InstanceAdder instanceAdder) {
        this.initialInstanceAdder = instanceAdder;
        return this;
    }

    /**
     * Adds a schema modification operation to apply.
     */
    public EvolutionTestBase operation(SMO operation) {
        this.operations.add(operation);
        return this;
    }

    /**
     * Sets the expected instance data after evolution.
     */
    public EvolutionTestBase expectedInstance(InstanceAdder instanceAdder) {
        this.expectedInstanceAdder = instanceAdder;
        return this;
    }

    /**
     * Runs the test by:
     * 1. Building the initial instance from the original schema
     * 2. Applying SMO operations (both schema via up() and instance via accept())
     * 3. Building expected instance from the evolved schema
     * 4. Deep comparison of actual vs expected
     */
    public void run() {
        // Build initial instance with original schema
        final var initialBuilder = new InstanceBuilder(schema);
        if (initialInstanceAdder != null) {
            initialInstanceAdder.add(initialBuilder);
        }
        final InstanceCategory actualInstance = initialBuilder.build();

        LOGGER.debug("INITIAL INSTANCE:\n{}", actualInstance);

        // Apply SMO operations: each operation modifies both schema and instance
        final var metadata = cz.matfyz.core.metadata.MetadataCategory.createEmpty(schema);
        final var evolver = new InstanceEvolver(actualInstance);

        for (final SMO operation : operations) {
            LOGGER.debug("Applying operation: {}", operation);
            // First update the schema
            operation.up(schema, metadata);
            // Then propagate to instance
            operation.accept(evolver);
        }

        LOGGER.debug("ACTUAL INSTANCE AFTER EVOLUTION:\n{}", actualInstance);

        // Build expected instance using the evolved schema
        final var expectedBuilder = new InstanceBuilder(schema);
        if (expectedInstanceAdder != null) {
            expectedInstanceAdder.add(expectedBuilder);
        }
        final InstanceCategory expectedInstance = expectedBuilder.build();

        LOGGER.debug("EXPECTED INSTANCE:\n{}", expectedInstance);

        // Compare objexes and morphisms collections
        assertEquals(
                expectedInstance.allObjexes().stream().toList(),
                actualInstance.allObjexes().stream().toList(),
                "Test objexes differ from the expected objexes.");

        assertEquals(
                expectedInstance.allMorphisms().stream().toList(),
                actualInstance.allMorphisms().stream().toList(),
                "Test morphisms differ from the expected morphisms.");

        // Deep comparison: compare rows and their mappings in each objex
        // This mirrors MTCAlgorithmTestBase's detailed comparison
        for (final var expectedObjex : expectedInstance.allObjexes()) {
            final var actualObjex = actualInstance.getObjex(expectedObjex.schema.key());

            final var expectedString = IterableUtils.toSet(expectedObjex.allRows()).stream()
                    .map(row -> rowToMappingsString(row, expectedObjex, schema))
                    .sorted().toList();

            final var actualString = IterableUtils.toSet(actualObjex.allRows()).stream()
                    .map(row -> rowToMappingsString(row, actualObjex, schema))
                    .sorted().toList();

            assertEquals(expectedString, actualString,
                    "Rows and mappings in objex " + expectedObjex.schema.key() + " differ from expected.");
        }
    }

    /**
     * Converts a row to a string representation including all its mappings.
     * Mirrors the approach in MTCAlgorithmTestBase for detailed comparison.
     */
    private static String rowToMappingsString(DomainRow row, InstanceObjex objex, SchemaCategory schema) {
        String output = "\n[row] (" + objex.schema.key() + ") " + row.toStringForTests(true);

        for (final var entry : row.getAllMappingsFrom()) {
            final var signature = entry.getKey();
            final var codObjex = schema.getMorphism(signature).cod();

            output += "\n\tmappings --[" + entry.getKey() + "]->(" + codObjex.key() + "): "
                    + entry.getValue().cod().toStringForTests(true);
        }

        return output + "\n";
    }

}
