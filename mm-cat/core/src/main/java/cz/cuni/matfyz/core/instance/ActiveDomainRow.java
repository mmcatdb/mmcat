package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.Id;

import java.util.*;

/**
 * An instance of this class represents a tuple from the {@link InstanceObject}.
 * The tuple is made of pairs (signature, value) for each signature in the superid. This structure is implemented by a map.
 * Each value is unique among all the values associated with the same signature. (TODO maybe not)
 * @author jachym.bartik
 */
public class ActiveDomainRow
{
    //private final Id superId;
    
    private final SuperIdWithValues superIdWithValues;
	private final Map<Signature, String> tuples;
    
    /*
    public Id superId()
    {
        return superId;
    }
    */
    
    public SuperIdWithValues superIdWithValues()
    {
        return superIdWithValues;
    }
    
    public Map<Signature, String> tuples()
    {
        return tuples;
    }
    
    public ActiveDomainRow(SuperIdWithValues superIdWithValues)
    {
        this.superIdWithValues = superIdWithValues;
        this.tuples = new TreeMap<>(superIdWithValues.map());
    }
}
