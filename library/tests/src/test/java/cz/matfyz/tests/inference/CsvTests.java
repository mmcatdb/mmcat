package cz.matfyz.tests.inference;

import cz.matfyz.tests.example.common.SparkProvider;
import cz.matfyz.wrappercsv.CsvControlWrapper;
import cz.matfyz.wrappercsv.CsvInferenceWrapper;
import cz.matfyz.wrappercsv.CsvProvider;
import cz.matfyz.wrappercsv.CsvProvider.CsvSettings;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class CsvTests {

    private final SparkProvider sparkProvider = new SparkProvider();

    CsvInferenceWrapper setup(CsvProvider csvProvider) {
        final var wrapper = new CsvControlWrapper(csvProvider).getInferenceWrapper(sparkProvider.getSettings());
        wrapper.startSession();

        return wrapper;
    }

    @Test
    void testServerUrl() throws Exception { // testing file
        @SuppressWarnings("deprecation")
        URL url = new URL("https://data.mmcatdb.com/googleplaystore.csv");
        final var settings = new CsvSettings(url.toURI().toString(), false, false);
        final var csvProvider = new CsvProvider(settings);

        final List<String> fileNames = csvProvider.getCsvFileNames();

        assertEquals("googleplaystore", fileNames.get(0));

        try (InputStream inputStream = csvProvider.getInputStream("googleplaystore")) {
            assertNotNull(inputStream);
        }
    }

    @Test
    void testLoadDocumentsBasicFromFile() throws Exception {
        URL url = ClassLoader.getSystemResource("inferenceSampleGoogleApps.csv"); // the file includes comma as a delimiter
        final var settings = new CsvSettings(url.toURI().toString(), false, false);
        final var csvProvider = new CsvProvider(settings);
        CsvInferenceWrapper inferenceWrapper = setup(csvProvider);

        var documents = inferenceWrapper.loadDocuments();

        assertNotNull(documents, "Documents should not be null");
        assertFalse(documents.isEmpty(), "Documents should not be empty");
        assertEquals(3, documents.count(), "There should be three documents");
    }

    @Test
    void testDelimiterSemicolon() throws Exception {
        String csvContent = "key1;key2\nvalue1;value2\nvalue3;value4";
        CsvSettings settings = new CsvSettings("", false, false);
        CsvProvider csvProvider = new StringCsvProvider(settings, csvContent);

        CsvInferenceWrapper inferenceWrapper = setup(csvProvider);

        var documents = inferenceWrapper.loadDocuments();

        assertNotNull(documents, "Documents should not be null");
        assertFalse(documents.isEmpty(), "Documents should not be empty");
        assertEquals(2, documents.count(), "There should be two documents");
    }

    @Test
    void testDelimiterTab() throws Exception {
        String csvContent = "key1\tkey2\nvalue1\tvalue2\nvalue3\tvalue4,value5";
        CsvSettings settings = new CsvSettings("", false, false);
        CsvProvider csvProvider = new StringCsvProvider(settings, csvContent);

        CsvInferenceWrapper inferenceWrapper = setup(csvProvider);

        var documents = inferenceWrapper.loadDocuments();

        assertNotNull(documents, "Documents should not be null");
        assertFalse(documents.isEmpty(), "Documents should not be empty");
        assertEquals(2, documents.count(), "There should be two documents");
    }

    private static class StringCsvProvider extends CsvProvider {
        private final String csvContent;

        StringCsvProvider(CsvSettings settings, String csvContent) {
            super(settings);
            this.csvContent = csvContent;
        }

        @Override public InputStream getInputStream(String kindName) {
            return new ByteArrayInputStream(csvContent.getBytes());
        }
    }
}
