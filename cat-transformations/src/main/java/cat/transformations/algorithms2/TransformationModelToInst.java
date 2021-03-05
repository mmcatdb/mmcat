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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavel.koupil
 */
public class TransformationModelToInst {

	private static final Logger LOGGER = Logger.getLogger(TransformationModelToInst.class.getName());

	public static String morphismName(String domain, String codomain) {
		return domain + "->" + codomain;
	}

	public void process(AbstractModel model, AbstractInstance instance) {
		LOGGER.log(Level.INFO, "--- BEGINNING");
		Queue<AbstractRecordProperty> queue = new LinkedList<>();
		Queue<String> queueOfNames = new LinkedList<>();

		for (AbstractKind kind : model.getKinds()) {
			LOGGER.log(Level.INFO, String.format("Processing KIND %s", kind.getName()));
			String kindName = kind.getName();

			for (AbstractRecordProperty record : kind.getRecords()) {
				queue.add(record);
				queueOfNames.add(kindName);

				while (!queue.isEmpty()) {
					AbstractRecordProperty _record = queue.poll();
					String _name = queueOfNames.poll();

					LOGGER.log(Level.INFO, String.format("\tProcessing RECORD %s WITH ASSIGNED NAME %s", _record, _name));

					// process identifier(s) - insert superid do entity according to kind.getName()
					var superid = _record.getIdentifier();
//					AbstractObject entity = instance.getOrCreate(kindName, AbstractType.ENTITY);
					AbstractCategoricalObject entity = instance.get(_name);
					entity.add(superid);
					LOGGER.log(Level.INFO, String.format("\tRetrieved entity %s", entity));

					for (AbstractProperty property : _record.getProperties()) {
						AbstractCategoricalObject object = instance.get(property.getName());
						LOGGER.log(Level.INFO, String.format("\t\tWorking with a pair (%s, %s) of TYPES (%s, %s)", entity.getName(), object.getName(), entity.getType(), object.getType()));

						switch (object.getType()) {
							case KIND ->
								processEntity(instance, entity, superid, (AbstractRecordProperty) property.getValue(), property.getName(), queue, queueOfNames);
							case RECORD ->
								processEntity(instance, entity, superid, (AbstractRecordProperty) property.getValue(), property.getName(), queue, queueOfNames);
							case ARRAY ->
								processArray(instance, entity, superid, (AbstractArrayProperty) property, queue, queueOfNames);
							case INLINED ->
								LOGGER.log(Level.SEVERE, "\t\tINLINED TODO");
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
					LOGGER.log(Level.INFO, "Nasleduje zpracovani referenci");
					for (AbstractReference reference : _record.getReferences()) {
						LOGGER.log(Level.WARNING, String.format("TODO - PROCESS REFERENCES", reference));
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

	private void processValue(AbstractInstance instance, AbstractCategoricalObject entity, AbstractIdentifier superid, AbstractProperty property) {
//		System.out.println(String.format("\t\tVALUE superid %s name %s value %s", superid, property.getName(), property.getValue()));
		var propertyName = property.getName();
		var attribute = instance.get(propertyName);
		attribute.add(property.getValue());		// WARN: TOHLE NENI ITERABLE! JE TREBA UPRAVIT ROZHRANI U ABSTRACT_OBJECT!
		LOGGER.log(Level.SEVERE, String.format("\t\tAdded value %s to domain %s", property.getValue(), attribute.getName()));
		AbstractCategoricalMorphism morphism = instance.getMorphism(morphismName(entity.getName(), propertyName));
		morphism.add(superid, property.getValue());
		LOGGER.log(Level.SEVERE, String.format("\t\tAdded mappings (%s, %s) TO %s", superid, property.getValue(), morphism));
	}

	private void processAttribute(AbstractInstance instance, AbstractCategoricalObject entity, AbstractIdentifier superid, AbstractAttributeProperty property) {
		var propertyName = property.getName();
		var attribute = instance.get(propertyName);
		attribute.add(property.getValue());		// WARN: TOHLE NENI ITERABLE! JE TREBA UPRAVIT ROZHRANI U ABSTRACT_OBJECT!
		LOGGER.log(Level.INFO, String.format("\t\tAdded value %s to domain %s", property.getValue(), attribute.getName()));
		System.out.println(morphismName(entity.getName(), propertyName));
		AbstractCategoricalMorphism morphism = instance.getMorphism(morphismName(entity.getName(), propertyName));
		morphism.add(superid, property.getValue());
		LOGGER.log(Level.INFO, String.format("\t\tAdded mappings (%s, %s) TO %s", superid, property.getValue(), morphism));

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
		LOGGER.log(Level.INFO, String.format("\t\tAdded mappings (%s, %s) TO %s", superid, valueSuperid, morphism));
	}

	private void processEntity(AbstractInstance instance, AbstractCategoricalObject entity, AbstractIdentifier superid, AbstractRecordProperty value, String propertyName, Queue<AbstractRecordProperty> queue, Queue<String> queueOfNames) {
		// aktualne zpracovavam property
//		var value = (AbstractRecord) property.getValue();		// ted mas i value, kterou ale musi byt embedded document, a tedy abstractRecord
		var object = instance.get(propertyName); // ted mas i zpracovavany objekt

		var valueSuperid = value.getIdentifier();

		AbstractCategoricalMorphism morphism = instance.getMorphism(morphismName(entity.getName(), object.getName()));	// WARN: co kdyz se odkazuje na jiny? TOHLE MUSIS OVERIT!
		System.out.println(morphismName(entity.getName(), propertyName));
		morphism.add(superid, valueSuperid);
		AbstractCategoricalMorphism comorphism = instance.getMorphism(morphismName(object.getName(), entity.getName()));	// WARN: co kdyz se odkazuje na jiny? TOHLE MUSIS OVERIT!
		comorphism.add(valueSuperid, superid);	// vyuzivam vlastnosti, ze kazdy identifikator ma schopnost identifikace!
		queue.add(value);
		queueOfNames.add(value.getName());
		LOGGER.log(Level.INFO, String.format("\t\tEntity %s - Added mappings (%s,%s) TO %s and comapping (%s,%s) TO %s", propertyName, superid, valueSuperid, morphism.getName(), valueSuperid, superid, comorphism.getName()));
	}

	private void processArray(AbstractInstance instance, AbstractCategoricalObject entity, AbstractIdentifier superid, AbstractArrayProperty property, Queue<AbstractRecordProperty> queue, Queue<String> queueOfNames) {
		var array = (Iterable<? extends AbstractProperty>) property.getElements();

		int size = 0;

		for (var value : array) {
			++size;
			AbstractCategoricalObject object = instance.get(value.getName());
			LOGGER.log(Level.INFO, String.format("\t\tZpracovavas %s prvek (%s,%s) pole (%s, %s)", size, object.getName(), object.getType(), property.getName(), property.getType()));
			switch (object.getType()) {
				case KIND ->
					processEntity(instance, entity, superid, (AbstractRecordProperty) value, property.getName(), queue, queueOfNames);
				case RECORD ->
					processEntity(instance, entity, superid, (AbstractRecordProperty) value, property.getName(), queue, queueOfNames);
				case ARRAY ->
					processArray(instance, entity, superid, (AbstractArrayProperty) value, queue, queueOfNames);
				case INLINED ->
					LOGGER.log(Level.SEVERE, "\t\tINLINED TODO");
				case ATTRIBUTE ->
					processAttribute(instance, instance.get(property.getName()), superid, (AbstractAttributeProperty) value);
				case STRUCTURED_ATTRIBUTE ->
					processStructuredAttribute(instance, entity, superid, (AbstractAttributeProperty) value, queue, queueOfNames);
				case IDENTIFIER ->
					processAttribute(instance, entity, superid, (AbstractAttributeProperty) value);
				case REFERENCE ->
					// SKIP FOR NOW! RESIS JE O PAR RADKU NIZ!
					// MELY BY SE PRESKOCIT, PROTOZE JEJICH HODNOTY PATRI DO JINYCH MORFISMU
					processReference();
//				case VALUE ->
//					processValue(instance, entity, superid, (AbstractProperty) value);
			}

		}

		if (size == 0) {
			LOGGER.log(Level.WARNING, "\t\tPole bylo prazdne, nic nedelas");
			// TODO: Co kdyz bylo pole prazdne? Asi nic nedelame...
		}
	}

	private void processReference() {
		LOGGER.log(Level.SEVERE, "NEMELO BY NASTAVAT!");
	}

}
