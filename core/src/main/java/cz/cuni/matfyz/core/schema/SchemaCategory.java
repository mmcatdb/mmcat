package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Category;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaMorphism.Max;
import cz.cuni.matfyz.core.schema.SchemaMorphism.Min;
import cz.cuni.matfyz.core.serialization.MapUniqueContext;
import cz.cuni.matfyz.core.serialization.UniqueContext;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author pavel.koupil, jachymb.bartik
 */
public class SchemaCategory implements Category//, JSONConvertible
{
    private static Logger LOGGER = LoggerFactory.getLogger(SchemaCategory.class);

    private final UniqueContext<SchemaObject, Key> objectContext = new MapUniqueContext<>();
    private final UniqueContext<SchemaMorphism, Signature> morphismContext = new MapUniqueContext<>();

    public SchemaCategory()
    {

	}

    public SchemaObject addObject(SchemaObject object)
    {
        return objectContext.createUniqueObject(object);
	}

	public SchemaMorphism addMorphism(SchemaMorphism morphism)
    {
        var newMorphism = morphismContext.createUniqueObject(morphism);
        newMorphism.setCategory(this);
		return newMorphism;
	}

	public SchemaMorphism dual(Signature signatureOfOriginal)
    {
        final SchemaMorphism result = signatureToMorphism(signatureOfOriginal.dual());
        assert result != null : "Schema morphism with signature " + signatureOfOriginal + " doesn't have its dual.";
        return result;
	}

    public SchemaObject keyToObject(Key key)
    {
        return objectContext.getUniqueObject(key);
    }
    
    public SchemaMorphism signatureToMorphism(Signature signature)
    {
        SchemaMorphism morphism = morphismContext.getUniqueObject(signature);
        if (morphism == null)
        {
            SchemaMorphism  newMorphism = createCompositeMorphism(signature);
            morphism = this.morphismContext.createUniqueObject(newMorphism);
        }

        return morphism;
    }

    public Iterable<SchemaObject> allObjects()
    {
        return objectContext.getAllUniqueObjects();
    }

    public Iterable<SchemaMorphism> allMorphisms()
    {
        return morphismContext.getAllUniqueObjects();
    }

    public UniqueContext<SchemaObject, Key> objectContext() // TODO
    {
        return this.objectContext;
    }

    private SchemaMorphism createCompositeMorphism(Signature signature)
    {
        Signature[] bases = signature.toBases().toArray(new Signature[0]);

        Signature lastSignature = bases[bases.length - 1];
        SchemaMorphism lastMorphism = this.signatureToMorphism(lastSignature);
        SchemaObject dom = lastMorphism.dom();
        SchemaObject cod = lastMorphism.cod();
        Min min = lastMorphism.min();
        Max max = lastMorphism.max();

        for (int i = 2; i <= bases.length; i++)
        {
            lastSignature = bases[bases.length - i];
            lastMorphism = this.signatureToMorphism(lastSignature);
            cod = lastMorphism.cod();
            min = SchemaMorphism.combineMin(min, lastMorphism.min());
            max = SchemaMorphism.combineMax(max, lastMorphism.max());
        }

        return new SchemaMorphism.Builder().fromArguments(signature, dom, cod, min, max);
    }

    /*
    @Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

    public static class Converter extends ToJSONConverterBase<SchemaCategory> {

        @Override
        protected JSONObject _toJSON(SchemaCategory object) throws JSONException {
            var output = new JSONObject();

            var objects = new JSONArray(object.objectContext.getAllUniqueObjects().stream().map(schemaObject -> schemaObject.toJSON()).toList());
			output.put("objects", objects);

			var morphisms = new JSONArray(object.morphismContext.getAllUniqueObjects().stream().map(schemaMorphism -> schemaMorphism.toJSON()).toList());
			output.put("morphisms", morphisms);
            
            return output;
        }

	}

	public static class Builder extends FromJSONBuilderBase<SchemaCategory> {

        @Override
        protected SchemaCategory _fromJSON(JSONObject jsonObject) throws JSONException {
            var output = new SchemaCategory();
            
            var objectsArray = jsonObject.getJSONArray("objects");
            var objectBuilder = new SchemaObject.Builder();
            for (int i = 0; i < objectsArray.length(); i++)
                output.addObject(objectBuilder.fromJSON(objectsArray.getJSONObject(i)));

            var morphismsArray = jsonObject.getJSONArray("morphisms");
            var morphismBuilder = new SchemaMorphism.Builder(output.objectContext);
            for (int i = 0; i < morphismsArray.length(); i++)
                output.addMorphism(morphismBuilder.fromJSON(morphismsArray.getJSONObject(i)));
            
            return output;
        }

    }
    */
}
