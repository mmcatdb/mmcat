package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.Id;

import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class IdWithValues implements Comparable<IdWithValues>
{
    private final Map<Signature, String> map;
    
    public Set<Signature> signatures()
    {
        return map.keySet();
    }
    
    private Id id;
    public Id id()
    {
        if (id == null)
            id = new Id(map.keySet());
        return id;
    }
    
    public Collection<String> values()
    {
        return map.values();
    }
    
    public Map<Signature, String> map()
    {
        return map;
    }

    private IdWithValues(Map<Signature, String> map)
    {
		this.map = map;
	}
    
    public static class Builder
    {
        private Map<Signature, String> map = new TreeMap<>();

        public Builder add(Signature signature, String value)
        {
            map.put(signature, value);
            return this;
        }

        public IdWithValues build()
        {
            var output = new IdWithValues(map);
            map = new TreeMap<>();
            return output;
        }
    }
    
    @Override
    public boolean equals(Object object) {
        return object instanceof IdWithValues idWithValues && Objects.equals(this.map, idWithValues.map);
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.map);
        return hash;
    }
    
    @Override
    public int compareTo(IdWithValues sid)
    {
        int idCompareResult = id().compareTo(sid.id());
        if (idCompareResult != 0)
            return idCompareResult;
        
        for (Signature signature : signatures())
        {
            int signatureCompareResult = map.get(signature).compareTo(sid.map.get(signature));
            if (signatureCompareResult != 0)
                return signatureCompareResult;
        }
        
        return 0;
    }
    
    @Override
	public String toString()
    {
        StringBuilder builder = new StringBuilder();
        
        builder.append("{");
        boolean notFirst = false;
        for (Signature signature : map.keySet())
        {
            if (notFirst)
                builder.append(", ");
            else
                notFirst = true;
            
            builder.append("(").append(signature).append(": \"").append(map.get(signature)).append("\")");
        }
        builder.append("}");
            
        return builder.toString();
	}
}
