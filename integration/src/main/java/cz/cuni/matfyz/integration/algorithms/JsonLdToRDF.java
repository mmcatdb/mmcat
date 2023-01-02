package cz.cuni.matfyz.integration.algorithms;

import cz.cuni.matfyz.core.utils.io.InputStreamProvider;

import java.io.IOException;

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

    private InputStreamProvider inputStreamProvider;

    public void input(InputStreamProvider inputStreamProvider) {
        this.inputStreamProvider = inputStreamProvider;
    }

    public Dataset algorithm() throws IOException {
        final var jsonLdStream = inputStreamProvider.getInputStream();

        final RDFParser parser = RDFParser.source(jsonLdStream)
            .forceLang(Lang.JSONLD11)
            .build();

        final Dataset dataset = parser.toDataset();

        LOGGER.info("Dataset parsed successfuly.");

        return dataset;

    }
    
}
