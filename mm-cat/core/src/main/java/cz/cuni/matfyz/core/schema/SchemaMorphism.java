package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;

/**
 *
 * @author pavel.koupil
 */
public class SchemaMorphism implements Morphism
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
	public static SchemaMorphism dual(SchemaMorphism morphism) {
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

	public void setCategory(SchemaCategory category) {
		this.category = category;
	}

	@Override
	public SchemaObject dom() {
		return dom;
	}

	@Override
	public SchemaObject cod() {
		return cod;
	}

	public Min min() {
		return min;
	}

	public Max max() {
		return max;
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

}
