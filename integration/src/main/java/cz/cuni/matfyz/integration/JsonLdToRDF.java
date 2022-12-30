package cz.cuni.matfyz.integration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.query.Dataset;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
public class JsonLdToRDF {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonLdToRDF.class);

    private String dataFileName;

    public void input(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public Dataset algorithm() throws URISyntaxException, FileNotFoundException {
        final var jsonLdStream = getDataFileContents();

        final RDFParser parser = RDFParser.source(jsonLdStream)
            .forceLang(Lang.JSONLD11)
            .build();

        final Dataset dataset = parser.toDataset();

        LOGGER.info("Dataset parsed successfuly.");

        return dataset;

    }

    private InputStream getDataFileContents() throws URISyntaxException, FileNotFoundException {
        final var url = ClassLoader.getSystemResource(dataFileName);
        Path pathToDataFile = Paths.get(url.toURI()).toAbsolutePath();
        File dataFile = pathToDataFile.toFile();

        return new FileInputStream(dataFile);
    }

}
