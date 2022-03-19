package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.FromJSONLoaderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.util.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class Mapping implements JSONConvertible
{
    private final SchemaCategory category;
	private final SchemaObject rootObject;
	private final SchemaMorphism rootMorphism;
    
	private ComplexProperty accessPath;
    private String kindName;
    private Collection<Signature> pkey;
    
    /*
    public Mapping(SchemaObject rootObject, ComplexProperty accessPath)
    {
        this(rootObject, null, accessPath);
    }
    */
    
    private Mapping(SchemaCategory category, SchemaObject rootObject, SchemaMorphism rootMorphism)//, ComplexProperty accessPath)
    {
        this.category = category;
        this.rootObject = rootObject;
        this.rootMorphism = rootMorphism;
        //this.accessPath = accessPath;
    }

    public SchemaCategory category()
    {
        return category;
    }
    
    public boolean hasRootMorphism()
    {
        return rootMorphism != null;
    }

    public SchemaObject rootObject()
    {
        return rootObject;
    }
    
    public SchemaMorphism rootMorphism()
    {
        return rootMorphism;
    }
    
    public ComplexProperty accessPath()
    {
        return accessPath;
    }

    public String kindName()
    {
        return kindName;
    }

    public Collection<Signature> pkey()
    {
        return pkey;
    }

    private final List<Reference> references = new ArrayList<Reference>();

    public List<Reference> references()
    {
        return references;
    }

    public void setReferences(Iterable<Reference> references)
    {
        this.references.clear();
        references.forEach(reference -> this.references.add(reference));
    }

    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<Mapping> {

        @Override
        protected JSONObject _toJSON(Mapping object) throws JSONException {
            var output = new JSONObject();
    
            output.put("kindName", object.kindName);
            output.put("accessPath", object.accessPath.toJSON());

            var pkey = new JSONArray(object.pkey.stream().map(signature -> signature.toJSON()).toList());
            output.put("pkey", pkey);
            
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
        protected void _loadFromJSON(Mapping mapping, JSONObject jsonObject) throws JSONException {
            mapping.accessPath = new ComplexProperty.Builder().fromJSON(jsonObject.getJSONObject("accessPath"));
            mapping.kindName = jsonObject.getString("kindName");

            var pkeyArray = jsonObject.getJSONArray("pkey");
            var pkey = new ArrayList<Signature>();
            var builder = new Signature.Builder();
            for (int i = 0; i < pkeyArray.length(); i++)
                pkey.add(builder.fromJSON(pkeyArray.getJSONObject(i)));
            mapping.pkey = pkey;
        }

        public Mapping fromArguments(SchemaCategory category, SchemaObject rootObject, SchemaMorphism rootMorphism, ComplexProperty accessPath, String kindName, Collection<Signature> pkey) {
			var mapping = new Mapping(category, rootObject, rootMorphism);
			mapping.accessPath = accessPath;
            mapping.kindName = kindName;
            mapping.pkey = pkey;
			return mapping;
		}
    
    }
}
