/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class InstanceMorphism implements Morphism {

	private final Signature signature;
	private final InstanceObject dom;
	private final InstanceObject cod;

	private final List<ActiveMappingRow> activeDomain;

	private InstanceCategory category;

	public void addMapping(ActiveMappingRow mapping) {
		activeDomain.add(mapping);
	}

	public InstanceMorphism(Signature signature, InstanceObject dom, InstanceObject cod) {
		this.signature = signature;
		this.dom = dom;
		this.cod = cod;

		activeDomain = new ArrayList<>();
	}

	public InstanceMorphism(SchemaMorphism morphism, InstanceCategory category) {
		signature = new Signature(morphism.signature());
		this.category = category;

		dom = this.category.object(morphism.dom().key());
		cod = this.category.object(morphism.cod().key());

		activeDomain = new ArrayList<>();

	}

	public void setCategory(InstanceCategory category) {
		this.category = category;
	}

	@Override
	public InstanceObject dom() {
		return dom;
	}

	@Override
	public InstanceObject cod() {
		return cod;
	}

	@Override
	public Morphism dual()
    {
		return category.dual(signature);
	}

	@Override
	public Signature signature()
    {
		return signature;
	}

}
