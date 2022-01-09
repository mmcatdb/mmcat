package cat.transformations.category;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author pavel.contos
 */
public class InstanceCategory {

	public CategoricalObject getOrCreate(String collectionName) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public CategoricalObject get(String key) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public CategoricalObject create(String key) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public void createMorphism(String string, EntityObject entity, CategoricalObject object) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public AttributeMorphism getOrCreateMorphism(String string, EntityObject entity, AttributeObject attribute) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public EntityObject getOrCreateEntity(String name) {
//		System.out.println("ZKOUSIM getOrCreateEntity WITH NAME: " + name);
		if (objects.containsKey(name)) {
			return (EntityObject) objects.get(name);// WARN: Exception - casting!
		} else {
			EntityObject entity = new EntityObject(name);
			objects.put(name, entity);
			return entity;
		}
	}

	public AttributeObject getOrCreateAttribute(String name) {
		if (objects.containsKey(name)) {
			return (AttributeObject) objects.get(name);// WARN: Exception - casting!

		} else {
			AttributeObject attribute = new AttributeObject(name);
			objects.put(name, attribute);
			return attribute;
		}
	}

	public AttributeMorphism getOrCreateAttributeMorphism(String name, EntityObject entity, AttributeObject attribute) {
		if (morphisms_TODOSIMPLE.containsKey(name)) {
			return (AttributeMorphism) morphisms_TODOSIMPLE.get(name);
		} else {
			AttributeMorphism morphism = new AttributeMorphism(name, entity, attribute);
			morphisms_TODOSIMPLE.put(name, morphism);
			return morphism;
		}
	}

	public RelationshipMorphism getOrCreateRelationshipMorphism(String name, EntityObject entity, EntityObject object) {
		if (morphisms_TODOSIMPLE.containsKey(name)) {
			return (RelationshipMorphism) morphisms_TODOSIMPLE.get(name);
		} else {
			RelationshipMorphism morphism = new RelationshipMorphism(name, entity, object);
			morphisms_TODOSIMPLE.put(name, morphism);
			return morphism;
		}
	}

	

	private static final class HomSetPair implements Comparable<HomSetPair> {

		// entitni objekt by mel obsahovat seznam sloupcu
		// a ten seznam sloupcu by mel byt hashovaci tabulkou, diky ktere ziskame index, ve kterem se neco schovava (hodnota pro dany klic)
		// rozlisovat FK, PK, attribute morfismy?
		private final String domain;
		private final String codomain;

		public HomSetPair(String domain, String codomain) {
			this.domain = domain;
			this.codomain = codomain;
		}

		@Override
		public int compareTo(HomSetPair other) {
			int domainComparison = domain.compareTo(other.domain);

			return domainComparison != 0 ? domainComparison : codomain.compareTo(other.codomain);
		}
	}

	// kategorie je mnozinou objektu
	private final Map<String, CategoricalObject> objects = new TreeMap<>();
	// homset pro kazdy par morfismu, kde homset(a,b) obsahuje vsechny morfismy (a,b)
	private final Map<HomSetPair, Set<CategoricalMorphism>> morphisms = new TreeMap<>();

	// WARN: this is temporary, better implementation in morphisms (line abowe)
	private final Map<String, CategoricalMorphism> morphisms_TODOSIMPLE = new TreeMap<>();

	public InstanceCategory() {
	}

	public int getColumnsCount(String entityName) {
		return findOutMorphisms(entityName).size();
	}

