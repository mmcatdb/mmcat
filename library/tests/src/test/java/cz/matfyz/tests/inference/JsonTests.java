package cz.matfyz.tests.inference;

import cz.matfyz.tests.example.basic.Datasources;
import cz.matfyz.tests.example.basic.MongoDB;
import cz.matfyz.tests.example.common.SparkProvider;
import cz.matfyz.tests.mock.MockJsonProvider;
import cz.matfyz.wrapperjson.JsonControlWrapper;
import cz.matfyz.wrapperjson.JsonInferenceWrapper;
import cz.matfyz.wrapperjson.JsonProvider;
import cz.matfyz.wrapperjson.JsonProvider.JsonSettings;

import java.io.UncheckedIOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class JsonTests {

    private static final SparkProvider sparkProvider = new SparkProvider();

    private JsonInferenceWrapper createInferenceWrapper(JsonProvider provider) {
        final var control = new JsonControlWrapper(provider);
        control.enableSpark(sparkProvider.getSettings());

        return control.getInferenceWrapper(provider.getKindName());
    }

    private static final Datasources datasources = new Datasources();

    @Test
    void todo() {

        final var content = "[ { \"number\": \"o_100\" }, { \"number\": \"o_200\" } ]";
        // final var content = "{ \"number\": \"o_100\" }\n{ \"number\": \"o_200\" }";

        final var provider = new MockJsonProvider(content);
        final var control = new JsonControlWrapper(provider);
        final var pull = control.getPullWrapper();

        final var output = pull.pullForest(MongoDB.order(datasources.schema).accessPath(), null);

        System.out.println(output);

    }

    @Test
    void canReadFromServerUrl() throws Exception {
        final var uri = new URI("https://data.mmcatdb.com/yelp_business_sample.json");
        final var provider = new JsonProvider(new JsonSettings(uri.toString(), false, false, false));
        final var inference = createInferenceWrapper(provider);

        assertDoesNotThrow(() -> {
            inference.loadDocuments();
        });
    }

    @Test
    void canLoadDocumentsFromFile() throws Exception {
        final var uri = ClassLoader.getSystemResource("inferenceSampleYelp.json").toURI();
        final var provider = new JsonProvider(new JsonSettings(uri.toString(), false, false, false));
        final var inference = createInferenceWrapper(provider);

        final var documents = inference.loadDocuments();

        assertNotNull(documents, "Documents should not be null");
        assertFalse(documents.isEmpty(), "Documents should not be empty");
    }

    @Test
    void canLoadDocumentsFromJsonArray() throws Exception {
        final var provider = new MockJsonProvider("[{\"key1\":\"value1\"},{\"key2\":\"value2\"}]");
        final var inference = createInferenceWrapper(provider);

        final var documents = inference.loadDocuments();

        assertNotNull(documents, "Documents should not be null");
        assertFalse(documents.isEmpty(), "Documents should not be empty");
        assertEquals(2, documents.count(), "There should be two documents");
    }

    @Test
    void canLoadDocumentsFromJsonObjects() throws Exception {
        final var provider = new MockJsonProvider("{\"key\":\"value1\"}\n{\"key\":\"value2\"}");
        final var inference = createInferenceWrapper(provider);

        final var documents = inference.loadDocuments();

        assertNotNull(documents, "Documents should not be null");
        assertFalse(documents.isEmpty(), "Documents should not be empty");
        assertEquals(2, documents.count(), "There should be two documents");
    }

    @Test
    void cannotLoadDocumentsFromInvalidJson() throws Exception {
        final var provider = new MockJsonProvider("{\"key\":\"value\"}\n{\"invalidJson\"");
        final var inference = createInferenceWrapper(provider);

        assertThrows(UncheckedIOException.class, () -> {
            inference.loadDocuments();
        });
    }

}
