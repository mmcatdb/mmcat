/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractInstance {

	public abstract AbstractCategoricalObject getOrCreate(String name, AbstractType type);

	public abstract AbstractCategoricalObject get(String name);

	public abstract boolean create(String name, AbstractType type);

	public abstract AbstractCategoricalMorphism getOrCreateMorphism(String name, AbstractCategoricalObject domain, AbstractCategoricalObject codomain);

	public abstract AbstractCategoricalMorphism getMorphism(String name);

	public abstract void createMorphism(String name, AbstractCategoricalObject domain, AbstractCategoricalObject codomain);

}
