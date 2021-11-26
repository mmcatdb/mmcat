package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Signature;

import java.util.*;

/**
 * Id is a set of signatures (each corresponding to a base or a composite morphism).
 * @author jachymb.bartik
 */
public class Id implements Comparable<Id>
{
    private final SortedSet<Signature> signatures;
    
    public Iterable<Signature> signatures()
    {
        return signatures;
    }

	public Id(Set<Signature> signatures)
    {
		this.signatures = new TreeSet<>(signatures);
	}
    
    public Id(Collection<Signature> signatures)
    {
		this.signatures = new TreeSet<>(signatures);
	}

	public Id(Signature... signatures)
    {
		this.signatures = new TreeSet<>(List.of(signatures));
	}

	@Override
	public int compareTo(Id id)
    {
        int sizeResult = signatures.size() - id.signatures.size();
        if (sizeResult != 0)
            return sizeResult;
        
        Iterator<Signature> iterator = id.signatures.iterator();
        
        for (Signature signature : signatures)
        {
            int signatureResult = signature.compareTo(iterator.next());
            if (signatureResult != 0)
                return signatureResult;
        }
        
        return 0;
	}
}
