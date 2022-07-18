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
            id = isTechnical ? Id.Technical() : new Id(map.keySet());
        return id;
        // Evolution extension
        //return new Id(map.keySet());
    }
    
    public Collection<String> values()
    {
        return map.values();
    }
    
    public Map<Signature, String> map()
    {
        return map;
    }

    // The point of a technical id is to differentiate two idWithValues from each other, but only if they do not share any other id.
    private final int technicalValue;
    public final boolean isTechnical;

    public static IdWithValues Technical(int value) {
        return new IdWithValues(value);
    }

    // Evolution extension
    public IdWithValues copy() {
        var mapCopy = new TreeMap<Signature, String>();
        this.map.forEach((signature, string) -> mapCopy.put(signature, string));
        return new IdWithValues(mapCopy);
    }

    private IdWithValues(Map<Signature, String> map) {
		this.map = map;
        this.technicalValue = 0;
        this.isTechnical = false;
	}

    private IdWithValues(int technicalValue) {
		this.map = new TreeMap<>();
        this.technicalValue = technicalValue;
        this.isTechnical = true;
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
        if (!(object instanceof IdWithValues))
            return false;

        var idWithValues = (IdWithValues) object;

        if (isTechnical)
            return idWithValues.isTechnical && technicalValue == idWithValues.technicalValue;

        if (idWithValues.isTechnical)
            return false;

        return Objects.equals(this.map, idWithValues.map);
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.map);
        return hash;
    }
    
    @Override
    public int compareTo(IdWithValues idWithValues)
    {
        int technicalCompare = technicalValue - idWithValues.technicalValue;
        if (isTechnical || idWithValues.isTechnical)
            return technicalCompare;

        int idCompareResult = id().compareTo(idWithValues.id());
        if (idCompareResult != 0)
            return idCompareResult;
        
        for (Signature signature : signatures())
        {
            int signatureCompareResult = map.get(signature).compareTo(idWithValues.map.get(signature));
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
        if (isTechnical) {
            builder.append("(TECHNICAL: ").append(technicalValue).append(")");
        }
        else {
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
        }
            
        return builder.toString();
	}
}
