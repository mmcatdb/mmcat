/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2;

import cat.transformations.algorithms2.model.AbstractArrayProperty;
import cat.transformations.algorithms2.model.AbstractReference;
import cat.transformations.algorithms2.model.AbstractModel;
import cat.transformations.algorithms2.model.AbstractInstance;
import cat.transformations.algorithms2.model.AbstractKind;
import cat.transformations.algorithms2.model.AbstractProperty;
import java.util.LinkedList;
import java.util.Queue;
import cat.transformations.algorithms2.model.AbstractRecordProperty;
import cat.transformations.algorithms2.model.AbstractAttributeProperty;
import cat.transformations.algorithms2.model.AbstractCategoricalObject;
import cat.transformations.algorithms2.model.AbstractCategoricalMorphism;
import cat.transformations.algorithms2.model.AbstractIdentifier;

/**
 *
 * @author pavel.koupil
 */
public class TransformationModelToInst {

	public static String morphismName(String domain, String codomain) {
		return domain + "->" + codomain;
	}

	private void processValue(AbstractInstance instance, AbstractCategoricalObject entity, AbstractIdentifier superid, AbstractProperty property) {
//		System.out.println(String.format("\t\tVALUE superid %s name %s value %s", superid, property.getName(), property.getValue()));
		var propertyName = property.getName();
		var attribute = instance.get(propertyName);
		attribute.add(property.getValue());		// WARN: TOHLE NENI ITERABLE! JE TREBA UPRAVIT ROZHRANI U ABSTRACT_OBJECT!
		AbstractCategoricalMorphism morphism = instance.getMorphism(morphismName(entity.getName(), propertyName));
		morphism.add(superid, property.getValue());
	}

	private void processAttribute(AbstractInstance instance, AbstractCategoricalObject entity, AbstractIdentifier superid, AbstractAttributeProperty property) {
		var propertyName = property.getName();
		var attribute = instance.get(propertyName);
		attribute.add(property.getValue());		// WARN: TOHLE NENI ITERABLE! JE TREBA UPRAVIT ROZHRANI U ABSTRACT_OBJECT!
		AbstractCategoricalMorphism morphism = instance.getMorphism(morphismName(entity.getName(), propertyName));
		morphism.add(superid, property.getValue());
	}

	private void processStructuredAttribute(AbstractInstance instance, AbstractCategoricalObject entity, AbstractIdentifier superid, AbstractAttributeProperty property, Queue<AbstractRecordProperty> queue, Queue<String> queueOfNames) {
		// aktualne zpracovavam property - POZOR: strukturovany atribut se od entity lisi pouze tim, ze ma morfismus pouze jednim smerem, zatimco entita ma morfismy obema smery!
		var value = (AbstractRecordProperty) property.getValue();		// ted mas i value, kterou ale musi byt embedded document, a tedy abstractRecord
		var object = instance.get(property.getName()); // ted mas i zpracovavany objekt

		var valueSuperid = value.getIdentifier();

		AbstractCategoricalMorphism morphism = instance.getMorphism(morphismName(entity.getName(), object.getName()));	// WARN: co kdyz se odkazuje na jiny? TOHLE MUSIS OVERIT!
		morphism.add(superid, valueSuperid);
		queue.add(value);
		queueOfNames.add(property.getName());
	}

	private void processEntity(AbstractInstance instance, AbstractCategoricalObject entity, AbstractIdentifier superid, AbstractRecordProperty value, String propertyName, Queue<AbstractRecordProperty> queue, Queue<String> queueOfNames) {
		// aktualne zpracovavam property
//		var value = (AbstractRecord) property.getValue();		// ted mas i value, kterou ale musi byt embedded document, a tedy abstractRecord
		var object = instance.get(propertyName); // ted mas i zpracovavany objekt

		var valueSuperid = value.getIdentifier();

		AbstractCategoricalMorphism morphism = instance.getMorphism(morphismName(entity.getName(), object.getName()));	// WARN: co kdyz se odkazuje na jiny? TOHLE MUSIS OVERIT!
		morphism.add(superid, valueSuperid);
		AbstractCategoricalMorphism comorphism = instance.getMorphism(morphismName(object.getName(), entity.getName()));	// WARN: co kdyz se odkazuje na jiny? TOHLE MUSIS OVERIT!
		comorphism.add(valueSuperid, superid);	// vyuzivam vlastnosti, ze kazdy identifikator ma schopnost identifikace!
		queue.add(value);
		queueOfNames.add(propertyName);
	}

