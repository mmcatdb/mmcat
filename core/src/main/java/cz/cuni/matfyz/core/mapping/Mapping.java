package cz.cuni.matfyz.core.mapping;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;

import java.util.*;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author pavel.koupil, jachym.bartik
 */
public class Mapping implements JSONConvertible
{
	//private final SchemaObject rootObject;
    private SchemaObject rootObject;
	private final SchemaMorphism rootMorphism;
	private final ComplexProperty accessPath;

    private String kindName;
    private Collection<Signature> pkey;
    
    
    public Mapping(SchemaObject rootObject, ComplexProperty accessPath)
    {
        this(rootObject, null, accessPath);
    }
    
    public Mapping(SchemaObject rootObject, SchemaMorphism rootMorphism, ComplexProperty accessPath)
    {
        this.rootObject = rootObject;
        this.rootMorphism = rootMorphism;
        this.accessPath = accessPath;
    }
    
    public boolean hasRootMorphism()
    {
        return rootMorphism != null;
    }

    public void setRootObject(SchemaObject object) // TODO remove later
    {
        this.rootObject = object;
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
    
            // TODO root object and morphism
            output.put("kindName", object.kindName);
            output.put("accessPath", object.accessPath.toJSON());
            
            return output;
        }
    
    }
    
    public static class Builder extends FromJSONBuilderBase<Mapping> {
    
        @Override
        protected Mapping _fromJSON(JSONObject jsonObject) throws JSONException {
            String kindName = jsonObject.getString("kindName");
            ComplexProperty accessPath = new ComplexProperty.Builder().fromJSON(jsonObject.getJSONObject("accessPath"));

            return new Mapping(null, accessPath);
        }
    
    }
}
