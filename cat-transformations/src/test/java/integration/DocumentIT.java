/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package integration;

import cat.transformations.algorithms.Algorithms;
import cat.transformations.algorithms.TransformationDocToInst;
import cat.transformations.algorithms.TransformationInstToDoc;
import cat.transformations.category.InstanceCategory;
import cat.transformations.commons.Constants;
import cat.transformations.model.AbstractTable;
import cat.transformations.model.CSVTable;
import cat.transformations.model.RelationalInstance;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Assert;
import org.bson.Document;
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
public class DocumentIT {

//    private static final Logger LOGGER = LoggerFactory.getLogger(CidServiceTest.class);
//    @InjectMocks
//    private final CidService service = new CidService();
//    @Mock
//    private RepositoryService repository;
//    @Mock
//    private VersionValidator versionValidator;
	public DocumentIT() {
	}

	@BeforeClass
	public static void setUpClass() {

		Logger mongoLogger = Logger.getLogger("org.mongodb.driver");
		mongoLogger.setLevel(Level.SEVERE);

		try (var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");

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

	}

	@AfterClass
	public static void tearDownClass() {

		try (var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");
			database.getCollection("cars").drop();
		}
	}

	@Before
	public void setUp() {
	}

//    @After
	public void tearDown() {
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
	public void demo() {
		printTestHeader("DocumentIT -> Demo");
		try (var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");
			// list collections
			for (String name : database.listCollectionNames()) {
				System.out.println(name);

				// cursor
				MongoCollection<Document> collection = database.getCollection(name);
				try (MongoCursor<Document> cur = collection.find().iterator()) {
					while (cur.hasNext()) {
						var doc = cur.next();
						System.out.println(doc);
//				var cars = new ArrayList<>(doc.values());
//				if (doc.containsKey("test")) {
//					for (String key : doc.keySet()) {
//						System.out.println(key + " :: " + doc.get(key) + " :: " + doc.get(key).getClass());
//					}
//				}
					}
				}
			}

//
//		// query
//		var query = new BasicDBObject("price", new BasicDBObject("$gt", 30000));
//		collection.find(query).forEach((Consumer<Document>) doc -> System.out.println(doc.toJson()));
//
//		// another query
//		FindIterable fit = collection.find(Filters.and(Filters.lt("price", 50000), Filters.gt("price", 20000))).sort(new Document("price", -1));
//
//		List<Document> docs = new ArrayList<>();
//
//		fit.into(docs);
//		
//		docs.forEach(doc -> {
//			System.out.println(doc);
//		});
		}
	}

	@Test
//	@Ignore
	public void transformDocToInst() {
		printTestHeader("DocumentIT -> Transform Doc-To-Inst");
		try (var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");

			var transformation = new TransformationDocToInst();
			var category = transformation.process(database);

			System.out.println(category);
		}
	}

	@Test
//	@Ignore
	public void transformDocToRel() {
		printTestHeader("DocumentIT -> Transform Doc-To-Rel");
		try (var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");

			var transformation = new TransformationDocToInst();
			var category = transformation.process(database);

			var relationalInstance = Algorithms.instToRel(category);
//			System.out.println("COUNT TABLES: " + relationalInstance.countTables());
			for (int index = 0; index < relationalInstance.countTables(); ++index) {
				System.out.println(relationalInstance.getTable(index));

			}

		}
	}

	@Test
//	@Ignore
	public void transformCSVToDoc() {
		printTestHeader("DocumentIT -> Transform CSV-To-Doc");
		try (var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");

			AbstractTable csv = new CSVTable("People", "data.csv");
			System.out.println(csv);

			var csvInstance = new RelationalInstance();
			csvInstance.addTable(csv);

			var instanceCategory = Algorithms.relToInst(csvInstance);

			TransformationInstToDoc transformation = new TransformationInstToDoc();
			transformation.process(instanceCategory, database);

			MongoCollection<Document> collection = database.getCollection("People");
			try (MongoCursor<Document> cur = collection.find().iterator()) {
				while (cur.hasNext()) {
					var doc = cur.next();
					System.out.println(doc);
				}
			}
			database.getCollection("People").drop();
		}
	}

	@Test
//	@Ignore
	public void transformRelToDoc() {
		printTestHeader("DocumentIT -> Transform Rel-To-Doc");
		try (var mongoClient = MongoClients.create("mongodb://172.16.254.2:27017")) {

			MongoDatabase database = mongoClient.getDatabase("koupil");

		}
	}

}
