/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations;

import cat.transformations.algorithms2.TransformationModelToInst;
import cat.transformations.algorithms2.model.AbstractInstance;
import cat.transformations.algorithms2.model.AbstractModel;
import cat.transformations.algorithms2.model.CategoricalInstance;
import cat.transformations.algorithms2.model.DocumentModel;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bson.Document;
import cat.transformations.algorithms2.model.AbstractType;
import cat.transformations.commons.Constants;
import cat.transformations.wrappers.DocumentWrapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 *
 * @author pavel.koupil
 */
public class Main {

	private static final Logger LOGGER = Logger.getLogger(TransformationModelToInst.class.getName());

	static {
		InputStream stream = Main.class.getClassLoader().getResourceAsStream("logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setup(MongoDatabase database) {

//		LOGGER.info(Constants.ANSI_BLUE + "TEST" + Constants.ANSI_RESET);
		try {

			database.createCollection("cars");
		} catch (MongoCommandException e) {

			database.getCollection("cars").drop();
		}

		var docs = new ArrayList<Document>();

		MongoCollection<Document> collection = database.getCollection("cars");

//		var d1 = new Document("_id", 1);
//		d1.append("name", "Audi");
//		d1.append("price", 52642);
//		docs.add(d1);
//
//		var d2 = new Document("_id", 2);
//		d2.append("name", "Mercedes");
//		d2.append("price", 57127);
//		docs.add(d2);
//
//		var d3 = new Document("_id", 3);
//		d3.append("name", "Skoda");
//		d3.append("price", 9000);
//		docs.add(d3);
//
//		var d4 = new Document("_id", 4);
//		d4.append("name", "Volvo");
//		d4.append("price", 29000);
//		docs.add(d4);
//
//		var d5 = new Document("_id", 5);
//		d5.append("name", "Bentley");
//		d5.append("price", 350000);
//		docs.add(d5);
//
//		var d6 = new Document("_id", 6);
//		d6.append("name", "Citroen");
//		d6.append("price", 21000);
//		docs.add(d6);
//		
		var d8 = new Document("_id", 8);
		d8.append("name", "Volkswagen");
		d8.append("price", 21600);

		var d8test = new Document();
		d8test.append("a", "AAA");
		d8test.append("b", "BBB");
		d8test.append("c", "CCC");
		d8.append("test", d8test);

		docs.add(d8);
		
		
		
		
		
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

		
		collection.insertMany(docs);

	}

	public static void main(String... args) {

		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);

		try ( var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");
			Main.setup(database);

			AbstractModel model = new DocumentModel();

			DocumentWrapper wrapper = new DocumentWrapper();
			wrapper.wrap(database, model);

			TransformationModelToInst transformation = new TransformationModelToInst();

			AbstractInstance category = new CategoricalInstance();
			category.create("cars", AbstractType.KIND);
			category.create("_id", AbstractType.IDENTIFIER);
			category.create("name", AbstractType.ATTRIBUTE);
			category.create("price", AbstractType.ATTRIBUTE);
			category.create("items", AbstractType.ARRAY);
			category.create("items.items", AbstractType.RECORD);
			category.create("items.att", AbstractType.ATTRIBUTE);
			category.create("test", AbstractType.RECORD);
			category.create("a", AbstractType.ATTRIBUTE);
			category.create("b", AbstractType.ATTRIBUTE);
			category.create("c", AbstractType.ATTRIBUTE);
			category.createMorphism(TransformationModelToInst.morphismName("cars", "_id"), category.get("cars"), category.get("_id"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "name"), category.get("cars"), category.get("name"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "price"), category.get("cars"), category.get("price"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "items"), category.get("cars"), category.get("items"));
			category.createMorphism(TransformationModelToInst.morphismName("items", "cars"), category.get("items"), category.get("cars"));
			category.createMorphism(TransformationModelToInst.morphismName("items", "items.items"), category.get("items"), category.get("items.items"));
			category.createMorphism(TransformationModelToInst.morphismName("items.items", "items"), category.get("items.items"), category.get("items"));
			category.createMorphism(TransformationModelToInst.morphismName("items.items", "items.att"), category.get("items.items"), category.get("items.att"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "test"), category.get("cars"), category.get("test"));
			category.createMorphism(TransformationModelToInst.morphismName("test", "cars"), category.get("test"), category.get("cars"));
			category.createMorphism(TransformationModelToInst.morphismName("test", "a"), category.get("test"), category.get("a"));
			category.createMorphism(TransformationModelToInst.morphismName("test", "b"), category.get("test"), category.get("b"));
			category.createMorphism(TransformationModelToInst.morphismName("test", "c"), category.get("test"), category.get("c"));

			System.out.println(model);

			transformation.process(model, category);

			System.out.println(category);
		}

	}

}
