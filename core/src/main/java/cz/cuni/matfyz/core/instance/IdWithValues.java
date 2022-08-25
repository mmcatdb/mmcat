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
    private final Map<Signature, String> tuples;

    public boolean hasSignature(Signature signature) {
        return tuples.containsKey(signature);
    }
    
    public Set<Signature> signatures() {
        return tuples.keySet();
    }

    public String getValue(Signature signature) {
        return tuples.get(signature);
    }
    
    private Id id;
    
    public Id id() {
        if (id == null)
            //id = isTechnical ? Id.Technical() : new Id(tuples.keySet());
            id = new Id(tuples.keySet());
        return id;
        // Evolution extension
        //return new Id(map.keySet());
    }
    
    public Collection<String> values() {
        return tuples.values();
    }
    
    public Map<Signature, String> map() {
        return tuples;
    }

    public int size() {
        return tuples.size();
    }

    // Evolution extension
    public IdWithValues copy() {
        var mapCopy = new TreeMap<Signature, String>();
        this.tuples.forEach((signature, string) -> mapCopy.put(signature, string));
        return new IdWithValues(mapCopy);
    }

    public static IdWithValues merge(IdWithValues... ids) {
        var builder = new Builder();
        for (var id : ids)
            builder.add(id);
        
        return builder.build();
    }

    public static IdWithValues Empty() {
        return merge();
    }

    private IdWithValues(Map<Signature, String> map) {
		this.tuples = map;
        //this.technicalValue = 0;
        //this.isTechnical = false;
	}

    /*
    private IdWithValues(int technicalValue) {
		this.tuples = new TreeMap<>();
        this.technicalValue = technicalValue;
        this.isTechnical = true;
	}
    */

    public static class Builder {

        private Map<Signature, String> map = new TreeMap<>();

        public Builder add(Signature signature, String value) {
            map.put(signature, value);
            return this;
        }

        public Builder add(IdWithValues idWithValues) {
            for (var tuple : idWithValues.tuples.entrySet())
                map.put(tuple.getKey(), tuple.getValue());
                
            return this;
        }

        public IdWithValues build() {
            // if (map.size() == 0)
            //    return IdWithValues.TechnicalId();

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

        // if (isTechnical)
        //    return idWithValues.isTechnical && technicalValue == idWithValues.technicalValue;

        // if (idWithValues.isTechnical)
        //    return false;

        return Objects.equals(this.tuples, idWithValues.tuples);
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.tuples);
        return hash;
    }
    
    @Override
    public int compareTo(IdWithValues idWithValues) {
        // int technicalCompare = technicalValue - idWithValues.technicalValue;
        // if (isTechnical || idWithValues.isTechnical)
        //    return technicalCompare;

        int idCompareResult = id().compareTo(idWithValues.id());
        if (idCompareResult != 0)
            return idCompareResult;
        
        for (Signature signature : signatures()) {
            int signatureCompareResult = tuples.get(signature).compareTo(idWithValues.tuples.get(signature));
            if (signatureCompareResult != 0)
                return signatureCompareResult;
        }
        
        return 0;
    }
    
    @Override
	public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("{");
        boolean notFirst = false;
        for (Signature signature : tuples.keySet()) {
            if (notFirst)
                builder.append(", ");
            else
                notFirst = true;
            
            builder.append("(").append(signature).append(": \"").append(tuples.get(signature)).append("\")");
        }
        builder.append("}");
            
        return builder.toString();
	}
}
