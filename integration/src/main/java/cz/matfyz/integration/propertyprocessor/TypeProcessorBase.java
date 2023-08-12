package cz.matfyz.integration.propertyprocessor;

import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.integration.utils.MorphismFinder;

/**
 * @author jachym.bartik
 */
public class TypeProcessorBase {

    protected final InstanceCategory category;
    protected final MorphismFinder finder;
    
    protected TypeProcessorBase(InstanceCategory category) {
        this.category = category;
        this.finder = new MorphismFinder(category);
    }

}
