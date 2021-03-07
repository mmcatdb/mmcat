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
public interface AbstractRecordProperty extends AbstractProperty/*, Map<String, AbstractValue>*/ {

	public abstract Iterable<String> getPropertyNames();

	public abstract AbstractProperty getProperty(String name);	// tohle je lepsi resit na urovni AbstractRecord

	public abstract AbstractIdentifier getIdentifier();	// na urovni AbstractRecord

	public abstract Iterable<AbstractReference> getReferences();	// na urovni AbstractRecord

	public abstract boolean isIdentifierCompound(String name);	// na urovni AbstractRecord

	public abstract boolean isReferenceCompound(String name);	// na urovni AbstractRecord

	public abstract boolean isNullable(String name);	// na urovni AbstractRecord

	public abstract AbstractObjectType getPropertyType(String name);	// na urovni AbstractRecord

	public abstract Iterable<AbstractProperty> getProperties();

	public abstract void putProperty(String name, AbstractProperty value);	// nemelo by tohle byt spis typu abstractValue?

	public void setIdentifier(AbstractIdentifier superid);

}
