package cz.cuni.matfyz.core;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.MappingRow;
import cz.cuni.matfyz.core.instance.SuperIdWithValues;
import cz.cuni.matfyz.core.schema.Key;

/**
 * @author jachymb.bartik
 */
public class TestInstanceCategoryBuilder {

    private final InstanceCategory category;
    
    public TestInstanceCategoryBuilder(InstanceCategory instance) {
        this.category = instance;
    }
    
    private final SuperIdWithValues.Builder builder = new SuperIdWithValues.Builder();
    
    public TestInstanceCategoryBuilder value(Signature signature, String value) {
        builder.add(signature, value);
        
        return this;
    }

    public DomainRow object(Key key) {
        var instanceObject = category.getObject(key);
        SuperIdWithValues superId = builder.build();

        var row = instanceObject.getRow(superId);
        if (row != null)
            return row;

        return instanceObject.getOrCreateRow(superId);
    }
    
    public MappingRow morphism(Signature signature, DomainRow domainRow, DomainRow codomainRow) {
        var row = new MappingRow(domainRow, codomainRow);
        category.getMorphism(signature).addMapping(row);
        
        return row;
    }

    public void morphism(Signature signature) {
        category.getMorphism(signature);
    }
    
}
