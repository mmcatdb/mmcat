package cz.cuni.matfyz.core.schema;

import org.json.JSONException;
import org.json.JSONObject;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.serialization.FromJSONBuilderBase;
import cz.cuni.matfyz.core.serialization.Identified;
import cz.cuni.matfyz.core.serialization.ToJSONConverterBase;
import cz.cuni.matfyz.core.serialization.JSONConvertible;
import cz.cuni.matfyz.core.serialization.UniqueContext;

/**
 *
 * @author pavel.koupil
 */
public class SchemaMorphism implements Morphism, JSONConvertible, Identified<Signature>
{
	private final Signature signature;
	private final SchemaObject dom;
	private final SchemaObject cod;
	private final Min min;
	private final Max max;
    
    public enum Min
    {
        ZERO,
        ONE
    }
    
    public enum Max
    {
        ONE,
        STAR
    }

	private SchemaCategory category;

    /*
	public static SchemaMorphism dual(SchemaMorphism morphism)
	{
		return SchemaMorphism.dual(morphism, 1, 1);
	}
    */

	public SchemaMorphism createDual(Min min, Max max)
    {
		SchemaMorphism result = new SchemaMorphism(signature.dual(), cod, dom, min, max);
		return result;
	}

	public SchemaMorphism(Signature signature, SchemaObject dom, SchemaObject cod, Min min, Max max)
    {
		this.signature = signature;
		this.dom = dom;
		this.cod = cod;
		this.min = min;
		this.max = max;
	}

	public void setCategory(SchemaCategory category)
	{
		this.category = category;
	}

	@Override
	public SchemaObject dom()
	{
		return dom;
	}

	@Override
	public SchemaObject cod()
	{
		return cod;
	}

	public Min min()
	{
		return min;
	}

	public Max max()
	{
		return max;
	}

	public boolean isArray()
	{
		return max == Max.STAR;
	}

	@Override
	public SchemaMorphism dual()
    {
		return category.dual(signature);
	}

	@Override
	public Signature signature()
    {
		return signature;
	}

	@Override
	public Signature identifier() {
		return signature;
	}

	@Override
    public JSONObject toJSON() {
        return new Converter().toJSON(this);
    }

	public static class Converter extends ToJSONConverterBase<SchemaMorphism> {

		@Override
        protected JSONObject _toJSON(SchemaMorphism object) throws JSONException {
            var output = new JSONObject();

			output.put("signature", object.signature.toJSON());
			output.put("domIdentifier", object.dom.identifier().toJSON());
			output.put("codIdentifier", object.cod.identifier().toJSON());
			output.put("min", object.min());
			output.put("max", object.max());

            return output;
        }

	}

	public static class Builder extends FromJSONBuilderBase<SchemaMorphism> {

		private final UniqueContext<SchemaObject, Key> context;

		public Builder(UniqueContext<SchemaObject, Key> context) {
			this.context = context;
		}

        @Override
        protected SchemaMorphism _fromJSON(JSONObject jsonObject) throws JSONException {
            var signature = new Signature.Builder().fromJSON(jsonObject.getJSONObject("signature"));

			var domKey = new Key.Builder().fromJSON(jsonObject.getJSONObject("domIdentifier"));
			SchemaObject dom = context.getUniqueObject(domKey);

			var codKey = new Key.Builder().fromJSON(jsonObject.getJSONObject("codIdentifier"));
			SchemaObject cod = context.getUniqueObject(codKey);

			var min = Min.valueOf(jsonObject.getString("min"));
			var max = Max.valueOf(jsonObject.getString("max"));

            return new SchemaMorphism(signature, dom, cod, min, max);
        }

    }
}
