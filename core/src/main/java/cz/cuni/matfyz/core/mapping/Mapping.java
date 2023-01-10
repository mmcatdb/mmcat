package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.serialization.FromJSONLoaderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.util.ArrayList;
import java.util.Collection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author pavel.koupil, jachym.bartik
 */
public class Mapping implements JSONConvertible, Comparable<Mapping> {

    private final SchemaCategory category;
    private final SchemaObject rootObject;
    private final SchemaMorphism rootMorphism;
    
    private ComplexProperty accessPath;
    private String kindName;
    private Collection<Signature> primaryKey;
    
    private Mapping(SchemaCategory category, SchemaObject rootObject, SchemaMorphism rootMorphism) {
        this.category = category;
        this.rootObject = rootObject;
        this.rootMorphism = rootMorphism;
    }

    public SchemaCategory category() {
        return category;
    }
    
    public boolean hasRootMorphism() {
        return rootMorphism != null;
    }

    public SchemaObject rootObject() {
        return rootObject;
    }
    
    public SchemaMorphism rootMorphism() {
        return rootMorphism;
    }
    
    public ComplexProperty accessPath() {
        return accessPath;
    }

    public String kindName() {
        return kindName;
    }

    public Collection<Signature> primaryKey() {
        return primaryKey;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other)
            return true;
        
        return other instanceof Mapping otherMapping && compareTo(otherMapping) == 0;
    }

    @Override
    public int compareTo(Mapping other) {
        // This guarantees uniqueness in one logical model, however mappings between different logical models are never compared.
        return kindName.compareTo(other.kindName);
    }

    /*
    private final List<Reference> references = new ArrayList<Reference>();

    public List<Reference> references() {
        return references;
    }

    public void setReferences(Iterable<Reference> references) {
        this.references.clear();
        references.forEach(this.references::add);
    }
    */

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<Mapping> {

        @Override
        protected JSONObject innerToJSON(Mapping object) throws JSONException {
            var output = new JSONObject();
    
            output.put("kindName", object.kindName);
            var primaryKey = new JSONArray(object.primaryKey.stream().map(Signature::toJSON).toList());
            output.put("primaryKey", primaryKey);
            output.put("accessPath", object.accessPath.toJSON());

            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONLoaderBase<Mapping> {
    
        public Mapping fromJSON(SchemaCategory category, SchemaObject rootObject, SchemaMorphism rootMorphism, JSONObject jsonObject) {
            var mapping = new Mapping(category, rootObject, rootMorphism);
            loadFromJSON(mapping, jsonObject);
            return mapping;
        }

        public Mapping fromJSON(SchemaCategory category, SchemaObject rootObject, SchemaMorphism rootMorphism, String jsonValue) {
            var mapping = new Mapping(category, rootObject, rootMorphism);
            loadFromJSON(mapping, jsonValue);
            return mapping;
        }

        @Override
        protected void innerLoadFromJSON(Mapping mapping, JSONObject jsonObject) throws JSONException {
            mapping.kindName = jsonObject.getString("kindName");
            
            var primaryKeyArray = jsonObject.getJSONArray("primaryKey");
            var primaryKey = new ArrayList<Signature>();
            var builder = new Signature.Builder();
            for (int i = 0; i < primaryKeyArray.length(); i++)
                primaryKey.add(builder.fromJSON(primaryKeyArray.getJSONObject(i)));

            mapping.primaryKey = primaryKey;

            mapping.accessPath = new ComplexProperty.Builder().fromJSON(jsonObject.getJSONObject("accessPath"));
        }

        public Mapping fromArguments(SchemaCategory category, SchemaObject rootObject, SchemaMorphism rootMorphism, ComplexProperty accessPath, String kindName, Collection<Signature> primaryKey) {
            var mapping = new Mapping(category, rootObject, rootMorphism);
            mapping.accessPath = accessPath;
            mapping.kindName = kindName;
            mapping.primaryKey = primaryKey;
            return mapping;
        }
    
    }

}
