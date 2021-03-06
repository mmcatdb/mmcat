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

	private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

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

	private static void printTestHeader(String text) {
		StringBuilder builder = new StringBuilder();
		builder.append(Constants.ANSI_BLUE);
		builder.append("------------------------------------------------------------------------------------------------------------------------");
		builder.append("\n");
		builder.append("\t");
		builder.append(text);
		builder.append("\n");
		builder.append("------------------------------------------------------------------------------------------------------------------------");
		builder.append("\n");
		builder.append(Constants.ANSI_RESET);
		System.out.println(builder);
	}

	public static void demoTest() {
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

	public static void testArrayOfRecords() {
		printTestHeader("Main -> testArrayOfRecords()");
		try ( var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");

			try {

				database.createCollection("cars");
			} catch (MongoCommandException e) {
				database.getCollection("cars").drop();
			}

			var docs = new ArrayList<Document>();

			MongoCollection<Document> collection = database.getCollection("cars");

			var nestedDocumentA = new Document();
			nestedDocumentA.append("a", "111");
			nestedDocumentA.append("b", "222");
			nestedDocumentA.append("c", "333");

			var nestedDocumentB = new Document();
			nestedDocumentB.append("a", "444");
			nestedDocumentB.append("b", "555");
			nestedDocumentB.append("c", "666");

			var nestedDocumentC = new Document();
			nestedDocumentC.append("a", "777");
			nestedDocumentC.append("b", "888");
			nestedDocumentC.append("c", "999");

			List<Document> array = new ArrayList<>();
			array.add(nestedDocumentA);
			array.add(nestedDocumentB);
			array.add(nestedDocumentC);

			var document = new Document("_id", 8);
			document.append("name", "Volkswagen");
			document.append("price", 21600);
			document.append("array", array);

			docs.add(document);

			collection.insertMany(docs);

			AbstractModel model = new DocumentModel();

			DocumentWrapper wrapper = new DocumentWrapper();
			wrapper.wrap(database, model);

			System.out.println(model);

			AbstractInstance category = new CategoricalInstance();
			category.create("cars", AbstractType.KIND);
			category.create("_id", AbstractType.IDENTIFIER);
			category.create("name", AbstractType.ATTRIBUTE);
			category.create("price", AbstractType.ATTRIBUTE);
			category.create("array", AbstractType.ARRAY);
			category.create("array.items", AbstractType.RECORD);
//			category.create("items.att", AbstractType.ATTRIBUTE);
//			category.create("test", AbstractType.RECORD);
			category.create("a", AbstractType.ATTRIBUTE);
			category.create("b", AbstractType.ATTRIBUTE);
			category.create("c", AbstractType.ATTRIBUTE);
			category.createMorphism(TransformationModelToInst.morphismName("cars", "_id"), category.get("cars"), category.get("_id"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "name"), category.get("cars"), category.get("name"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "price"), category.get("cars"), category.get("price"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "array"), category.get("cars"), category.get("array"));
			category.createMorphism(TransformationModelToInst.morphismName("array", "cars"), category.get("array"), category.get("cars"));
			category.createMorphism(TransformationModelToInst.morphismName("array", "array.items"), category.get("array"), category.get("array.items"));
			category.createMorphism(TransformationModelToInst.morphismName("array.items", "array"), category.get("array.items"), category.get("array"));
//			category.createMorphism(TransformationModelToInst.morphismName("items.items", "items.att"), category.get("items.items"), category.get("items.att"));
//			category.createMorphism(TransformationModelToInst.morphismName("cars", "test"), category.get("cars"), category.get("test"));
//			category.createMorphism(TransformationModelToInst.morphismName("test", "cars"), category.get("test"), category.get("cars"));
			category.createMorphism(TransformationModelToInst.morphismName("array.items", "a"), category.get("array.items"), category.get("a"));
			category.createMorphism(TransformationModelToInst.morphismName("array.items", "b"), category.get("array.items"), category.get("b"));
			category.createMorphism(TransformationModelToInst.morphismName("array.items", "c"), category.get("array.items"), category.get("c"));

			TransformationModelToInst transformation = new TransformationModelToInst();
			transformation.process(model, category);

			System.out.println(category);

		}

	}

	public static void testArrayOfAttributes() {
		printTestHeader("Main -> testArrayOfAttributes()");
		try ( var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");

			try {

				database.createCollection("cars");
			} catch (MongoCommandException e) {
				database.getCollection("cars").drop();
			}

			var docs = new ArrayList<Document>();

			MongoCollection<Document> collection = database.getCollection("cars");

			List<String> array = new ArrayList<>();
			array.add("value1DUPL");
			array.add("value2");
			array.add("value3");
			array.add("value1DUPL");

			var document = new Document("_id", 1);
			document.append("name", "Skoda");
			document.append("price", 21600);
			document.append("multiAttribute", array);

			docs.add(document);

			collection.insertMany(docs);

			AbstractModel model = new DocumentModel();

			DocumentWrapper wrapper = new DocumentWrapper();
			wrapper.wrap(database, model);

			System.out.println(model);

			AbstractInstance category = new CategoricalInstance();
			category.create("cars", AbstractType.KIND);
			category.create("_id", AbstractType.IDENTIFIER);
			category.create("name", AbstractType.ATTRIBUTE);
			category.create("price", AbstractType.ATTRIBUTE);
			category.create("multiAttribute", AbstractType.MULTI_ATTRIBUTE);
//			category.create("array.items", AbstractType.RECORD);
//			category.create("items.att", AbstractType.ATTRIBUTE);
//			category.create("test", AbstractType.RECORD);
//			category.create("a", AbstractType.ATTRIBUTE);
//			category.create("b", AbstractType.ATTRIBUTE);
//			category.create("c", AbstractType.ATTRIBUTE);
			category.createMorphism(TransformationModelToInst.morphismName("cars", "_id"), category.get("cars"), category.get("_id"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "name"), category.get("cars"), category.get("name"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "price"), category.get("cars"), category.get("price"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "multiAttribute"), category.get("cars"), category.get("multiAttribute"));
//			category.createMorphism(TransformationModelToInst.morphismName("array", "cars"), category.get("array"), category.get("cars"));
//			category.createMorphism(TransformationModelToInst.morphismName("array", "array.items"), category.get("array"), category.get("array.items"));
//			category.createMorphism(TransformationModelToInst.morphismName("array.items", "array"), category.get("array.items"), category.get("array"));
//			category.createMorphism(TransformationModelToInst.morphismName("items.items", "items.att"), category.get("items.items"), category.get("items.att"));
//			category.createMorphism(TransformationModelToInst.morphismName("cars", "test"), category.get("cars"), category.get("test"));
//			category.createMorphism(TransformationModelToInst.morphismName("test", "cars"), category.get("test"), category.get("cars"));
//			category.createMorphism(TransformationModelToInst.morphismName("array.items", "a"), category.get("array.items"), category.get("a"));
//			category.createMorphism(TransformationModelToInst.morphismName("array.items", "b"), category.get("array.items"), category.get("b"));
//			category.createMorphism(TransformationModelToInst.morphismName("array.items", "c"), category.get("array.items"), category.get("c"));

			System.out.println(category);

			TransformationModelToInst transformation = new TransformationModelToInst();
			transformation.process(model, category);

			System.out.println(category);

		}

	}

	public static void testStructuredAttribute() {
		printTestHeader("Main -> testStructuredAttribute()");
		try ( var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");

			try {

				database.createCollection("cars");
			} catch (MongoCommandException e) {
				database.getCollection("cars").drop();
			}

			var docs = new ArrayList<Document>();

			MongoCollection<Document> collection = database.getCollection("cars");

			Document address = new Document();
			address.append("street", "Rovna 1333");
			address.append("city", "Zelivec");
			address.append("postalcode", 25168);

			var document = new Document("_id", 1);
			document.append("name", "Skoda");
			document.append("price", 21600);
			document.append("address", address);

			docs.add(document);

			collection.insertMany(docs);

			AbstractModel model = new DocumentModel();

			DocumentWrapper wrapper = new DocumentWrapper();
			wrapper.wrap(database, model);

			System.out.println(model);

			AbstractInstance category = new CategoricalInstance();
			category.create("cars", AbstractType.KIND);
			category.create("_id", AbstractType.IDENTIFIER);
			category.create("name", AbstractType.ATTRIBUTE);
			category.create("price", AbstractType.ATTRIBUTE);
			category.create("address", AbstractType.STRUCTURED_ATTRIBUTE);
//			category.create("array.items", AbstractType.RECORD);
//			category.create("items.att", AbstractType.ATTRIBUTE);
//			category.create("test", AbstractType.RECORD);
			category.create("street", AbstractType.ATTRIBUTE);
			category.create("city", AbstractType.ATTRIBUTE);
			category.create("postalcode", AbstractType.ATTRIBUTE);
			category.createMorphism(TransformationModelToInst.morphismName("cars", "_id"), category.get("cars"), category.get("_id"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "name"), category.get("cars"), category.get("name"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "price"), category.get("cars"), category.get("price"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "address"), category.get("cars"), category.get("address"));
//			category.createMorphism(TransformationModelToInst.morphismName("array", "cars"), category.get("array"), category.get("cars"));
//			category.createMorphism(TransformationModelToInst.morphismName("array", "array.items"), category.get("array"), category.get("array.items"));
//			category.createMorphism(TransformationModelToInst.morphismName("array.items", "array"), category.get("array.items"), category.get("array"));
//			category.createMorphism(TransformationModelToInst.morphismName("items.items", "items.att"), category.get("items.items"), category.get("items.att"));
//			category.createMorphism(TransformationModelToInst.morphismName("cars", "test"), category.get("cars"), category.get("test"));
//			category.createMorphism(TransformationModelToInst.morphismName("test", "cars"), category.get("test"), category.get("cars"));
			category.createMorphism(TransformationModelToInst.morphismName("address", "street"), category.get("address"), category.get("street"));
			category.createMorphism(TransformationModelToInst.morphismName("address", "city"), category.get("address"), category.get("city"));
			category.createMorphism(TransformationModelToInst.morphismName("address", "postalcode"), category.get("address"), category.get("postalcode"));

			System.out.println(category);

			TransformationModelToInst transformation = new TransformationModelToInst();
			transformation.process(model, category);

			System.out.println(category);

		}

	}

	public static void testNestedDocument1_1() {
		printTestHeader("Main -> testStructuredAttribute()");
		try ( var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");

			try {

				database.createCollection("cars");
			} catch (MongoCommandException e) {
				database.getCollection("cars").drop();
			}

			var docs = new ArrayList<Document>();

			MongoCollection<Document> collection = database.getCollection("cars");

			Document address = new Document();
			address.append("street", "Rovna 1333");
			address.append("city", "Zelivec");
			address.append("postalcode", 25168);

			var document = new Document("_id", 1);
			document.append("name", "Skoda");
			document.append("price", 21600);
			document.append("address", address);

			docs.add(document);

			collection.insertMany(docs);

			AbstractModel model = new DocumentModel();

			DocumentWrapper wrapper = new DocumentWrapper();
			wrapper.wrap(database, model);

			System.out.println(model);

			AbstractInstance category = new CategoricalInstance();
			category.create("cars", AbstractType.KIND);
			category.create("_id", AbstractType.IDENTIFIER);
			category.create("name", AbstractType.ATTRIBUTE);
			category.create("price", AbstractType.ATTRIBUTE);
			category.create("address", AbstractType.STRUCTURED_ATTRIBUTE);
//			category.create("array.items", AbstractType.RECORD);
//			category.create("items.att", AbstractType.ATTRIBUTE);
//			category.create("test", AbstractType.RECORD);
			category.create("street", AbstractType.ATTRIBUTE);
			category.create("city", AbstractType.ATTRIBUTE);
			category.create("postalcode", AbstractType.ATTRIBUTE);
			category.createMorphism(TransformationModelToInst.morphismName("cars", "_id"), category.get("cars"), category.get("_id"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "name"), category.get("cars"), category.get("name"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "price"), category.get("cars"), category.get("price"));
			category.createMorphism(TransformationModelToInst.morphismName("cars", "address"), category.get("cars"), category.get("address"));
//			category.createMorphism(TransformationModelToInst.morphismName("array", "cars"), category.get("array"), category.get("cars"));
//			category.createMorphism(TransformationModelToInst.morphismName("array", "array.items"), category.get("array"), category.get("array.items"));
//			category.createMorphism(TransformationModelToInst.morphismName("array.items", "array"), category.get("array.items"), category.get("array"));
//			category.createMorphism(TransformationModelToInst.morphismName("items.items", "items.att"), category.get("items.items"), category.get("items.att"));
//			category.createMorphism(TransformationModelToInst.morphismName("cars", "test"), category.get("cars"), category.get("test"));
//			category.createMorphism(TransformationModelToInst.morphismName("test", "cars"), category.get("test"), category.get("cars"));
			category.createMorphism(TransformationModelToInst.morphismName("address", "street"), category.get("address"), category.get("street"));
			category.createMorphism(TransformationModelToInst.morphismName("address", "city"), category.get("address"), category.get("city"));
			category.createMorphism(TransformationModelToInst.morphismName("address", "postalcode"), category.get("address"), category.get("postalcode"));

			System.out.println(category);

			TransformationModelToInst transformation = new TransformationModelToInst();
			transformation.process(model, category);

			System.out.println(category);

		}

	}

	public static void main(String... args) {
//		Main.demoTest();
//		Main.testArrayOfRecords();
//		Main.testArrayOfAttributes();
		Main.testStructuredAttribute();
		Main.testNestedDocument1_1();
	}

}
