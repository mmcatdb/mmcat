package cz.matfyz.tests.inference;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.wrappercsv.CsvControlWrapper;
import cz.matfyz.wrappercsv.CsvInferenceWrapper;
import cz.matfyz.wrappercsv.CsvProvider;
import cz.matfyz.wrappercsv.CsvProvider.CsvSettings;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class CsvInferenceWrapperTests {

    CsvInferenceWrapper setup(CsvProvider csvProvider) throws Exception {
        final var sparkSettings = new SparkSettings("local[*]", "./spark");
        CsvInferenceWrapper csvInferenceWrapper = new CsvControlWrapper(csvProvider).getInferenceWrapper(sparkSettings);
        csvInferenceWrapper.buildSession();
        csvInferenceWrapper.initiateContext();
        return csvInferenceWrapper;
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

    // test class to test csv formats w/o having to load a file
    private static class StringCsvProvider extends CsvProvider {
        private final String csvContent;

        StringCsvProvider(CsvSettings settings, String csvContent) {
            super(settings);
            this.csvContent = csvContent;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(csvContent.getBytes());
        }
    }
}
