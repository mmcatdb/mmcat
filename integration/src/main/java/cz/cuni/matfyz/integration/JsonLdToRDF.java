package cz.cuni.matfyz.integration;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonLdToRDF {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogRDFHandler.class);

    private String dataFileName;

    public void input(String dataFileName) {
        this.dataFileName = dataFileName;
    }

    public void algorithm() throws Exception {
        final var jsonLdStream = getDataFileContents();

        final RDFParser parser = RDFParser.source(jsonLdStream)
            .forceLang(Lang.JSONLD11)
            .build();

        final Dataset dataset = parser.toDataset();
        LOGGER.info("Dataset parsed successfuly.");

        processModel(dataset.getDefaultModel());
        dataset.listModelNames().forEachRemaining(resource -> {
            final Model model = dataset.getNamedModel(resource.getURI());
            processModel(model);
        });
    }

    private void processModel(Model model) {
        model.listStatements().forEach(statement -> {
            LOGGER.info("[Statement]: " + statement);
        });
    }

    private InputStream getDataFileContents() throws Exception {
        final var url = ClassLoader.getSystemResource(dataFileName);
        Path pathToDataFile = Paths.get(url.toURI()).toAbsolutePath();
        File dataFile = pathToDataFile.toFile();

        return new FileInputStream(dataFile);
    }

}
