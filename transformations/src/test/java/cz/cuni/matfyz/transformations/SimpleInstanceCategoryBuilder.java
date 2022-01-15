package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.category.*;

import java.util.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class SimpleInstanceCategoryBuilder
{
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(SimpleInstanceCategoryBuilder.class);
	
    private final InstanceCategory instance;
    
    public SimpleInstanceCategoryBuilder(InstanceCategory instance)
    {
        this.instance = instance;
    }
    
    private final IdWithValues.Builder builder = new IdWithValues.Builder();
    
    public SimpleInstanceCategoryBuilder value(Signature signature, String value)
    {
        builder.add(signature, value);
        
        return this;
    }
    
    public ActiveDomainRow object(Key key)
    {
        IdWithValues idWithValues = builder.build();
        var activeDomainRow = new ActiveDomainRow(idWithValues);
        
        Map<IdWithValues, ActiveDomainRow> innerMap = instance.object(key).activeDomain().get(idWithValues.id());
        if (innerMap == null)
        {
            innerMap = new TreeMap<>();
            instance.object(key).activeDomain().put(idWithValues.id(), innerMap);
        }
        
        innerMap.put(idWithValues, activeDomainRow);
        
        return activeDomainRow;
    }
    
    public ActiveMappingRow morphism(Signature signature, ActiveDomainRow domainRow, ActiveDomainRow codomainRow)
    {
        var row = new ActiveMappingRow(domainRow, codomainRow);
        instance.morphism(signature).addMapping(row);
        
        var dualRow = new ActiveMappingRow(codomainRow, domainRow);
        instance.morphism(signature.dual()).addMapping(dualRow);
        
        return row;
    }
    
}
