/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.CategoricalObject;
import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;

/**
 *
 * @author pavel.koupil
 */
public class SchemaMorphism implements Morphism {

	private final Signature signature;
	private final SchemaObject dom;
	private final SchemaObject cod;
	private final int min;
	private final int max;

	private SchemaCategory category;

	public static SchemaMorphism dual(SchemaMorphism morphism) {
		return SchemaMorphism.dual(morphism, 1, 1);
	}

	public static SchemaMorphism dual(SchemaMorphism morphism, int min, int max) {
		SchemaMorphism result = new SchemaMorphism(morphism.signature().dual(), morphism.cod(), morphism.dom(), min, max);
		return result;
	}

	public SchemaMorphism(Signature signature, SchemaObject dom, SchemaObject cod, int min, int max) {
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

	public int min() {
		return min;
	}

	public int max() {
		return max;
	}

	@Override
	public SchemaMorphism dual() {
		return category.dual(signature.dual());
	}

	@Override
	public Signature signature() {
		return signature;
	}

}
