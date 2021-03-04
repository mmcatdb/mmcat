/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author pavel.koupil
 */
public class DocumentRecord implements AbstractRecord {

	private static final AbstractType TYPE = AbstractType.ENTITY;	// WARN: Nepouzivas DocumentRecord jeste pro nested dokumenty?
	// pokud ano, pak musis tohle presunout do konstruktoru a umoznit nastavit neco jineho... ale pak to nemusi davat smysl

	private final Map<String, AbstractValue> properties = new TreeMap<>();
	private final List<AbstractIdentifier> superid = new ArrayList<>();
	private final List<AbstractReference> references = new ArrayList<>();

	private String name;

	public DocumentRecord() {
		this(null);
	}

	public DocumentRecord(String name) {

		this.name = name;
	}

	@Override
	public AbstractValue getProperty(String name) {
		return properties.get(name);
	}

	@Override
	public Iterable<AbstractIdentifier> getIdentifiers() {
		return superid;
	}

	@Override
	public Iterable<AbstractReference> getReferences() {
		return references;
	}

	@Override
	public boolean isIdentifierCompound(String name) {
		return properties.get(name).isIdentifierCompound();
	}

	@Override
	public boolean isReferenceCompound(String name) {
		return properties.get(name).isReferenceCompound();
	}

	@Override
	public boolean isNullable(String name) {
		return properties.get(name).isNullable();
	}

	@Override
	public AbstractType getPropertyType(String name) {
		return properties.get(name).getType();
	}

	@Override
	public Iterable<AbstractValue> getProperties() {
		return properties.values();
	}

	@Override
	public AbstractType getType() {
		return TYPE;
	}

	@Override
	public Iterable<String> getPropertyNames() {
		return properties.keySet();
	}

	@Override
	public void putProperty(String name, AbstractValue value) {
		properties.put(name, value);
	}

	@Override
	public boolean isIdentifierCompound() {
		return false;
	}

	@Override
	public boolean isReferenceCompound() {
		return false;
	}

	@Override
	public boolean isNullable() {
		return true;
	}

}
