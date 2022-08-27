package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.Identified;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author pavel.koupil, jachymb.bartik
 */
public class SchemaObject implements Serializable, CategoricalObject, JSONConvertible, Identified<Key> {
    //private static final Logger LOGGER = LoggerFactory.getLogger(SchemaObject.class);
    
    private final Key key; // Identifies the object, in the paper it's a number >= 100
    private final String label;
    private final Id superId; // Should be a union of all ids (super key).
    private final Set<Id> ids; // Each id is a set of signatures so that the correspondig set of attributes can unambiguosly identify this object (candidate key).

    public SchemaObject(Key key, String label, Id superId, Set<Id> ids) {
        this.key = key;
        this.label = label;
        this.superId = superId;
        this.ids = Set.of(ids.toArray(Id[]::new));
    }

    @Override
    public Key identifier() {
        return key;
    }

    @Override
    public Key key() {
        return key;
    }

    @Override
    public String label() {
        return label;
    }

    @Override
    public Id superId() {
        return superId;
    }

    /**
     * Immutable.
     */
    @Override
    public Set<Id> ids() {
        return new TreeSet<>(ids);
    }

    @Override
    public int compareTo(CategoricalObject categoricalObject) {
        return key.compareTo(categoricalObject.key());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SchemaObject schemaObject && key.equals(schemaObject.key);
    }

    /**
     * Auto-generated, constants doesn't have any special meaning.
     * @return 
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 79 * hash + Objects.hashCode(this.key);
        return hash;
    }

    @Override
    public String toString() {
        return "SchemaObject TODO";
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<SchemaObject> {

        @Override
        protected JSONObject innerToJSON(SchemaObject object) throws JSONException {
            var output = new JSONObject();

            output.put("key", object.key.toJSON());
            output.put("label", object.label);
            output.put("superId", object.superId.toJSON());

            var ids = new JSONArray(object.ids.stream().map(id -> id.toJSON()).toList());
            output.put("ids", ids);
            
            return output;
        }

    }

    public static class Builder extends FromJSONBuilderBase<SchemaObject> {

        @Override
        protected SchemaObject innerFromJSON(JSONObject jsonObject) throws JSONException {
            var key = new Key.Builder().fromJSON(jsonObject.getJSONObject("key"));
            var label = jsonObject.getString("label");
            var superId = new Id.Builder().fromJSON(jsonObject.getJSONObject("superId"));

            var idsArray = jsonObject.getJSONArray("ids");
            var ids = new TreeSet<Id>();
            var builder = new Id.Builder();
            for (int i = 0; i < idsArray.length(); i++)
                ids.add(builder.fromJSON(idsArray.getJSONObject(i)));

            return new SchemaObject(key, label, superId, ids);
        }

    }

}
