package cz.matfyz.wrappermongodb;

import java.util.ArrayList;
import java.util.List;

import com.mongodb.client.MongoCollection;

import org.bson.Document;

public final class MongoDBUtils {

    private MongoDBUtils() {}

    /**
     * Recursively collects all property names from a given document.
     *
     * @param propertyNames The set to store property names.
     * @param prefix The prefix to apply to nested properties.
     */
    private static void collectProperties(Document document, List<String> propertyNames, String prefix) {
        document.forEach((key, value) -> {
            String field = prefix.isEmpty() ? key : prefix + "." + key;

            if (!field.equals("_id")) {
                if (!propertyNames.contains(field)) {
                    propertyNames.add(field);
                }
            }

            if (value instanceof Document documentValue)
                collectProperties(documentValue, propertyNames, field);
        });
    }

    /**
     * Retrieves a list of all distinct property names in a given collection.
     */
    public static List<String> getPropertyNames(MongoCollection<Document> collection) {
        List<String> propertyNames = new ArrayList<>();

        for (final var document : collection.find())
            collectProperties(document, propertyNames, "");

        return propertyNames;
    }

}
