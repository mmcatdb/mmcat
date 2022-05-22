package cz.cuni.matfyz.transformations.processes;

import cz.cuni.matfyz.core.record.*;
import cz.cuni.matfyz.abstractWrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractWrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.utils.Result;
import cz.cuni.matfyz.transformations.algorithms.MTCAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachym.bartik
 */
public class DatabaseToInstance {

    private static Logger LOGGER = LoggerFactory.getLogger(DatabaseToInstance.class);

    private AbstractPullWrapper pullWrapper;
    private Mapping mapping;
    private InstanceCategory defaultInstance;

    public void input(AbstractPullWrapper pullWrapper, Mapping mapping, InstanceCategory defaultInstance) {
        this.pullWrapper = pullWrapper;
        this.mapping = mapping;
        this.defaultInstance = defaultInstance;
    }
    
    public Result<InstanceCategory> run() {
        ForestOfRecords forest;
        try {
            forest = pullWrapper.pullForest(mapping.accessPath(), new PullWrapperOptions.Builder().buildWithKindName(mapping.kindName()));
        }
        catch (Exception exception) {
            LOGGER.error("Pull forest failed.", exception);
            return new Result<InstanceCategory>(null, "Pull forest failed.");
        }

        InstanceCategory instance = defaultInstance != null ?
            defaultInstance :
            new InstanceCategoryBuilder().setSchemaCategory(mapping.category()).build();

        var transformation = new MTCAlgorithm();
		transformation.input(mapping, instance, forest);
		transformation.algorithm();

        return new Result<InstanceCategory>(instance);
    }

}
