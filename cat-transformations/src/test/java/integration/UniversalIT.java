/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integration;

import cat.transformations.Main;
import cat.transformations.algorithms.Algorithms;
import cat.transformations.algorithms.TransformationDocToInst;
import cat.transformations.algorithms.TransformationInstToDoc;
import cat.transformations.algorithms2.TransformationModelToInst;
import cat.transformations.algorithms2.model.AbstractInstance;
import cat.transformations.algorithms2.model.AbstractModel;
import cat.transformations.algorithms2.model.AbstractType;
import cat.transformations.algorithms2.model.CategoricalInstance;
import cat.transformations.algorithms2.model.DocumentModel;
import cat.transformations.category.InstanceCategory;
import cat.transformations.commons.Constants;
import cat.transformations.model.AbstractTable;
import cat.transformations.model.CSVTable;
import cat.transformations.model.RelationalInstance;
import cat.transformations.wrappers.DocumentWrapper;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author pavel.koupil
 */
@RunWith(MockitoJUnitRunner.class)
public class UniversalIT {

	private static final Logger LOGGER = Logger.getLogger(UniversalIT.class.getName());

	static {
		InputStream stream = UniversalIT.class.getClassLoader().getResourceAsStream("logging.properties");
		try {
			LogManager.getLogManager().readConfiguration(stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public UniversalIT() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {

	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
		try ( var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");
			database.getCollection("cars").drop();
		}
	}

	private void printTestHeader(String text) {
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

	@Test
//	@Ignore
	public void testArrayOfRecords() {
		printTestHeader("UniversalIT -> ARRAY OF RECORDS");
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
			category.create("array.items", AbstractType.NESTED_KIND);
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
	
	@Test
//	@Ignore
	public void testArrayOfAttributes() {
		printTestHeader("UniversalIT -> testArrayOfAttributes()");
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

}
