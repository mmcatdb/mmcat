/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms;

import cat.transformations.category.AttributeMorphism;
import cat.transformations.category.AttributeObject;
import cat.transformations.category.CategoricalObject;
import cat.transformations.category.EntityObject;
import cat.transformations.category.InstanceCategory;
import cat.transformations.category.RelationshipMorphism;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import org.bson.Document;

/**
 *
 * @author pavel.contos
 */
public class TransformationDocToInst {

	public InstanceCategory process(MongoDatabase database) {
		InstanceCategory result = new InstanceCategory();

// ----- BEGINNING -------------------------------------------------------------
		// for each collection
		Queue<Document> queue = new LinkedList<>();
		Queue<String> queueOfNames = new LinkedList<>();

		for (String collectionName : database.listCollectionNames()) {
			MongoCollection<Document> collection = database.getCollection(collectionName);

			try (MongoCursor<Document> cursor = collection.find().iterator()) {
				while (cursor.hasNext()) {
					Document document = cursor.next();
					queue.add(document);
					queueOfNames.add(collectionName);
					while (!queue.isEmpty()) {
						Document doc = queue.poll();
						String name = queueOfNames.poll();
						// identifier
						var sid = doc.get("_id");
						if (sid == null) {
							sid = doc.get(name+"_id");
						}
						EntityObject entity = result.getOrCreateEntity(name);
						entity.addValue(sid);
						// vyber rychlejsi, bud Map.Entry nebo keySet
//                        for (String propertyName : doc.keySet()) {}
						for (Map.Entry<String, Object> property : doc.entrySet()) {
							if (property.getValue() instanceof List) {
								// NOTE: mame array
								processArray(result, property.getKey(), (List) property.getValue(), entity, sid, queue, queueOfNames);
							} else if (property.getValue() instanceof Document) {
								// NOTE: mame record
								processRecord(result, property.getKey(), (Document) property.getValue(), entity, sid, queue, queueOfNames);
							} else {
								// NOTE: mame simple type
								processAttribute(result, property.getKey(), property.getValue(), entity, sid);
							}

						}
					}
				}
			}

		}
// ----- END -------------------------------------------------------------------
		return result;
	}

	private void processAttribute(InstanceCategory result, String key, Object value, EntityObject entity, Object sid) {
		AttributeObject attribute = result.getOrCreateAttribute(key);
		attribute.add(value);
		AttributeMorphism morphism = result.getOrCreateAttributeMorphism(entity.getName() + "->" + attribute.getName(), entity, attribute);
		morphism.addMapping(sid, value);
	}

	private void processRecord(InstanceCategory result, String key, Document document, EntityObject entity, Object sid, Queue<Document> queue, Queue<String> queueOfNames) {
		EntityObject object = result.getOrCreateEntity(key);

		var embeddedSID = document.get("eid");
		if (embeddedSID == null) {
			embeddedSID = System.currentTimeMillis() + "TODO-EMBEDDED_ID!";
			// tady bys mel modifikovat soubor a vytvorit sid? nezmeni to puvodni soubor?
			document.append(key+"_id", embeddedSID);
//			System.out.println("CREATED EMBEDDED SID: " + embeddedSID);
		}
		// tady vkladas jen mapping, value jde az pozdeji
		RelationshipMorphism morphism = result.getOrCreateRelationshipMorphism(entity.getName() + "->" + object.getName(), entity, object);
		morphism.addMapping(sid, embeddedSID);
		queue.add(document);
		queueOfNames.add(key);
	}

	private void processArray(InstanceCategory result, String key, List array, EntityObject entity, Object sid, Queue<Document> queue, Queue<String> queueOfNames) {
		if (array.isEmpty()) {
			// TODO: co delat s prazdnym polem?
			return;
		} else if (array.get(0) instanceof List) {
			// TODO: co delat s polem poli?
			for (var element : array) {
				processArray(result, key, (List) element, entity, sid, queue, queueOfNames);
			}
		} else if (array.get(0) instanceof Document) {
			for (var element : array) {
				processRecord(result, key, (Document) element, entity, sid, queue, queueOfNames);
			}
		} else {
			for (var element : array) {
				processAttribute(result, key, element, entity, sid);
			}
		}
	}

}
