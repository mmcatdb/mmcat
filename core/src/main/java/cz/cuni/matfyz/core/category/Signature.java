package cz.cuni.matfyz.core.category;

import cz.cuni.matfyz.core.utils.ArrayUtils;
import cz.cuni.matfyz.core.mapping.IContext;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

/**
 * This class represents a signature of a morphism. It can be empty, base or composite.
 * @author jachym.bartik
 */
public class Signature implements Comparable<Signature>, IContext, JSONConvertible
{

	private final int[] ids;
    
    private Signature()
    {
        this.ids = new int[] {};
    }
    
    private Signature(int[] ids)
    {
        //assert ids.length > 0 : "Empty ids array passed to Signature constructor.";
        this.ids = ids.clone();
	}
    
    public Signature(int id)
    {
        this.ids = new int[] { id };
    }

    /*
	public Signature(Signature signature)
    {
		this.ids = signature.ids;
	}
    */
    
    public Signature concatenate(Signature other)
    {
        return new Signature(ArrayUtils.concatenate(other.ids, ids));
	}
    
    public static Signature Empty()
    {
        return new Signature();
    }
    
    public static Signature Null()
    {
        return new Signature(-Integer.MAX_VALUE); // TODO edit
    }
    
    /*
    public static Signature combine(Signature a, Signature b)
    {
        return new Signature(ArrayUtils.concatenate(a.ids, b.ids));
    }
    */

	public Signature dual()
    {
        int n = ids.length;
        if (n == 0)
            return this;
                    
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
        if (getType() == Type.EMPTY)
            return "_EMPTY";
        
        StringBuilder builder = new StringBuilder();
        
        builder.append(ids[0]);
        for (int i = 1; i < ids.length; i++)
            builder.append(".").append(ids[i]);
            
        return builder.toString();
	}

    @Override
    public boolean equals(Object obj)
    {
        return obj instanceof Signature signature && compareTo(signature) == 0;
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
    
    public boolean hasDualOfAsSuffix(Signature signature)
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
        
        int length = ids.length - signature.ids.length;
        return length == 0 ? Signature.Empty() : new Signature(Arrays.copyOfRange(ids, 0, length));
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<Signature> {

        @Override
        protected JSONObject _toJSON(Signature object) throws JSONException {
            var output = new JSONObject();
    
            var ids = new JSONArray(object.ids);
            output.put("ids", ids);
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<Signature> {
    
        @Override
        protected Signature _fromJSON(JSONObject jsonObject) throws JSONException {
            var idsArray = jsonObject.getJSONArray("ids");
            var ids = new int[idsArray.length()];
            for (int i = 0; i < idsArray.length(); i++)
                ids[i] = idsArray.getInt(i);
                
            return new Signature(ids);
        }
    
    }
}
