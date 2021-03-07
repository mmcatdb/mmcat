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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavel.koupil
 */
public class DocumentRecord implements AbstractRecordProperty {

	private static final Logger LOGGER = Logger.getLogger(DocumentRecord.class.getName());

	private static final AbstractObjectType TYPE = AbstractObjectType.NESTED_KIND;	// WARN: Nepouzivas DocumentRecord jeste pro nested dokumenty?
	// pokud ano, pak musis tohle presunout do konstruktoru a umoznit nastavit neco jineho... ale pak to nemusi davat smysl

	private final Map<String, AbstractProperty> properties = new TreeMap<>();
	private AbstractIdentifier superid;// = new SimpleIdentifier();
	private final List<AbstractReference> references = new ArrayList<>();

	private String name;

	public DocumentRecord() {
		this(null);
	}

	public DocumentRecord(String name) {
		this.name = name;
	}

	@Override
	public AbstractProperty getProperty(String name) {
		return properties.get(name);
	}

	@Override
	public AbstractIdentifier getIdentifier() {
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
	public AbstractObjectType getPropertyType(String name) {
		return properties.get(name).getType();
	}

	@Override
	public Iterable<AbstractProperty> getProperties() {
		return properties.values();
	}

	@Override
	public AbstractObjectType getType() {
		return TYPE;
	}

	@Override
	public Iterable<String> getPropertyNames() {
		return properties.keySet();
	}

	@Override
	public void putProperty(String name, AbstractProperty value) {
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

	@Override
	public AbstractValue getValue() {
		LOGGER.log(Level.WARNING, "U DOKUMENTU VRACIS CELY DOKUMENT! JAKO GETVALUE! NEBEZPECNE!");
		return this;
	}

	@Override
	public int compareTo(AbstractValue o) {
//		System.out.println("VOLAS compareTo U " + this + "::::::::::AND::::::::::" + o);
//		return -1;
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void setIdentifier(AbstractIdentifier superid) {
		this.superid = superid;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append(superid);
		builder.append(" -> ");

		builder.append("{");

		int index = 0;
		for (Map.Entry<String, AbstractProperty> property : properties.entrySet()) {
//		properties.entrySet().forEach(property -> {
			builder.append(property);
			if (++index < properties.size()) {
				builder.append(",");
			}

//		});
		}

		builder.append("}");

		builder.append("+REF:");
		references.forEach(reference -> {
			builder.append(reference);
		});

		return builder.toString();
	}

	@Override
	public String getName() {
		return name;
	}

}
