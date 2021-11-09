package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Signature;

import java.util.*;

/**
 * Id is a set of signatures (each corresponding to a base or a composite morphism).
 * @author jachymb.bartik
 */
public class Id implements Comparable<Id>
{
    private final Set<Signature> signatures;
    
    public Iterable<Signature> signatures()
    {
        return signatures;
    }

	public Id(Set<Signature> signatures)
    {
		this.signatures = signatures;
	}

	public Id(Signature... signatures)
    {
		this.signatures = new TreeSet(List.of(signatures));
	}

	@Override
	public int compareTo(Id id)
    {
        throw new UnsupportedOperationException();
	}
}
