package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.Id;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author jachymb.bartik
 */
public class IdWithValues implements Serializable, Comparable<IdWithValues>, JSONConvertible {

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
    
    private Id cachedId;
    
    public Id id() {
        if (cachedId == null)
            cachedId = new Id(tuples.keySet());
        return cachedId;
        // Evolution extension
        //return new Id(map.keySet());
    }

    public Collection<String> values() {
        return tuples.values();
    }

    public int size() {
        return tuples.size();
    }

    public boolean isEmpty() {
        return tuples.isEmpty();
    }

    public boolean containsId(Id id) {
        for (var signature : id.signatures())
            if (!hasSignature(signature))
                return false;
        return true;
    }

    public IdWithValues findId(Id id) {
        var builder = new Builder();

        for (var signature : id.signatures()) {
            var value = this.tuples.get(signature);
            if (value == null)
                return null;
            builder.add(signature, value);
        }

        return builder.build();
    }

    public IdWithValues findFirstId(Set<Id> ids) {
        for (var id : ids)
            if (containsId(id))
                return findId(id);

        return null;
    }

    public record FindIdsResult(Set<IdWithValues> foundIds, Set<Id> notFoundIds) {}

    /**
     * Returns all ids that are contained there as a subset.
     * @param ids The ids we want to find.
     * @return A set of found ids and also not found ids.
     */
    public FindIdsResult findAllIds(Set<Id> ids) {
        final var foundIds = new TreeSet<IdWithValues>();
        final var notFoundIds = new TreeSet<Id>();

        for (Id id : ids) {
            var foundId = findId(id);

            if (foundId == null)
                notFoundIds.add(id);
            else
                foundIds.add(foundId);
        }

        return new FindIdsResult(foundIds, notFoundIds);
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

    public static IdWithValues createEmpty() {
        return merge();
    }

    private IdWithValues(Map<Signature, String> map) {
        this.tuples = map;
    }

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
        for (var entry : tuples.entrySet()) {
            if (notFirst)
                builder.append(", ");
            else
                notFirst = true;
            
            builder.append("(").append(entry.getKey()).append(": \"").append(entry.getValue()).append("\")");
        }
        builder.append("}");
            
        return builder.toString();
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<IdWithValues> {

        @Override
        protected JSONObject innerToJSON(IdWithValues object) throws JSONException {
            var output = new JSONObject();

            var tuples = new ArrayList<JSONObject>();
            
            for (var entry : object.tuples.entrySet()) {
                var jsonTuple = new JSONObject();
                jsonTuple.put("signature", entry.getKey().toJSON());
                jsonTuple.put("value", entry.getValue());

                tuples.add(jsonTuple);
            }

            output.put("tuples", new JSONArray(tuples));
            
            return output;
        }
    
    }

}
