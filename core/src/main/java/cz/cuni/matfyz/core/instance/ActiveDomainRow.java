package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * An instance of this class represents a tuple from the {@link InstanceObject}.
 * The tuple is made of pairs (signature, value) for each signature in the superid. This structure is implemented by a map.
 * Each value is unique among all the values associated with the same signature.
 * @author jachym.bartik
 */
public class ActiveDomainRow implements Comparable<ActiveDomainRow>, JSONConvertible
{
    //private final Id superId;
    
    private final IdWithValues idWithValues;
	private final Map<Signature, String> tuples;
    
    /*
    public Id superId()
    {
        return superId;
    }
    */
    
    public IdWithValues idWithValues()
    {
        return idWithValues;
    }

    /*
    public Map<Signature, String> tuples()
    {
        return tuples;
    }
    */

    // Evolution extension
    /*
    public void addValue(Signature signature, String value) {
        idWithValues.map().put(signature, value);
        tuples.put(signature, value);
    }
    */

    public boolean hasSignature(Signature signature)
    {
        return tuples.containsKey(signature);
    }

    public Set<Signature> signatures()
    {
        return tuples.keySet();
    }

    public String getValue(Signature signature)
    {
        return tuples.get(signature);
    }
    
    public ActiveDomainRow(IdWithValues idWithValues)
    {
        this.idWithValues = idWithValues;
        this.tuples = new TreeMap<>(idWithValues.map());
    }

    @Override
    public int compareTo(ActiveDomainRow row)
    {
        return idWithValues.compareTo(row.idWithValues());
    }
    
    @Override
    public String toString()
    {
        return idWithValues.toString();
    }
    
    @Override
    public boolean equals(Object object)
    {
        return object instanceof ActiveDomainRow row && idWithValues.equals(row.idWithValues);
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<ActiveDomainRow> {

        @Override
        protected JSONObject _toJSON(ActiveDomainRow object) throws JSONException {
            var output = new JSONObject();

            var map = object.idWithValues.map();
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
