package cz.cuni.matfyz.transformations.processes;

import cz.cuni.matfyz.core.record.*;
import cz.cuni.matfyz.abstractWrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractWrappers.PullWrapperOptions;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.utils.Result;
import cz.cuni.matfyz.transformations.algorithms.ModelToCategory;

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

    public void input(AbstractPullWrapper pullWrapper, Mapping mapping) {
        this.pullWrapper = pullWrapper;
        this.mapping = mapping;
    }
    
    public Result<InstanceCategory> run() {
        ForestOfRecords forest;
        try {
            forest = pullWrapper.pullForest(mapping.accessPath(), new PullWrapperOptions.Builder().buildWithKindName(mapping.kindName()));
        }
        catch (Exception exception) {
            LOGGER.error("Pull forest failed.", exception);
            return new Result<InstanceCategory>("Pull forest failed.");
        }

        InstanceCategory instance = new InstanceCategoryBuilder().setSchemaCategory(mapping.category()).build();
        
        var transformation = new ModelToCategory();
		transformation.input(mapping, instance, forest);
		transformation.algorithm();

        return new Result<InstanceCategory>(instance);
    }

}
