package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.util.*;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An instance of this class represents a tuple from the {@link InstanceObject}.
 * The tuple is made of pairs (signature, value) for each signature in the superid. This structure is implemented by a map.
 * Each value is unique among all the values associated with the same signature.
 * @author jachym.bartik
 */
public class DomainRow implements Comparable<DomainRow>, JSONConvertible {

    public final InstanceObject instanceObject;
    // The tuples that holds the value of this row.
    public final IdWithValues superId;
    // All technical ids under which is this row known.
    public final Set<Integer> technicalIds;

    public final Set<Signature> unnotifiedSignatures;

    // Various ids that can be constructed from this row.
    //private final Map<Id, IdWithValues> ids;
    
    // Evolution extension
    /*
    public void addValue(Signature signature, String value) {
        idWithValues.map().put(signature, value);
        tuples.put(signature, value);
    }
    */
 // TODO
    public DomainRow(IdWithValues superId, InstanceObject instanceObject) {
        this.instanceObject = instanceObject;
        this.superId = superId;
        this.technicalIds = new TreeSet<>();

        // TODO this should happen only when there are no id, not when superId is empty
        //if (superId.size() == 0)
        if (instanceObject.findIdsInSuperId(superId, instanceObject.schemaObject().ids()).getValue0().size() == 0)
            this.technicalIds.add(instanceObject.generateTechnicalId());

        this.unnotifiedSignatures = new TreeSet<>(instanceObject.schemaObject().superId().signatures());
    }
 // TODO
    public DomainRow(IdWithValues superId, Set<Integer> technicalIds, InstanceObject instanceObject) {
        this.instanceObject = instanceObject;
        this.superId = superId;
        this.technicalIds = technicalIds;

        //if (superId.size() == 0 && technicalIds.size() == 0)
        if (instanceObject.findIdsInSuperId(superId, instanceObject.schemaObject().ids()).getValue0().size() == 0)
            this.technicalIds.add(instanceObject.generateTechnicalId());

        this.unnotifiedSignatures = new TreeSet<>(instanceObject.schemaObject().superId().signatures());
    }
 // TODO
    public DomainRow(Integer technicalId, InstanceObject instanceObject) {
        this.instanceObject = instanceObject;
        this.superId = IdWithValues.Empty();
        this.technicalIds = new TreeSet<>(List.of(technicalId));
        this.unnotifiedSignatures = new TreeSet<>(instanceObject.schemaObject().superId().signatures());
    }

    public boolean hasSignature(Signature signature) {
        return superId.hasSignature(signature);
    }

    public Set<Signature> signatures() {
        return superId.signatures();
    }

    public String getValue(Signature signature) {
        return superId.getValue(signature);
    }

    private final Map<InstanceMorphism, Set<MappingRow>> mappingsFrom = new TreeMap<>();
    //public final Map<InstanceMorphism, Set<MappingRow>> mappingsTo = new TreeMap<>();

    public Set<MappingRow> getMappingsFromForMorphism(InstanceMorphism morphism) {
        var mappings = mappingsFrom.get(morphism);
        return mappings != null ? mappings : new TreeSet<>();
    }

    public Set<Entry<InstanceMorphism, Set<MappingRow>>> getAllMappingsFrom() {
        return mappingsFrom.entrySet();
    }

    public void addMappingFrom(InstanceMorphism morphism, MappingRow mapping) {
        //addMapping(mappingsFrom, morphism, mapping);
        var mappingsOfSameType = mappingsFrom.get(morphism);
        if (mappingsOfSameType == null) {
            mappingsOfSameType = new TreeSet<>();
            mappingsFrom.put(morphism, mappingsOfSameType);
        }

        mappingsOfSameType.add(mapping);
    }

    /*
    public void addMappingTo(InstanceMorphism morphism, MappingRow mapping) {
        addMapping(mappingsTo, morphism, mapping);
    }

    private void addMapping(Map<InstanceMorphism, Set<MappingRow>> map, InstanceMorphism morphism, MappingRow mapping) {
        var mappingsOfSameType = map.get(morphism);
        if (mappingsOfSameType == null) {
            mappingsOfSameType = new TreeSet<>();
            map.put(morphism, mappingsOfSameType);
        }

        mappingsOfSameType.add(mapping);
    }
     */


    public void removeMappingFrom(InstanceMorphism morphism, MappingRow mapping) {
        var mappingsOfSameType = mappingsFrom.get(morphism);
        mappingsOfSameType.remove(mapping);
    }

    public Set<DomainRow> traverseThrough(List<InstanceMorphism> path) {
        var rowSet = new TreeSet<DomainRow>();
        rowSet.add(this);

        for (var baseMorphism : path) {
            var nextSet = new TreeSet<DomainRow>();
            for (var row : rowSet) {
                var codomainRows = row.getMappingsFromForMorphism(baseMorphism).stream().map(mapping -> mapping.codomainRow()).toList();
                nextSet.addAll(codomainRows);
            }
            rowSet = nextSet;
        }

        return rowSet;
    }

    @Override
    public int compareTo(DomainRow row) {
        // TODO technical ids?
        return superId.compareTo(row.superId);
    }
    
    @Override
    public String toString() {
        var builder = new StringBuilder();
        builder.append(superId.toString());
        if (technicalIds.size() > 0) {
            builder.append("[");
            var notFirst = false;
            for (var technicalId : technicalIds) {
                if (notFirst)
                    builder.append(", ");
                notFirst = true;
                builder.append(technicalId);
            }
            builder.append("]");
        }

        return builder.toString();
    }
    
    @Override
    public boolean equals(Object object) {
        return object instanceof DomainRow row && superId.equals(row.superId);
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<DomainRow> {

        @Override
        protected JSONObject _toJSON(DomainRow object) throws JSONException {
            var output = new JSONObject();

            var map = object.superId.map();
            var tuples = new ArrayList<JSONObject>();
            
            for (Signature signature : map.keySet()) {
                var jsonTuple = new JSONObject();
                jsonTuple.put("signature", signature.toJSON());
                jsonTuple.put("value", map.get(signature));

                tuples.add(jsonTuple);
            }

            output.put("tuples", new JSONArray(tuples));
            
            return output;
        }
    
    }
}