	public Map<String, Object> getDocumentRecord(String entityName, int index) {
		Map<String, Object> document = new TreeMap<>();

		int column = 0;
		var outMorphisms = findOutMorphisms(entityName);
		for (var morphism : outMorphisms) {
			EntityObject object = (EntityObject) objects.get(entityName);
			var value = morphism.getValue(object.get(index));
			morphism.getCodomain();
//			System.out.println("VALUE: " + value);

			// WARN: TADY MUSIS REALIZOVAT PRUCHOD GRAFEM DO HLOUBKY, PROTOZE MUZES MIT DO NEKONECNA ZANOROVANE DOKUMENTY TAM A ZPET, NAVSTIVENE JIZ JEDNOU IGNORUJ!
			Set<String> visited = new TreeSet<>();

			if (value == null) {
				System.out.println("EntityName: " + entityName + " index: " + index + " IS NULL!!!!!");
				value = "";// TODO! BUG! MUSIS TAM NECHAT NULL! ZKUS TO S NULL
				document.put(morphism.getCodomain(), value);
			} else {
				if (value instanceof List) {
					System.out.println("TODO: InstanceCategory->getDocumentRecord() :: process nested array");
					// nebude to fungovat pres isntanceof, musis to delat tak, jak popisujes
					// process array
					// mnozina mapovani, {} -> {}, velikost objektu kontrolovat, jestli je to 1 nebo vice a pak to spada sem, takze value size? a kontrolovat codomain type! to ti urci, jestli je to entita nebo property!
					document.put(morphism.getCodomain(), processEmbeddedArray(/*TODO PARAMS*/visited));
				} else if (value instanceof Map) {
					System.out.println("TODO: InstanceCategory->getDocumentRecord() :: process nested document");
					// tady kontroluj, jestli je to na druhe strane entita nebo vztah
					// process map
					// a zaroven do mnoziny visited pridej jmeno, protoze tento entitni objekt bude zpracovan, tak at se tam nevracis, ale pridej to az ve vnorene funkci
					document.put(morphism.getCodomain(), processEmbeddedDocument(/*TODO PARAMS*/visited));
				} else {
					// tady kontroluj, jestli to na druhe strane je property
					// process property
					document.put(morphism.getCodomain(), value);
				}
				// ERROR: PRIRAZUJES DO SPATNEHO SLOUPCE! NEKDE SI MUSIS PAMATOVAT PORADI NEBO S TIM NEJAK PRACOVAT NAHODOU TO TED FUNGUJE, ALE OPRAVDU NAHODOU
			}
			++column;
		}
		return document;
	}
	
	private Object processEmbeddedArray(Set<String> visited) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	private Object processEmbeddedDocument(Set<String> visited) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	public Object[] getRelationalRecord(String entityName, int index) {
		// projdi vsechny morfismy a vytvor z toho Map<Identifier, Object[]> a vrat to cele? Nebo v prvnim kroku pouze ten jeden radek...
		Object[] result = new Object[getColumnsCount(entityName)];

		int column = 0;
		var outMorphisms = findOutMorphisms(entityName);
		for (var morphism : outMorphisms) {
			EntityObject object = (EntityObject) objects.get(entityName);
			var value = morphism.getValue(object.get(index));
//			System.out.println("VALUE: " + value);

			if (value == null) {
				System.out.println("EntityName: " + entityName + " index: " + index + " IS NULL!!!!!");
				result[column] = "";
			} else {

				result[column] = value;	// ERROR: PRIRAZUJES DO SPATNEHO SLOUPCE! NEKDE SI MUSIS PAMATOVAT PORADI NEBO S TIM NEJAK PRACOVAT NAHODOU TO TED FUNGUJE, ALE OPRAVDU NAHODOU
			}
			++column;
		}

		return result;
	}

// ----- BEGINNING ----- INSTANCE TO REL TRANSFORMATION ----------------------------------------------------------------
	public int size() {
		return objects.size();
	}

	public boolean isEntity(String entityName) {
		// WARN: Vysoce neefektivni implementace, bude potreba zmenit rozhrani a umoznovat u kazdeho objektu urcit, jakeho typu ten dany objekt je!
		// coz my vime, protoze mame tridu pro kazdy typ a bylo by dobre mit tam i nejaky staticky atribut TYPE = Entity!
		boolean first = false;
		boolean second = false;
		for (var entry : morphisms_TODOSIMPLE.entrySet()) {
			if (first && second) {
				break;
			}

			if (entityName.equals(entry.getValue().getDomain()) && !entityName.equals(entry.getValue().getCodomain())) {
				// rovna se domene, ale nerovna se codomene
				first = true;
			} else if (entityName.equals(entry.getValue().getCodomain())) {
				second = true;
			} else {
			}
		}
		//		System.out.println(entityName + " :: " + first + " :: " + second);
		// entitni typ bud obsahuje na konci i na zacatku (pokud je odkazovan jinou tabulkou) nebo obsahuje pouze na zacatku, ale nikdy ne jen na konci
		return (first && second) || (first && !second);
	}

	public List<CategoricalMorphism> findOutMorphisms(String entityName) {
		// WARN: Vysoce neefektivni implementace, mnohem lepsi je pamatovat si homset pro kazdy objekt! umoznilo by to pruchod grafem!
		List<CategoricalMorphism> result = new ArrayList<>();

		for (var entry : morphisms_TODOSIMPLE.entrySet()) {

			if (entityName.equals(entry.getValue().getDomain())/* && !entityName.equals(entry.getValue().getCodomain())*/) {
				// rovna se domene, ale nerovna se codomene
				result.add(entry.getValue());
			}
		}
		return result;
	}

