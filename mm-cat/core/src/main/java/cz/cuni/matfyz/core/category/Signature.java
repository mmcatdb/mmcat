package cz.cuni.matfyz.core.category;

import cz.cuni.matfyz.core.utils.ArrayUtils;
import cz.cuni.matfyz.core.mapping.IContext;

import java.util.Arrays;

/**
 * This class represents a signature of a morphism. It can be empty, base or composite.
 * @author jachym.bartik
 */
public class Signature implements Comparable<Signature>, IContext
{
	private final int[] ids;
    
    public Signature(int... ids)
    {
        this.ids = ids;
	}

	public Signature(Signature signature)
    {
		this.ids = signature.ids;
	}
    
    public static Signature Empty()
    {
        return new Signature();
    }
    
    public static Signature combine(Signature a, Signature b)
    {
        return new Signature(ArrayUtils.concatenate(a.ids, b.ids));
    }

	public Signature dual()
    {
        assert ids.length > 0 : "Trying to find a dual signature to an empty morphism signature.";
        
        int n = ids.length;
        int[] array = new int[n];
        for (int i = 0; i < n; i++)
            array[i] = - ids[n - i - 1];
        
        return new Signature(array);
	}
    
    public enum Type
    {
        EMPTY,
        BASE,
        COMPOSITE
    }
    
    public Type getType()
    {
        return ids.length == 0 ? Type.EMPTY : ids.length == 1 ? Type.BASE : Type.COMPOSITE;
    }

    @Override
	public String toString()
    {
		return String.join(".", Arrays.toString(ids));
	}
    
    public boolean equals(Signature signature)
    {
        return compareTo(signature) == 0;
    }
    
    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Signature signature ? equals(signature) : false;
    }

    /**
     * Auto-generated, constants doesn't have any special meaning.
     * @return 
     */
    @Override
    public int hashCode()
    {
        int hash = 7;
        hash = 83 * hash + Arrays.hashCode(this.ids);
        return hash;
    }
    
    @Override
    public int compareTo(Signature signature) {
        final int lengthDifference = ids.length - signature.ids.length;
        
        return lengthDifference != 0 ? lengthDifference : compareIdsWithSameLength(signature.ids);
    }
    
    private int compareIdsWithSameLength(int[] anotherIds)
    {
        for (int i = 0; i < ids.length; i++)
        {
            final int idDifference = ids[i] - anotherIds[i];
            if (idDifference != 0)
                return idDifference;
        }
        return 0;
    }
    
    public Boolean hasDualOfAsSuffix(Signature signature)
    {
        if (signature == null)
            return false;
        
        Signature dual = signature.dual();
        int dualLength = dual.ids.length;
        
        if (ids.length < dualLength)
            return false;
        
        for (int i = 0; i < dualLength; i++)
            if (dual.ids[i] != ids[i + ids.length - dualLength])
                return false;
        
        return true;
    }
    
    public Signature traverseThrough(Signature signature)
    {
        if (!hasDualOfAsSuffix(signature))
            return null;
        
        return new Signature(Arrays.copyOfRange(ids, 0, ids.length - signature.ids.length));
    }
}
