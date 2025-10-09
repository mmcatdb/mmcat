package cz.matfyz.tests.inference;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.printable.Printer;
import cz.matfyz.core.utils.printable.Stringifier;
import cz.matfyz.inference.MMInferOneInAll;
import cz.matfyz.inference.schemaconversion.utils.InferenceResult;
import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.basic.MongoDB;
import cz.matfyz.tests.example.common.SparkProvider;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.tests.example.common.TestMapping;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class MongoDBTests {

    private static final SparkProvider sparkProvider = new SparkProvider();

    private static final Datasources datasources = new Datasources();
    private static final TestDatasource<MongoDBControlWrapper> datasource = datasources.mongoDB();

    @BeforeAll
    public static void setupAll() {
        datasource.setup();
        datasource.wrapper.enableSpark(sparkProvider.getSettings());
    }

    @Test
    void canReadFromDb() throws Exception {
        final var kindNames = datasource.wrapper.getPullWrapper().getKindNames();
        final var firstKind = kindNames.get(0);
        final var inference = datasource.wrapper.getInferenceWrapper(firstKind);

        assertDoesNotThrow(() -> {
            inference.loadRecords();
        });
    }

    @Test
    void basicInference() throws Exception {
        final var result = new MMInferOneInAll()
            .input(datasource.wrapper.createProvider())
            .run();

        // The incomplete kinds can't be inferred properly so they are commented out.
        // The inference algorithms are run on all kinds from the database, so at least we know they don't crash, we just don't check the results.
        assertKindMappingEquals(result, MongoDB.orderKind, MongoDB::order);
        assertKindMappingEquals(result, MongoDB.addressKind, MongoDB::address);
        // assertKindMappingEquals(result, MongoDB.addressMissingSimpleKind, MongoDB::addressMissingSimple);
        // assertKindMappingEquals(result, MongoDB.addressMissingComplexKind, MongoDB::addressMissingComplex);
        assertKindMappingEquals(result, MongoDB.tagKind, MongoDB::tag);
        assertKindMappingEquals(result, MongoDB.itemKind, MongoDB::item);
        // assertKindMappingEquals(result, MongoDB.itemEmptyKind, MongoDB::itemEmpty);

        // Dynamic names can't be inferred, they have to be created manually.
        // assertKindMappingEquals(result, MongoDB.contactKind, MongoDB::contact);
        // Auxiliary properties can't be inferred, they have to be created manually.
        // assertKindMappingEquals(result, MongoDB.customerKind, MongoDB::customer);
        // Again, dynamic name.
        // assertKindMappingEquals(result, MongoDB.noteKind, MongoDB::note);
    }

    private void assertKindMappingEquals(InferenceResult result, String kindName, Function<SchemaCategory, TestMapping> expectedProvider) {
        final var actual = findMappingForKind(result, kindName);
        assertNotNull(actual);

        final var expected = expectedProvider.apply(datasources.schema);

        // So that the error message is more informative.
        final String prefix = kindName + ":\n";

        // Override all signatures to <s> to ignore them in comparison.
        final var stringifier = new Stringifier()
            .set(Signature.class, s -> "<s>")
            .set(BaseSignature.class, s -> "<s>");

        final var actualString = prefix + Printer.print(actual.accessPath(), stringifier);
        final var expectedString = prefix + Printer.print(expected.accessPath(), stringifier);

        assertEquals(expectedString, actualString);
    }

    private @Nullable Mapping findMappingForKind(InferenceResult result, String kindName) {
        for (final var pair : result.pairs()) {
            for (final var mapping : pair.mappings()) {
                if (mapping.kindName().equals(kindName))
                    return mapping;
            }
        }

        return null;
    }

}
