/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

import java.util.Map;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractRecord extends AbstractValue/*, Map<String, AbstractValue>*/ {

	public abstract Iterable<String> getPropertyNames();

	public abstract AbstractValue getProperty(String name);	// tohle je lepsi resit na urovni AbstractRecord

	public abstract Iterable<AbstractIdentifier> getIdentifiers();	// na urovni AbstractRecord

	public abstract Iterable<AbstractReference> getReferences();	// na urovni AbstractRecord

	public abstract boolean isIdentifierCompound(String name);	// na urovni AbstractRecord

	public abstract boolean isReferenceCompound(String name);	// na urovni AbstractRecord

	public abstract boolean isNullable(String name);	// na urovni AbstractRecord

	public abstract AbstractType getPropertyType(String name);	// na urovni AbstractRecord

	public abstract Iterable<AbstractValue> getProperties();

	public abstract void putProperty(String name, AbstractValue value);	// nemelo by tohle byt spis typu abstractValue?

}