	public Set<String> getObjectNames() {
		return objects.keySet();
	}

	public CategoricalObject getObject(String name) {
		return objects.get(name);
	}

// ----- END ----------- INSTANCE TO REL TRANSFORMATION ----------------------------------------------------------------	
	public boolean addEntityObject(String entityName) {
		if (objects.containsKey(entityName)) {
			System.out.println("Object " + entityName + " already exists - not created");
			return false;
		}
		objects.put(entityName, new EntityObject(entityName));
		return true;
	}

	public boolean addRelationshipObject(String relationshipName) {
		if (objects.containsKey(relationshipName)) {
			System.out.println("Object " + relationshipName + " already exists - not created");
			return false;
		}
		objects.put(relationshipName, new RelationshipObject(relationshipName));
		return true;
	}

	public boolean addAttributeObject(String attributeName) {
		if (objects.containsKey(attributeName)) {
			System.out.println("Object " + attributeName + " already exists - not created");
			return false;
		}
		objects.put(attributeName, new AttributeObject(attributeName));
		return true;
	}

//	public void addMorphism(String domain, String codomain, String morphismName) {
//	}
	public void addEntityObjectValue(String entityName, EntityObject.EntityValue value) {
		objects.get(entityName).add(value);
	}

	public void addRelationshipObjectValue() {
	}

	public void addAttributeObjectValue(String attributeName, Object object) {
		objects.get(attributeName).add(object);
	}

	public void addAttributeMorphism(String morphismName, String domain, String codomain) {
		addAttributeMorphism(morphismName, objects.get(domain), objects.get(codomain));
	}

	public void addAttributeMorphism(String morphismName, CategoricalObject domain, CategoricalObject codomain) {
		// TODO: for now simplified: only one morphism per (a,b) pair!
		AttributeMorphism morphism = new AttributeMorphism(morphismName, domain, codomain);
		morphisms_TODOSIMPLE.put(morphismName, morphism);
	}

	public void addAttributeMorphismMapping(String morphismName, String domain, String codomain, EntityObject.EntityValue key, Object value) {
		addAttributeMorphismMapping(morphismName, objects.get(domain), objects.get(codomain), key, value);
	}

	public void addAttributeMorphismMapping(String morphismName, CategoricalObject domain, CategoricalObject codomain, EntityObject.EntityValue key, Object value) {
		CategoricalMorphism morphism = morphisms_TODOSIMPLE.get(morphismName);
		morphism.add(key, value);
	}

	public void addRelationshipMorphism(String morphismName, String domain, String codomain) {
		addRelationshipMorphism(morphismName, objects.get(domain), objects.get(codomain));
	}

	public void addRelationshipMorphism(String morphismName, CategoricalObject domain, CategoricalObject codomain) {
		RelationshipMorphism morphism = new RelationshipMorphism(morphismName, domain, codomain);
		morphisms_TODOSIMPLE.put(morphismName, morphism);
	}

	public void addRelationshipMorphismMapping(String morphismName, String domain, String codomain, EntityObject.EntityValue key, Object value) {
//		System.out.println(String.format("VOLANO addRelationshipMorphismMapping(%s, %s, %s, %s, %s)", morphismName, domain, codomain, key, value));
		addRelationshipMorphismMapping(morphismName, objects.get(domain), objects.get(codomain), key, value);
	}

	public void addRelationshipMorphismMapping(String morphismName, CategoricalObject domain, CategoricalObject codomain, EntityObject.EntityValue key, Object value) {
		CategoricalMorphism morphism = morphisms_TODOSIMPLE.get(morphismName);
//		System.out.println("VKLADANO DO: " + morphism);
		morphism.add(key, value);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("Objects:\t");
		for (String key : objects.keySet()) {
			builder.append(key).append(", ");
		}
		builder.append("\n");

		for (String key : objects.keySet()) {
			var object = objects.get(key);
			builder.append(object);
			builder.append("\n");
		}
		builder.append("\n");

		for (String key : morphisms_TODOSIMPLE.keySet()) {
			var object = morphisms_TODOSIMPLE.get(key);
			builder.append(object);
			builder.append("\n");
		}
		builder.append("\n");

		return builder.toString();
	}

}
