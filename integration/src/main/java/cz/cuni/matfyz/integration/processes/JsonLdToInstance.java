package cz.cuni.matfyz.integration.processes;

import cz.cuni.matfyz.core.exception.NamedException;
import cz.cuni.matfyz.core.exception.OtherException;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.utils.Statistics;
import cz.cuni.matfyz.core.utils.Statistics.Interval;
import cz.cuni.matfyz.core.utils.io.InputStreamProvider;
import cz.cuni.matfyz.integration.algorithms.JsonLdToRDF;
import cz.cuni.matfyz.integration.algorithms.RDFToInstance;

import java.io.IOException;

/**
 * @author jachym.bartik
 */
public class JsonLdToInstance {

    private SchemaCategory category;
    private InstanceCategory currentInstance;
    private InputStreamProvider inputStreamProvider;

    public JsonLdToInstance input(SchemaCategory category, InstanceCategory currentInstance, InputStreamProvider inputStreamProvider) {
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
