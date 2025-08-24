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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;

class CsvTests {

    private final SparkProvider sparkProvider = new SparkProvider();

    CsvInferenceWrapper setup(CsvProvider provider) {
        final var wrapper = new CsvControlWrapper(provider)
            .enableSpark(sparkProvider.getSettings())
            .getInferenceWrapper();
        wrapper.startSession();

        return (CsvInferenceWrapper) wrapper;
    }

    @Test
    void testServerUrl() throws Exception { // testing file
        @SuppressWarnings("deprecation")
        final URL url = new URL("https://data.mmcatdb.com/googleplaystore.csv");
        final var settings = new CsvSettings(url.toURI().toString(), ',', true, false, false, false);
        final var provider = new CsvProvider(settings);

        final List<String> filenames = List.of(provider.getCsvFilenames());

        assertEquals("googleplaystore", filenames.get(0));

        try (
            InputStream inputStream = provider.getInputStream()
        ) {
            assertNotNull(inputStream);
        }
        catch (Exception e) {
            fail(e);
        }
    }

    @Test
    void testLoadDocumentsBasicFromFile() throws Exception {
        final URL url = ClassLoader.getSystemResource("inferenceSampleGoogleApps.csv"); // the file includes comma as a delimiter
        final var settings = new CsvSettings(url.toURI().toString(), ',', true, false, false, false);
        final var provider = new CsvProvider(settings);
        final CsvInferenceWrapper inferenceWrapper = setup(provider);
        final var documents = inferenceWrapper.loadDocuments();

        assertEquals(3, documents.count(), "There should be three documents");
    }

    @Test
    void testDelimiterSemicolon() throws Exception {
        final CsvProvider provider = new StringCsvProvider(';', true, """
            key1;key2
            value1;value2
            value3;value4
            """);

        final CsvInferenceWrapper inferenceWrapper = setup(provider);
        final var documents = inferenceWrapper.loadDocuments();

        assertEquals(2, documents.count(), "There should be two documents");
    }

    @Test
    void testDelimiterTab() throws Exception {
        final CsvProvider provider = new StringCsvProvider('\t', true, """
            key1\tkey2
            value1\tvalue2
            value3\tvalue4,value5
            """);

        final CsvInferenceWrapper inferenceWrapper = setup(provider);
        final var documents = inferenceWrapper.loadDocuments();

        assertEquals(2, documents.count(), "There should be two documents");
    }

    @Test
    void testQuotes() throws Exception {
        final CsvProvider provider = new StringCsvProvider(',', true, """
            key1,key2
            "valueA,valueB","valueC valueD"
            "value1 \\" value3",value4 value5
            """);

        final CsvInferenceWrapper inferenceWrapper = setup(provider);
        final var documents = inferenceWrapper.loadDocuments();

        assertEquals(2, documents.count(), "There should be two documents");
    }

    @Test
    void testNoHeader() throws Exception {
        final CsvProvider provider = new StringCsvProvider(',', false, """
            value1,value2
            value3,value4
            value5,value6
            """);

        final CsvInferenceWrapper inferenceWrapper = setup(provider);
        final var documents = inferenceWrapper.loadDocuments();

        assertEquals(3, documents.count(), "There should be two documents");
    }

    private static class StringCsvProvider extends CsvProvider {
        private final String csvContent;

        StringCsvProvider(char separator, boolean hasHeader, String csvContent) {
            super(new CsvSettings("", separator, hasHeader, false, false, false));
            this.csvContent = csvContent;
        }

        @Override public InputStream getInputStream() {
            return new ByteArrayInputStream(csvContent.getBytes());
        }
    }
}