	private void processArray(AbstractInstance instance, AbstractCategoricalObject entity, AbstractIdentifier superid, AbstractArrayProperty property, Queue<AbstractRecordProperty> queue, Queue<String> queueOfNames) {
		var array = (Iterable<? extends AbstractProperty>) property.getElements();

		int size = 0;

		for (var value : array) {
			++size;

			switch (value.getType()) {
				case KIND ->
					processEntity(instance, entity, superid, (AbstractRecordProperty) value, property.getName(), queue, queueOfNames);
				case RECORD ->
					processEntity(instance, entity, superid, (AbstractRecordProperty) value, property.getName(), queue, queueOfNames);
				case ARRAY ->
					processArray(instance, entity, superid, (AbstractArrayProperty) value, queue, queueOfNames);
				case INLINED ->
					System.out.println("TODO - ALGORITHM INLINED!");
				case ATTRIBUTE ->
					processAttribute(instance, entity, superid, (AbstractAttributeProperty) value);
				case STRUCTURED_ATTRIBUTE ->
					processStructuredAttribute(instance, entity, superid, (AbstractAttributeProperty) value, queue, queueOfNames);
				case IDENTIFIER ->
					processAttribute(instance, entity, superid, (AbstractAttributeProperty) value);
				case REFERENCE ->
					// SKIP FOR NOW! RESIS JE O PAR RADKU NIZ!
					// MELY BY SE PRESKOCIT, PROTOZE JEJICH HODNOTY PATRI DO JINYCH MORFISMU
					processReference();
				case VALUE ->
					processValue(instance, entity, superid, (AbstractProperty) value);
			}

		}

		if (size == 0) {
			// TODO: Co kdyz bylo pole prazdne? Asi nic nedelame...
		}
	}

	private void processReference() {
		// SKIP, RESIS JE V MAIN ALGORITMU O PAR RADKU NIZ!
	}

	public void process(AbstractModel model, AbstractInstance instance) {
		Queue<AbstractRecordProperty> queue = new LinkedList<>();
		Queue<String> queueOfNames = new LinkedList<>();

		for (AbstractKind kind : model.getKinds()) {
			String kindName = kind.getName();

			for (AbstractRecordProperty record : kind.getRecords()) {
				queue.add(record);
				queueOfNames.add(kindName);

				while (!queue.isEmpty()) {
					AbstractRecordProperty _record = queue.poll();
					String _name = queueOfNames.poll();

					// process identifier(s) - insert superid do entity according to kind.getName()
					var superid = _record.getIdentifier();
//					AbstractObject entity = instance.getOrCreate(kindName, AbstractType.ENTITY);
					AbstractCategoricalObject entity = instance.get(_name);
					entity.add(superid);

					// process properties
					// odkud mame testovat AbstractType? Z instancni/schematicke kategorie nebo primo z instance? Nebo vytvarime mapovani logickeho modelu primo podle znalosti schematicke instance?
					for (AbstractProperty property : _record.getProperties()) {

						// tohle je spatne implementovano, tohle jsou data, ktera by mela byt ve schematicke kategorii - a nemela by byt obsazena ve wrapperu!
						switch (property.getType()) {
							case KIND ->
								processEntity(instance, entity, superid, (AbstractRecordProperty) property.getValue(), property.getName(), queue, queueOfNames);
							case RECORD ->
								processEntity(instance, entity, superid, (AbstractRecordProperty) property.getValue(), property.getName(), queue, queueOfNames);
							case ARRAY ->
								processArray(instance, entity, superid, (AbstractArrayProperty) property, queue, queueOfNames);
							case INLINED ->
								System.out.println("TODO - ALGORITHM INLINED!");

							case ATTRIBUTE ->
								processAttribute(instance, entity, superid, (AbstractAttributeProperty) property);
							case STRUCTURED_ATTRIBUTE ->
								processStructuredAttribute(instance, entity, superid, (AbstractAttributeProperty) property, queue, queueOfNames);
							case IDENTIFIER ->
								processAttribute(instance, entity, superid, (AbstractAttributeProperty) property);
							case REFERENCE ->
								// SKIP FOR NOW! RESIS JE O PAR RADKU NIZ!
								// MELY BY SE PRESKOCIT, PROTOZE JEJICH HODNOTY PATRI DO JINYCH MORFISMU
								processReference();
						}

					}

					// process reference(s)
					// reference je dvojice, kdy mam referenci (sloupce) a na co reference odkazuje
					for (AbstractReference reference : _record.getReferences()) {
						String referencedName = reference.getReferenced();
						var referenced = instance.get(referencedName);
						// TODO: morphismName
						AbstractCategoricalMorphism morphism = instance.getMorphism(morphismName(_name, referencedName));
						morphism.add(superid, reference);

					}
				}
			}

		}

	}

}
