package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.IdWithValues;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.MappingRow;
import cz.cuni.matfyz.core.schema.Key;

/**
 * @author jachymb.bartik
 */
public class TestInstanceCategoryBuilder {

    private final InstanceCategory category;
    
    public TestInstanceCategoryBuilder(InstanceCategory instance) {
        this.category = instance;
    }
    
    private final IdWithValues.Builder builder = new IdWithValues.Builder();
    
    public TestInstanceCategoryBuilder value(Signature signature, String value) {
        builder.add(signature, value);
        
        return this;
    }

    public DomainRow object(Key key) {
        var instanceObject = category.getObject(key);
        IdWithValues superId = builder.build();

        return instanceObject.createRow(superId);
    }
    
    public MappingRow morphism(Signature signature, DomainRow domainRow, DomainRow codomainRow) {
        var row = new MappingRow(domainRow, codomainRow);
        category.getMorphism(signature).addMapping(row);
        
        var dualRow = new MappingRow(codomainRow, domainRow);
        category.getMorphism(signature.dual()).addMapping(dualRow);
        
        return row;
    }

    public void morphism(Signature signature) {
        category.getMorphism(signature);
    }
    
}
