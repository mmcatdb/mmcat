package cz.matfyz.integration.processes;

import cz.matfyz.core.exception.NamedException;
import cz.matfyz.core.exception.OtherException;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceCategoryBuilder;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.Statistics;
import cz.matfyz.core.utils.Statistics.Interval;
import cz.matfyz.core.utils.io.InputStreamProvider;
import cz.matfyz.integration.algorithms.JsonLdToRDF;
import cz.matfyz.integration.algorithms.RDFToInstance;

import java.io.IOException;

/**
 * @author jachym.bartik
 */
public class JsonLdToCategory {

    private SchemaCategory category;
    private InstanceCategory currentInstance;
    private InputStreamProvider inputStreamProvider;

    public JsonLdToCategory input(SchemaCategory category, InstanceCategory currentInstance, InputStreamProvider inputStreamProvider) {
        this.category = category;
        this.currentInstance = currentInstance;
        this.inputStreamProvider = inputStreamProvider;

        return this;
    }

    public InstanceCategory run() {
        try {
            return innerRun();
        }
        catch (NamedException e) {
            throw e;
        }
        catch (Exception e) {
            throw new OtherException(e);
        }
    }

    private InstanceCategory innerRun() throws IOException {
        final InstanceCategory instance = currentInstance != null
            ? currentInstance
            : new InstanceCategoryBuilder().setSchemaCategory(category).build();

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

        return instance;
    }

}
