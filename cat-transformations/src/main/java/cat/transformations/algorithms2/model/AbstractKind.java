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
public interface AbstractKind {

	public abstract String getName();

	public abstract Iterable<String> getPropertyNames();	// ke zvazeni, jestli tohle potrebujeme tady...

	public abstract Iterable<AbstractRecordProperty> getRecords();

	public abstract AbstractRecordProperty getRecord(int index);

	public abstract AbstractRecordProperty getRecord(AbstractIdentifier identifier);

	public abstract int size();
	
	public abstract void add(AbstractRecordProperty record);

}
