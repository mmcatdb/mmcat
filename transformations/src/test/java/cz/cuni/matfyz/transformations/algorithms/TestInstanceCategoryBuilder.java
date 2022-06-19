package cz.cuni.matfyz.transformations.algorithms;

import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.category.*;

import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class TestInstanceCategoryBuilder
{
    private final InstanceCategory instance;
    
    public TestInstanceCategoryBuilder(InstanceCategory instance)
    {
        this.instance = instance;
    }
    
    private final IdWithValues.Builder builder = new IdWithValues.Builder();
    
    public TestInstanceCategoryBuilder value(Signature signature, String value)
    {
        builder.add(signature, value);
        
        return this;
    }
    
    public ActiveDomainRow object(Key key)
    {
        IdWithValues idWithValues = builder.build();
        var activeDomainRow = new ActiveDomainRow(idWithValues);
        
        Map<IdWithValues, ActiveDomainRow> innerMap = instance.getObject(key).activeDomain().get(idWithValues.id());
        if (innerMap == null)
        {
            innerMap = new TreeMap<>();
            instance.getObject(key).activeDomain().put(idWithValues.id(), innerMap);
        }
        
        innerMap.put(idWithValues, activeDomainRow);
        
        return activeDomainRow;
    }
    
    public ActiveMappingRow morphism(Signature signature, ActiveDomainRow domainRow, ActiveDomainRow codomainRow)
    {
        var row = new ActiveMappingRow(domainRow, codomainRow);
        instance.getMorphism(signature).addMapping(row);
        
        var dualRow = new ActiveMappingRow(codomainRow, domainRow);
        instance.getMorphism(signature.dual()).addMapping(dualRow);
        
        return row;
    }
    
}
