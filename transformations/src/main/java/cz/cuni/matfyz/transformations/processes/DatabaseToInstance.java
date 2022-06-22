package cz.cuni.matfyz.transformations.processes;

import cz.cuni.matfyz.core.record.*;
import cz.cuni.matfyz.abstractWrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractWrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.utils.DataResult;
import cz.cuni.matfyz.transformations.algorithms.MTCAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachym.bartik
 */
public class DatabaseToInstance {

    private static Logger LOGGER = LoggerFactory.getLogger(DatabaseToInstance.class);

    private Mapping mapping;
    private InstanceCategory currentInstance;
    private AbstractPullWrapper pullWrapper;

    public void input(Mapping mapping, InstanceCategory currentInstance, AbstractPullWrapper pullWrapper) {
        this.mapping = mapping;
        this.currentInstance = currentInstance;
        this.pullWrapper = pullWrapper;
    }
    
    public DataResult<InstanceCategory> run() {
        ForestOfRecords forest;
        try {
            forest = pullWrapper.pullForest(mapping.accessPath(), new PullWrapperOptions.Builder().buildWithKindName(mapping.kindName()));
        }
        catch (Exception exception) {
            LOGGER.error("Pull forest failed.", exception);
            return new DataResult<InstanceCategory>(null, "Pull forest failed.");
        }

        InstanceCategory instance = currentInstance != null ?
            currentInstance :
            new InstanceCategoryBuilder().setSchemaCategory(mapping.category()).build();

        var transformation = new MTCAlgorithm();
		transformation.input(mapping, instance, forest);
		transformation.algorithm();

        return new DataResult<InstanceCategory>(instance);
    }

}
