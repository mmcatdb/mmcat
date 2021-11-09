package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.Id;
import cz.cuni.matfyz.core.utils.IterableUtils;

import java.util.*;

/**
 * An instance of this class represents a tuple from the {@link InstanceObject}.
 * The tuple is made of pairs (signature, value) for each signature in the superid. This structure is implemented by a map.
 * Each value is unique among all the values associated with the same signature.
 * @author jachym.bartik
 */
public class ActiveDomainRow
{
	private final Map<Signature, String> tuples = new TreeMap<>();
    
    public Id superId()
    {
        return new Id(tuples.keySet());
    }
    
    public Map<Signature, String> tuples()
    {
        return tuples;
    }
    
    public ActiveDomainRow(Id superId, String ...values)
    {
        this(superId, Arrays.asList(values));
    }
    
    public ActiveDomainRow(Id superId, Iterable<String> values)
    {
        boolean haveSameLength = IterableUtils.iterateTwo(superId.signatures(), values, (signature, value) -> tuples.put(signature, value));
        
        assert haveSameLength : "Active domain row input superId and values lengths are not equal.";
    }
}
