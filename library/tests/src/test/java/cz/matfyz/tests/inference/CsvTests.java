package cz.matfyz.tests.inference;

import cz.matfyz.tests.example.common.SparkProvider;
import cz.matfyz.tests.mock.MockCsvProvider;
import cz.matfyz.wrappercsv.CsvControlWrapper;
import cz.matfyz.wrappercsv.CsvInferenceWrapper;
import cz.matfyz.wrappercsv.CsvProvider;
import cz.matfyz.wrappercsv.CsvProvider.CsvSettings;

import java.net.URI;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class CsvTests {

    private static final SparkProvider sparkProvider = new SparkProvider();

    private CsvInferenceWrapper createInferenceWrapper(CsvProvider provider) {
        final var control = new CsvControlWrapper(provider);
        control.enableSpark(sparkProvider.getSettings());

        return control.getInferenceWrapper(provider.getKindName());
    }

    @Test
    void canReadFromServerUrl() throws Exception { // testing file
        final var uri = new URI("https://data.mmcatdb.com/googleplaystore.csv");
        final var provider = new CsvProvider(new CsvSettings(uri.toString(), ',', true, false, false, false));
        final var inference = createInferenceWrapper(provider);

        assertDoesNotThrow(() -> {
            inference.loadDocuments();
        });
    }

    @Test
    void canLoadDocumentsFromFile() throws Exception {
        final URL url = ClassLoader.getSystemResource("inferenceSampleGoogleApps.csv"); // the file includes comma as a delimiter
        final var provider = new CsvProvider(new CsvSettings(url.toURI().toString(), ',', true, false, false, false));
        final var inference = createInferenceWrapper(provider);
        final var documents = inference.loadDocuments();

        assertEquals(3, documents.count(), "There should be three documents");
    }

    @Test
    void delimiterSemicolon() throws Exception {
        final CsvProvider provider = new MockCsvProvider(';', true, """
            key1;key2
            value1;value2
            value3;value4
            """);
        final var inference = createInferenceWrapper(provider);

        final var documents = inference.loadDocuments();

        assertEquals(2, documents.count(), "There should be two documents");
    }

    @Test
    void delimiterTab() throws Exception {
        final CsvProvider provider = new MockCsvProvider('\t', true, """
            key1\tkey2
            value1\tvalue2
            value3\tvalue4,value5
            """);
        final var inference = createInferenceWrapper(provider);

        final var documents = inference.loadDocuments();

        assertEquals(2, documents.count(), "There should be two documents");
    }

    @Test
    void quotes() throws Exception {
        final CsvProvider provider = new MockCsvProvider(',', true, """
            key1,key2
            "valueA,valueB","valueC valueD"
            "value1 \\" value3",value4 value5
            """);
        final var inference = createInferenceWrapper(provider);

        final var documents = inference.loadDocuments();

        assertEquals(2, documents.count(), "There should be two documents");
    }

    @Test
    void noHeader() throws Exception {
        final CsvProvider provider = new MockCsvProvider(',', false, """
            value1,value2
            value3,value4
            value5,value6
            """);
        final var inference = createInferenceWrapper(provider);

        final var documents = inference.loadDocuments();

        assertEquals(3, documents.count(), "There should be two documents");
    }

}
