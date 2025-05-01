package cz.matfyz.wrappermongodb;

import java.util.HashSet;
import java.util.Set;

import com.mongodb.client.MongoCollection;

import org.bson.Document;

public final class MongoDBUtils {
    private MongoDBUtils() {}

    /**
     * Recursively collects all property names from a given document.
     *
     * @param document The document to process.
     * @param properties The set to store property names.
     * @param prefix The prefix to apply to nested properties.
     */
    private static void collectProperties(Document document, Set<String> properties, String prefix) {
        document.forEach((key, value) -> {
            String field = prefix.isEmpty() ? key : prefix + "." + key;

            if (!field.equals("_id")) {
                properties.add(field);
            }

            if (value instanceof Document documentValue)
                collectProperties(documentValue, properties, field);
        });
    }

    /**
     * Retrieves a set of all distinct property names in a given collection.
     *
     * @param collection The collection to inspect.
     * @return A {@link Set} containing all property names found in the collection.
     */
    public static Set<String> getPropertyNames(MongoCollection<Document> collection) {
        Set<String> properties = new HashSet<>();
        collection.find().forEach(doc -> collectProperties(doc, properties, ""));
        return properties;
    }

}
