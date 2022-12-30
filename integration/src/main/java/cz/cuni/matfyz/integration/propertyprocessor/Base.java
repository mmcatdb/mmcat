package cz.cuni.matfyz.integration.propertyprocessor;

import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceMorphism;
import cz.cuni.matfyz.core.instance.InstanceObject;
import cz.cuni.matfyz.integration.MorphismFinder;


/**
 * @author jachym.bartik
 */
public class Base {
    
    protected final InstanceCategory category;
    
    protected Base(InstanceCategory category) {
        this.category = category;
    }
    
    protected InstanceMorphism findMorphismFromObject(String pimIri, InstanceObject object) {
        return MorphismFinder.findFromObject(category, pimIri, object);
    }

    protected InstanceMorphism findDirectMorphismFromObject(String pimIri, InstanceObject object) {
        return MorphismFinder.findDirectFromObject(category, pimIri, object);
    }

}
