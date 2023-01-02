package cz.cuni.matfyz.integration.processes;

import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.core.utils.Statistics;
import cz.cuni.matfyz.core.utils.Statistics.Interval;
import cz.cuni.matfyz.core.utils.io.InputStreamProvider;
import cz.cuni.matfyz.integration.algorithms.JsonLdToRDF;
import cz.cuni.matfyz.integration.algorithms.RDFToInstance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachym.bartik
 */
public class JsonLdToInstance {

    private static final Logger LOGGER = LoggerFactory.getLogger(JsonLdToInstance.class);

    private SchemaCategory category;
    private InstanceCategory currentInstance;
    private InputStreamProvider inputStreamProvider;

    public void input(SchemaCategory category, InstanceCategory currentInstance, InputStreamProvider inputStreamProvider) {
        this.category = category;
        this.currentInstance = currentInstance;
        this.inputStreamProvider = inputStreamProvider;
    }

    public DataResult<InstanceCategory> run() {

        final InstanceCategory instance = currentInstance != null
            ? currentInstance
            : new InstanceCategoryBuilder().setSchemaCategory(category).build();

        try {
            final var jsonToRDF = new JsonLdToRDF();
            jsonToRDF.input(inputStreamProvider);
            
            Statistics.start(Interval.JSON_LD_TO_RDF);
            final var dataset = jsonToRDF.algorithm();
            Statistics.end(Interval.JSON_LD_TO_RDF);
            
            final var rdfToInstance = new RDFToInstance();
            rdfToInstance.input(dataset, instance);

            Statistics.start(Interval.RDF_TO_INSTANCE);
            rdfToInstance.algorithm();
            Statistics.end(Interval.RDF_TO_INSTANCE);
        }
        catch (Exception exception) {
            LOGGER.error("JSON-LD to instance failed.", exception);
            return new DataResult<>(null, "JSON-LD to instance failed.");
        }

        return new DataResult<>(instance);
    }

}
