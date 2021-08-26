package cz.cuni.mff.ksi.nosql.s13e.datagen;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import net.jimblackler.jsongenerator.Configuration;
import net.jimblackler.jsongenerator.Generator;
import net.jimblackler.jsongenerator.JsonGeneratorException;
import net.jimblackler.jsonschemafriend.GenerationException;
import net.jimblackler.jsonschemafriend.Schema;
import net.jimblackler.jsonschemafriend.SchemaStore;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenerateJson {

	private static final int BATCH_COUNT = 100;

	public static final String PROPERTY_SEED = "datagen.seed";
	public static final String PROPERTY_DOCUMENT_COUNT = "datagen.documentCount";
	public static final String PROPERTY_DB_NAME = "datagen.dbName";

	private static final long seed = Long.getLong(PROPERTY_SEED, 0xdeadbeef);
	private static final int documentCount = Integer.getInteger(PROPERTY_DOCUMENT_COUNT, 1000);
	private static final String dbName = System.getProperty(PROPERTY_DB_NAME, "contos");

	static {
		if (documentCount % BATCH_COUNT != 0) {
			throw new RuntimeException("documentCount must be divisible by " + BATCH_COUNT);
		}
	}

	private static final int batchSize = documentCount / BATCH_COUNT;

	public static void main(String[] args) throws GenerationException, JsonGeneratorException {
		SchemaStore schemaStore = new SchemaStore();
		Schema schema = schemaStore.loadSchema(GenerateJson.class.getResource("schema.json"));
		Generator generator = new Generator(new GeneratorConfiguration(), schemaStore, new Random(seed));

		try ( MongoClient client = new MongoClient()) {
			client.dropDatabase(dbName);
			MongoCollection<Document> articles = client.getDatabase(dbName).getCollection("articles");

			List<Document> documents = new ArrayList<>(batchSize);
			for (int batchNumber = 0; batchNumber < BATCH_COUNT; batchNumber++) {
				for (int i = 0; i < batchSize; i++) {
					Document document = Document.parse(generator.generate(schema, 10).toString());
//                    document.put("_id", batchNumber * batchSize + i);
					documents.add(document);
				}
				articles.insertMany(documents);
				documents.clear();
				System.out.println(dots(batchNumber + 1));
			}
		}
	}

	private static String dots(int size) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < size; i++) {
			builder.append(".");
		}
		return builder.toString();
	}

	private static class GeneratorConfiguration implements Configuration {

		@Override
		public boolean isPedanticTypes() {
			return false;
		}

		@Override
		public boolean isGenerateNulls() {
			return true;
		}

		@Override
		public boolean isGenerateMinimal() {
			return false;
		}

		@Override
		public float nonRequiredPropertyChance() {
			return .5f;
		}

	}

}
