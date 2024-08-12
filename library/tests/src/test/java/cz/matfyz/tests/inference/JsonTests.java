package cz.matfyz.tests.inference;

import cz.matfyz.tests.example.common.SparkProvider;
import cz.matfyz.wrapperjson.JsonControlWrapper;
import cz.matfyz.wrapperjson.JsonInferenceWrapper;
import cz.matfyz.wrapperjson.JsonProvider;
import cz.matfyz.wrapperjson.JsonProvider.JsonSettings;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class JsonTests {

    private final SparkProvider sparkProvider = new SparkProvider();

    JsonInferenceWrapper setup(JsonProvider jsonProvider) {
        final var wrapper = new JsonControlWrapper(jsonProvider).getInferenceWrapper(sparkProvider.getSettings());
        wrapper.startSession();

        return wrapper;
    }

    @Test
    void testServerUrl() throws Exception {
        @SuppressWarnings("deprecation")
        URL url = new URL("https://data.mmcatdb.com/yelp_business_sample.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);

        final List<String> fileNames = jsonProvider.getJsonFileNames();

        assertEquals("yelp_business_sample", fileNames.get(0));

        try (InputStream inputStream = jsonProvider.getInputStream("yelpbusinesssampel")) {
            assertNotNull(inputStream);
        }
    }

    @Test
    void testLocalUrlFolder() throws Exception {
        @SuppressWarnings("deprecation")
        URL url = new URL("file:///mnt/c/Users/alzbe/Documents/mff_mgr/Diplomka/Datasets/test_json_folder/");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);

        final List<String> fileNames = jsonProvider.getJsonFileNames();

        for (String fileName : fileNames) {
            System.out.println(fileName);
            try (InputStream inputStream = jsonProvider.getInputStream(fileName)) {
                assertNotNull(inputStream);
            }
        }
    }

    @Test
    void testLocalUrlFile() throws Exception {
        @SuppressWarnings("deprecation")
        URL url = new URL("file:///mnt/c/Users/alzbe/Documents/mff_mgr/Diplomka/Datasets/test_json_folder/customer.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);

        final List<String> fileNames = jsonProvider.getJsonFileNames();

        assertEquals("customer", fileNames.get(0));

        try (InputStream inputStream = jsonProvider.getInputStream("yelpbusinesssampel")) {
            assertNotNull(inputStream);
        }
    }

    @Test
    void testLoadDocumentsBasicFromFile() throws Exception {
        URL url = ClassLoader.getSystemResource("inferenceSampleYelp.json");
        final var settings = new JsonSettings(url.toURI().toString(), false, false);
        final var jsonProvider = new JsonProvider(settings);
        JsonInferenceWrapper inferenceWrapper = setup(jsonProvider);

        var documents = inferenceWrapper.loadDocuments();

        assertNotNull(documents, "Documents should not be null");
        assertFalse(documents.isEmpty(), "Documents should not be empty");
    }

    @Test
    void testLoadDocumentsFromArrayJson() throws Exception {
        String jsonArray = "[{\"key1\":\"value1\"},{\"key2\":\"value2\"}]";
        JsonSettings settings = new JsonSettings("", false, false);
        JsonProvider jsonProvider = new StringJsonProvider(settings, jsonArray);

        JsonInferenceWrapper inferenceWrapper = setup(jsonProvider);

        var documents = inferenceWrapper.loadDocuments();

        assertNotNull(documents, "Documents should not be null");
        assertFalse(documents.isEmpty(), "Documents should not be empty");
        assertEquals(2, documents.count(), "There should be two documents");
    }

    @Test
    void testLoadDocumentsFromObjectJson() throws Exception {
        String jsonObject = "{\"key\":\"value\"}";
        JsonSettings settings = new JsonSettings("", false, false);
        JsonProvider jsonProvider = new StringJsonProvider(settings, jsonObject);

        JsonInferenceWrapper inferenceWrapper = setup(jsonProvider);

        var documents = inferenceWrapper.loadDocuments();

        assertNotNull(documents, "Documents should not be null");
        assertFalse(documents.isEmpty(), "Documents should not be empty");
        assertEquals(1, documents.count(), "There should be one document");
    }

    @Test // TODO: handle malformed files better
    void testLoadDocumentsWithMalformedJson() throws Exception {
        String malformedJson = "{\"key\":\"value\"}\n{\"malformedJson\"";
        JsonSettings settings = new JsonSettings("", false, false);
        JsonProvider jsonProvider = new StringJsonProvider(settings, malformedJson);

        JsonInferenceWrapper inferenceWrapper = setup(jsonProvider);

        var documents = inferenceWrapper.loadDocuments();

        assertNotNull(documents, "Documents should not be null");
        assertFalse(documents.isEmpty(), "Documents should not be empty");
    }

    // test class to test json formats w/o having to load a file
    private static class StringJsonProvider extends JsonProvider {
        private final String jsonContent;

        StringJsonProvider(JsonSettings settings, String jsonContent) {
            super(settings);
            this.jsonContent = jsonContent;
        }

        @Override
        public InputStream getInputStream(String kindName) {
            return new ByteArrayInputStream(jsonContent.getBytes());
        }
    }
}
