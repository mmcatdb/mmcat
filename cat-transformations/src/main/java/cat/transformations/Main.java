/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations;

import cat.transformations.algorithms2.TransformationModelToInst;
import cat.transformations.algorithms2.model.AbstractInstance;
import cat.transformations.algorithms2.model.AbstractKind;
import cat.transformations.algorithms2.model.AbstractModel;
import cat.transformations.algorithms2.model.AbstractProperty;
import cat.transformations.algorithms2.model.AbstractRecord;
import cat.transformations.algorithms2.model.CategoricalInstance;
import cat.transformations.algorithms2.model.DocumentKind;
import cat.transformations.algorithms2.model.DocumentModel;
import cat.transformations.algorithms2.model.DocumentProperty;
import cat.transformations.algorithms2.model.DocumentRecord;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;

/**
 *
 * @author pavel.koupil
 */
public class Main {

	public static void setup(MongoDatabase database) {

		try {

			database.createCollection("cars");
		} catch (MongoCommandException e) {

			database.getCollection("cars").drop();
		}

		var docs = new ArrayList<Document>();

		MongoCollection<Document> collection = database.getCollection("cars");

		var d1 = new Document("_id", 1);
		d1.append("name", "Audi");
		d1.append("price", 52642);
		docs.add(d1);

		var d2 = new Document("_id", 2);
		d2.append("name", "Mercedes");
		d2.append("price", 57127);
		docs.add(d2);

		var d3 = new Document("_id", 3);
		d3.append("name", "Skoda");
		d3.append("price", 9000);
		docs.add(d3);

		var d4 = new Document("_id", 4);
		d4.append("name", "Volvo");
		d4.append("price", 29000);
		docs.add(d4);

		var d5 = new Document("_id", 5);
		d5.append("name", "Bentley");
		d5.append("price", 350000);
		docs.add(d5);

		var d6 = new Document("_id", 6);
		d6.append("name", "Citroen");
		d6.append("price", 21000);
		docs.add(d6);

		var d7 = new Document("_id", 7);
		d7.append("name", "Hummer");
		d7.append("price", 41400);

		List<Object> items = new ArrayList<>();
		items.add(10);
		items.add(11);
		items.add(12);
		items.add(13);
		items.add(14);
		d7.append("items", items);

		docs.add(d7);

		var d8 = new Document("_id", 8);
		d8.append("name", "Volkswagen");
		d8.append("price", 21600);

		var d8test = new Document();
		d8test.append("a", "AAA");
		d8test.append("b", "BBB");
		d8test.append("c", "CCC");
		d8.append("test", d8test);

		docs.add(d8);

		collection.insertMany(docs);

	}

	public static void main(String... args) {

		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);

		try (var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");
			Main.setup(database);

			AbstractModel model = new DocumentModel();

			// list collections
			for (String name : database.listCollectionNames()) {
				System.out.println(name);
				AbstractKind kind = new DocumentKind(name);

				// cursor
				MongoCollection<Document> collection = database.getCollection(name);
				try (MongoCursor<Document> cur = collection.find().iterator()) {
					while (cur.hasNext()) {
						var doc = cur.next();

						AbstractRecord record = new DocumentRecord();
						// ted preved vsechny property a nacpi je do dokumentu

						for (Map.Entry<String, Object> property : doc.entrySet()) {
							if (property.getValue() instanceof List) {
								// NOTE: mame array
								processArray(record, property.getKey(), (List) property.getValue());
							} else if (property.getValue() instanceof Document) {
								// NOTE: mame record
								processRecord(record);
							} else {
								// NOTE: mame simple type
								processAttribute(record, property.getKey(), property.getValue());
							}

						}

						kind.add(record);

					}
				}
				model.putKind("cars", kind);
			}

			TransformationModelToInst transformation = new TransformationModelToInst();

			AbstractInstance category = new CategoricalInstance();

			transformation.process(model, category);
		}

	}

	private static void processAttribute(AbstractRecord parent, String name, Object value) {
		AbstractProperty property = new DocumentProperty(name, value, false, false, false);
		parent.putProperty(name, property);
	}

	private static void processRecord(AbstractRecord parent) {
//
//		// record muze obsahovat property slozitych typu... takze opet zanorovani a volani sebe sama
//		EntityObject object = result.getOrCreateEntity(key);
//
//		var embeddedSID = document.get("eid");
//		if (embeddedSID == null) {
//			embeddedSID = System.currentTimeMillis() + "TODO-EMBEDDED_ID!";
//			// tady bys mel modifikovat soubor a vytvorit sid? nezmeni to puvodni soubor?
//			document.append(key + "_id", embeddedSID);
////			System.out.println("CREATED EMBEDDED SID: " + embeddedSID);
//		}
//		// tady vkladas jen mapping, value jde az pozdeji
//		RelationshipMorphism morphism = result.getOrCreateRelationshipMorphism(entity.getName() + "->" + object.getName(), entity, object);
//		morphism.addMapping(sid, embeddedSID);
//		queue.add(document);
//		queueOfNames.add(key);
	}

	private static void processArray(AbstractRecord parent, String name, List array) {
		if (array.isEmpty()) {
			parent.putProperty(name, null);	// TODO: neni dobre, mas vkladat prazdne pole!
			// TODO: chybi ti AbstractArray...
			// TODO: co delat s prazdnym polem?
			System.out.println("TODO: processArray - empty array");
		} else if (array.get(0) instanceof List) {
			// TODO: co delat s polem poli?
//			for (var element : array) {
//				processArray(result, key, (List) element, entity, sid, queue, queueOfNames);
//			}
			System.out.println("TODO: processArray - nested array");
		} else if (array.get(0) instanceof Document) {
//			for (var element : array) {
//				processRecord(result, key, (Document) element, entity, sid, queue, queueOfNames);
//			}
			System.out.println("TODO: processArray - array of documents");
		} else {
//			for (var element : array) {
//				processAttribute(result, key, element, entity, sid);
//			}
			System.out.println("TODO: processArray - array of properties");
		}
	}

}
