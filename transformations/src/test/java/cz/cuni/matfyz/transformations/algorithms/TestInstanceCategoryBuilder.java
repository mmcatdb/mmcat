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

    private final InstanceCategory instance;
    
    public TestInstanceCategoryBuilder(InstanceCategory instance) {
        this.instance = instance;
    }
    
    private final IdWithValues.Builder builder = new IdWithValues.Builder();
    
    public TestInstanceCategoryBuilder value(Signature signature, String value) {
        builder.add(signature, value);
        
        return this;
    }

    public DomainRow object(Key key) {
        var instanceObject = instance.getObject(key);
        IdWithValues idWithValues = builder.build();

        var domainRow = new DomainRow(idWithValues, instanceObject);
        instanceObject.addRow(domainRow);
        
        return domainRow;
    }
    
    public MappingRow morphism(Signature signature, DomainRow domainRow, DomainRow codomainRow) {
        var row = new MappingRow(domainRow, codomainRow);
        instance.getMorphism(signature).addMapping(row);
        
        var dualRow = new MappingRow(codomainRow, domainRow);
        instance.getMorphism(signature.dual()).addMapping(dualRow);
        
        return row;
    }

    public void morphism(Signature signature) {
        instance.getMorphism(signature);
    }
    
}
