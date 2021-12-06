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
        assert signatures.size() > 0 : "Empty signature array passed to Id constructor.";
		this.signatures = new TreeSet<>(signatures);
	}
    
    public Id(Collection<Signature> signatures)
    {
        assert signatures.size() > 0 : "Empty signature array passed to Id constructor.";
		this.signatures = new TreeSet<>(signatures);
	}

    // There must be at least one signature
	public Id(Signature... signatures)
    {
        assert signatures.length > 0 : "Empty signature array passed to Id constructor.";
		this.signatures = new TreeSet<>(List.of(signatures));
	}
    
    public static Id Empty()
    {
        return new Id(Signature.Empty());
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
    
    @Override
	public String toString()
    {
		StringBuilder builder = new StringBuilder();

		builder.append("(");
        for (Signature signature : signatures.headSet(signatures.last()))
            builder.append(signature).append(", ");
        builder.append(signatures.last()).append(")");

        return builder.toString();
	}
}
