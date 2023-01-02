package cz.cuni.matfyz.integration.propertyprocessor;

import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.integration.utils.MorphismFinder;


/**
 * @author jachym.bartik
 */
public class Base {
    
    protected final InstanceCategory category;
    protected final MorphismFinder finder;
    
    protected Base(InstanceCategory category) {
        this.category = category;
        this.finder = new MorphismFinder(category);
    }
    
}
