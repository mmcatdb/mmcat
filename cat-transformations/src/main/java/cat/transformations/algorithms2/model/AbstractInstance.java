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

	public abstract AbstractObject getOrCreate(String name, AbstractType type);

	public abstract AbstractObject get(String name);

	public abstract boolean create(String name, AbstractType type);

	public abstract AbstractMorphism getMorphism(String name);

}
