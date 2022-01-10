package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Signature;

import java.util.*;

/**
 * An instance of this class represents a tuple from the {@link InstanceObject}.
 * The tuple is made of pairs (signature, value) for each signature in the superid. This structure is implemented by a map.
 * Each value is unique among all the values associated with the same signature. (TODO maybe not)
 * @author jachym.bartik
 */
public class ActiveDomainRow implements Comparable<ActiveDomainRow>
{
    //private final Id superId;
    
    private final IdWithValues idWithValues;
	private final Map<Signature, String> tuples;
    
    /*
    public Id superId()
    {
        return superId;
    }
    */
    
    public IdWithValues idWithValues()
    {
        return idWithValues;
    }
    
    public Map<Signature, String> tuples()
    {
        return tuples;
    }
    
    public ActiveDomainRow(IdWithValues idWithValues)
    {
        this.idWithValues = idWithValues;
        this.tuples = new TreeMap<>(idWithValues.map());
    }

    @Override
    public int compareTo(ActiveDomainRow row)
    {
        return idWithValues.compareTo(row.idWithValues());
    }
    
    @Override
    public String toString()
    {
        return idWithValues.toString();
    }
    
    @Override
    public boolean equals(Object object)
    {
        return object instanceof ActiveDomainRow activeDomainRow ? equals(activeDomainRow) : false;
    }
    
    public boolean equals(ActiveDomainRow row)
    {
        if (row == null)
            return false;
        
        return idWithValues.equals(row.idWithValues);
    }
}
