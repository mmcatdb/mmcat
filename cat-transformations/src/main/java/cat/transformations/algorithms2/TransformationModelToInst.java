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
import cat.transformations.algorithms2.model.AbstractValue;
import cat.transformations.algorithms2.model.SimpleIdentifier;
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
						System.out.println(property.getName() + ":::!!!!");
						AbstractCategoricalObject object = instance.get(property.getName());
						LOGGER.log(Level.INFO, String.format("\t\tWorking with a pair (%s, %s) of TYPES (%s, %s)", entity.getName(), object.getName(), entity.getType(), object.getType()));

						switch (object.getType()) {
							case KIND ->
								// TOHLE NESMI NASTAT! NEBO NASTAVA TO NA TOP LEVEL UROVNI?
								processRecord(instance, entity, superid, (AbstractRecordProperty) property.getValue(), property.getName(), queue, queueOfNames);
							case RECORD ->
								// TOHLE JE PRIPAD PRIMO VNORENEHO DOKUMENTU, TAKZE MUSIS UDELAT VZTAHOVY OBJEKT + VNORENY OBJEKT, ALTERNATIVOU JE STRUCTURED_ATTRIBUTE, OBOJI KARDINALITY 1:1
								// NASTAVA TO NEKDY V TOMTO PRIPADE? NE... TADY TO MUSI BYT STRUCTURED ATTRIBUTE NEBO ARRAY!
								// TOHLE NESMI NASTAT!
								processRecord(instance, entity, superid, (AbstractRecordProperty) property.getValue(), property.getName(), queue, queueOfNames);
							case ARRAY ->
								processArray(instance, entity, superid, (AbstractArrayProperty) property, queue, queueOfNames);
							case INLINED ->
								processInlined();
							case ATTRIBUTE ->
								processAttribute(instance, entity, superid, (AbstractAttributeProperty) property);
							case MULTI_ATTRIBUTE ->
								processMultiAttribute(instance, entity, superid, (AbstractArrayProperty) property);
							case INLINED_ATTRIBUTE ->
								processAttribute(instance, entity, superid, (AbstractAttributeProperty) property);
							case STRUCTURED_ATTRIBUTE ->
								processStructuredAttribute(instance, entity, superid, (AbstractRecordProperty) property, queue, queueOfNames);
							case INLINED_STRUCTURED_ATTRIBUTE ->
								processStructuredAttribute(instance, entity, superid, (AbstractRecordProperty) property, queue, queueOfNames);
							case IDENTIFIER ->
								processAttribute(instance, entity, superid, (AbstractAttributeProperty) property);
							case MULTI_IDENTIFIER ->
								System.out.println("TODO - MULTI_IDENTIFIER");
							case REFERENCE ->
								// SKIP FOR NOW! RESIS JE O PAR RADKU NIZ!
								// MELY BY SE PRESKOCIT, PROTOZE JEJICH HODNOTY PATRI DO JINYCH MORFISMU
								processReference();
							case MULTI_REFERENCE ->
								// JAK BY TOHLE MELO VYPADAT?
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

	private void processStructuredAttribute(AbstractInstance instance, AbstractCategoricalObject parentObject, AbstractIdentifier parentSuperid, AbstractRecordProperty structuredProperty, Queue<AbstractRecordProperty> queue, Queue<String> queueOfNames) {
		// aktualne zpracovavam property - POZOR: strukturovany atribut se od entity lisi pouze tim, ze ma morfismus pouze jednim smerem, zatimco entita ma morfismy obema smery!
		var value = (AbstractRecordProperty) structuredProperty.getValue();		// ted mas i value, kterou ale musi byt embedded document, a tedy abstractRecord
		var object = instance.get(structuredProperty.getName()); // ted mas i zpracovavany objekt
		var valueSuperid = value.getIdentifier();

		addMapping(instance, morphismName(parentObject.getName(), object.getName()), parentSuperid, valueSuperid);

		queue.add(value);
		queueOfNames.add(structuredProperty.getName());
	}

	private void processRecord(AbstractInstance instance, AbstractCategoricalObject parentObject, AbstractIdentifier parentSuperid, AbstractRecordProperty recordProperty, String recordName, Queue<AbstractRecordProperty> queue, Queue<String> queueOfNames) {
		System.out.println("PARENT_OBJECT_CLASS" + parentObject.getClass() + " :: " + parentObject.getName());
		var object = instance.get(recordName); // ted mas i zpracovavany objekt

		var valueSuperid = recordProperty.getIdentifier();

		addMapping(instance, morphismName(parentObject.getName(), object.getName()), parentSuperid, valueSuperid);
		addMapping(instance, morphismName(object.getName(), parentObject.getName()), valueSuperid, parentSuperid);

		queue.add(recordProperty);
		queueOfNames.add(recordProperty.getName());
	}

	private void processMultiAttribute(AbstractInstance instance, AbstractCategoricalObject parentObject, AbstractIdentifier parentSuperid, AbstractArrayProperty arrayProperty) {
		var elements = (Iterable<? extends AbstractProperty>) arrayProperty.getElements();

		var arrayObject = instance.get(arrayProperty.getName());

		for (var element : elements) {
			System.out.println("ADDING ELEMENT " + elements + " TO " + arrayObject.getName());
			arrayObject.add(element);
			LOGGER.log(Level.INFO, String.format("\t\tAdded value %s to multi_attribute domain %s", element, arrayProperty.getName()));
			addMapping(instance, morphismName(parentObject.getName(), element.getName()), parentSuperid, element.getValue());
		}
	}

	private void processArray(AbstractInstance instance, AbstractCategoricalObject parentObject, AbstractIdentifier parentSuperid, AbstractArrayProperty arrayProperty, Queue<AbstractRecordProperty> queue, Queue<String> queueOfNames) {
		var elements = (Iterable<? extends AbstractProperty>) arrayProperty.getElements();

		AbstractCategoricalObject arrayObject = instance.get(arrayProperty.getName());

		int size = 0;
		for (var element : elements) {
			AbstractCategoricalObject elementObject = instance.get(element.getName());
			LOGGER.log(Level.INFO, String.format("\t\tZpracovavas %s prvek (%s,%s) pole (%s, %s)", size, elementObject.getName(), elementObject.getType(), arrayProperty.getName(), arrayProperty.getType()));
			switch (elementObject.getType()) {
				case KIND -> {
					// TAHLE SITUACE BY NEMELA NASTAT... jinak se resi stejne jako RECORD
					LOGGER.log(Level.SEVERE, "\t\tNESMI NASTAT -> KIND!");
				}
				case RECORD -> {
					AbstractRecordProperty recordElement = (AbstractRecordProperty) element;
					AbstractIdentifier arraySuperid = new SimpleIdentifier(parentSuperid, recordElement.getIdentifier());
					arrayObject.add(arraySuperid);

					processRecord(instance, arrayObject, arraySuperid, recordElement, recordElement.getName(), queue, queueOfNames);

					// mapovani parentObject -> arrayObject (ale ne arrayElementObject!)
					addMapping(instance, morphismName(parentObject.getName(), arrayObject.getName()), parentSuperid, arraySuperid);
					addMapping(instance, morphismName(arrayObject.getName(), parentObject.getName()), arraySuperid, parentSuperid);
				}
				case ARRAY -> {
					processArray(instance, parentObject, parentSuperid, (AbstractArrayProperty) element, queue, queueOfNames);
				}
				// WARN: nasledujici pripady nesmi nastat, event. mohou nastat v pripade pole poli, ale to jeste over!
				case INLINED -> {
					LOGGER.log(Level.SEVERE, "\t\tINLINED TODO");
					// pole inlined take nesmi nastat, protoze by to melo byt parovani 1:1
				}
				case ATTRIBUTE -> {
					LOGGER.log(Level.SEVERE, "\t\tNESMI NASTAT -> ATTRIBUTE!");
//					processAttribute(instance, instance.get(arrayProperty.getName()), parentSuperid, (AbstractAttributeProperty) element);
					// nahrazeni za multi-attribute
				}
				case MULTI_ATTRIBUTE -> {
					LOGGER.log(Level.SEVERE, "\t\tNESMI NASTAT -> MULTI_ATTRIBUTE!");
//					processAttribute(instance, instance.get(arrayProperty.getName()), parentSuperid, (AbstractAttributeProperty) element);
					// nahrazeni za multi-attribute
				}
				case INLINED_ATTRIBUTE -> {
					LOGGER.log(Level.SEVERE, "\t\tNESMI NASTAT -> ATTRIBUTE!");
//					processAttribute(instance, instance.get(arrayProperty.getName()), parentSuperid, (AbstractAttributeProperty) element);
					// nahrazeni za multi-attribute
				}
				case STRUCTURED_ATTRIBUTE -> {
					LOGGER.log(Level.SEVERE, "\t\tNESMI NASTAT -> STRUCTURED_ATTRIBUTE!");
//					processStructuredAttribute(instance, parentObject, parentSuperid, (AbstractAttributeProperty) element, queue, queueOfNames);
				}
				case INLINED_STRUCTURED_ATTRIBUTE -> {
					LOGGER.log(Level.SEVERE, "\t\tNESMI NASTAT -> STRUCTURED_ATTRIBUTE!");
//					processStructuredAttribute(instance, parentObject, parentSuperid, (AbstractAttributeProperty) element, queue, queueOfNames);
				}
				case IDENTIFIER -> {
					LOGGER.log(Level.SEVERE, "\t\tNESMI NASTAT -> IDENTIFIER!");
//					processAttribute(instance, parentObject, parentSuperid, (AbstractAttributeProperty) element);
				}
				case MULTI_IDENTIFIER -> {
					LOGGER.log(Level.SEVERE, "\t\tNESMI NASTAT -> IDENTIFIER!");
//					processAttribute(instance, parentObject, parentSuperid, (AbstractAttributeProperty) element);
				}
				case REFERENCE -> {
					LOGGER.log(Level.SEVERE, "\t\tNESMI NASTAT -> REFERENCE!");
//					processReference();
				}
				case MULTI_REFERENCE -> {
					LOGGER.log(Level.SEVERE, "\t\tNESMI NASTAT -> MULTI_REFERENCE!");
//					processReference();
				}
			}
			++size;
		}

		if (size == 0) {
			LOGGER.log(Level.WARNING, "\t\tPole bylo prazdne, nic nedelas");
			// TODO: Co kdyz bylo pole prazdne? Asi nic nedelame...
		}
	}

	private void processAttribute(AbstractInstance instance, AbstractCategoricalObject entity, AbstractIdentifier superid, AbstractAttributeProperty attribute) {
		var attributeObject = instance.get(attribute.getName());
		attributeObject.add(attribute.getValue());
		LOGGER.log(Level.INFO, String.format("\t\tAdded value %s to attribute domain %s", attribute.getValue(), attribute.getName()));
		addMapping(instance, morphismName(entity.getName(), attribute.getName()), superid, attribute.getValue());
	}

	private void processInlined() {
		LOGGER.log(Level.SEVERE, "\t\tINLINED TODO");
	}

	private void processReference() {
		LOGGER.log(Level.SEVERE, "NEMELO BY NASTAVAT!");
	}

	private void addMapping(AbstractInstance instance, String name, AbstractValue domainValue, AbstractValue codomainValue) {
		LOGGER.log(Level.SEVERE, name);
		AbstractCategoricalMorphism morphism = instance.getMorphism(name);
		morphism.add(domainValue, codomainValue);
		LOGGER.log(Level.SEVERE, String.format("\t\tAdded mappings (%s, %s) TO %s", domainValue, codomainValue, morphism));
	}

}
