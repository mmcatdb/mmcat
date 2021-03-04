/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2;

import cat.transformations.algorithms2.model.AbstractValue;
import cat.transformations.algorithms2.model.AbstractReference;
import cat.transformations.algorithms2.model.AbstractObject;
import cat.transformations.algorithms2.model.AbstractMorphism;
import cat.transformations.algorithms2.model.AbstractModel;
import cat.transformations.algorithms2.model.AbstractInstance;
import cat.transformations.algorithms2.model.AbstractKind;
import java.util.LinkedList;
import java.util.Queue;
import cat.transformations.algorithms2.model.AbstractSimpleProperty;
import cat.transformations.algorithms2.model.AbstractRecordProperty;

/**
 *
 * @author pavel.koupil
 */
public class TransformationModelToInst {

	private String morphismName(String domain, String codomain) {
		return domain + "->" + codomain;
	}

	private void processAttribute(AbstractInstance instance, AbstractObject entity, Iterable<? extends AbstractValue> superid, AbstractSimpleProperty property) {
		var propertyName = property.getName();
		var attribute = instance.get(propertyName);
		attribute.add(property.getValue());		// WARN: TOHLE NENI ITERABLE! JE TREBA UPRAVIT ROZHRANI U ABSTRACT_OBJECT!
		AbstractMorphism morphism = instance.getMorphism(morphismName(entity.getName(), propertyName));
		morphism.add(superid, property.getValue());
	}

	private void processStructuredAttribute(AbstractInstance instance, AbstractObject entity, Iterable<? extends AbstractValue> superid, AbstractSimpleProperty property, Queue<AbstractRecordProperty> queue, Queue<String> queueOfNames) {
		// aktualne zpracovavam property - POZOR: strukturovany atribut se od entity lisi pouze tim, ze ma morfismus pouze jednim smerem, zatimco entita ma morfismy obema smery!
		var value = (AbstractRecordProperty) property.getValue();		// ted mas i value, kterou ale musi byt embedded document, a tedy abstractRecord
		var object = instance.get(property.getName()); // ted mas i zpracovavany objekt

		var valueSuperid = value.getIdentifiers();

		AbstractMorphism morphism = instance.getMorphism(morphismName(entity.getName(), object.getName()));	// WARN: co kdyz se odkazuje na jiny? TOHLE MUSIS OVERIT!
		morphism.add(superid, valueSuperid.iterator().next());
		queue.add(value);
		queueOfNames.add(property.getName());
	}

	private void processEntity(AbstractInstance instance, AbstractObject entity, Iterable<? extends AbstractValue> superid, AbstractRecordProperty value, String propertyName, Queue<AbstractRecordProperty> queue, Queue<String> queueOfNames) {
		// aktualne zpracovavam property
//		var value = (AbstractRecord) property.getValue();		// ted mas i value, kterou ale musi byt embedded document, a tedy abstractRecord
		var object = instance.get(propertyName); // ted mas i zpracovavany objekt

		var valueSuperid = value.getIdentifiers();

		AbstractMorphism morphism = instance.getMorphism(morphismName(entity.getName(), object.getName()));	// WARN: co kdyz se odkazuje na jiny? TOHLE MUSIS OVERIT!
		morphism.add(superid, valueSuperid.iterator().next());
		AbstractMorphism comorphism = instance.getMorphism(morphismName(object.getName(), entity.getName()));	// WARN: co kdyz se odkazuje na jiny? TOHLE MUSIS OVERIT!
		comorphism.add(valueSuperid, superid.iterator().next());	// vyuzivam vlastnosti, ze kazdy identifikator ma schopnost identifikace!
		queue.add(value);
		queueOfNames.add(propertyName);
	}

	private void processArray(AbstractInstance instance, AbstractObject entity, Iterable<? extends AbstractValue> superid, AbstractSimpleProperty property, Queue<AbstractRecordProperty> queue, Queue<String> queueOfNames) {
		var array = (Iterable<? extends AbstractValue>) property.getValue();

		int size = 0;

		for (var value : array) {
			++size;

			switch (value.getType()) {
				case ATTRIBUTE ->
					processAttribute(instance, entity, superid, (AbstractSimpleProperty) value);
				case STRUCTURED_ATTRIBUTE ->
					processStructuredAttribute(instance, entity, superid, (AbstractSimpleProperty) value, queue, queueOfNames);
				case RECORD ->
					// v poli mas rovnou AbstractRecords!
					processEntity(instance, entity, superid, (AbstractRecordProperty) value, property.getName(), queue, queueOfNames);
				case ARRAY ->
					processArray(instance, entity, superid, (AbstractSimpleProperty) value, queue, queueOfNames);
				case REFERENCE ->
					processReference();
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
					var superid = _record.getIdentifiers();
//					AbstractObject entity = instance.getOrCreate(kindName, AbstractType.ENTITY);
					AbstractObject entity = instance.get(_name);
					entity.add(superid);

					// process properties
					// odkud mame testovat AbstractType? Z instancni/schematicke kategorie nebo primo z instance? Nebo vytvarime mapovani logickeho modelu primo podle znalosti schematicke instance?
					for (AbstractValue property : _record.getProperties()) {

						switch (property.getType()) {
							case ATTRIBUTE ->
								processAttribute(instance, entity, superid, (AbstractSimpleProperty) property);
							case STRUCTURED_ATTRIBUTE ->
								processStructuredAttribute(instance, entity, superid, (AbstractSimpleProperty) property, queue, queueOfNames);
							case RECORD -> {
								AbstractSimpleProperty p = (AbstractSimpleProperty) property;
								processEntity(instance, entity, superid, (AbstractRecordProperty) p.getValue(), p.getName(), queue, queueOfNames);
							}
							case ARRAY ->
								processArray(instance, entity, superid, (AbstractSimpleProperty) property, queue, queueOfNames);
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
						AbstractMorphism morphism = instance.getMorphism(morphismName(_name, referencedName));
						morphism.add(superid, reference);

					}
				}
			}

		}

	}

}
