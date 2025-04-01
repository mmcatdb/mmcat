package cz.matfyz.inference.adminer;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.BiFunction;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.conversions.Bson;

public final class MongoDBAlgorithms {
    private MongoDBAlgorithms() {}

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

    /**
     * Parses a string into a list of strings.
     *
     * @param value the input string to parse.
     * @return a list of strings split by commas.
     * @throws InvalidParameterException if the input is not properly enclosed.
     */
    public static List<String> parseStringToList(String value) {
        return Arrays.stream(value.split(";"))
            .map(String::trim).toList();
    }

    /**
     * Defines a mapping between operator names and their corresponding MongoDB filter functions.
     *
     * @return A {@link Map} where the key is the operator name (e.g., "Equal", "Less") and the value
     *         is a {@link BiFunction} that takes a column name and a value, and produces a {@link Bson} filter.
     */
    private static Map<String, BiFunction<String, Object, Bson>> defineOperators() {
        final var ops = new TreeMap<String, BiFunction<String, Object, Bson>>();

        ops.put("Equal", Filters::eq);
        ops.put("NotEqual", Filters::ne);
        ops.put("Less", Filters::lt);
        ops.put("LessOrEqual", Filters::lte);
        ops.put("Greater", Filters::gt);
        ops.put("GreaterOrEqual", Filters::gte);

        ops.put("In", (column, value) -> Filters.in(column, parseStringToList((String) value)));
        ops.put("NotIn", (column, value) -> Filters.nin(column, parseStringToList((String) value)));

        ops.put("MatchRegEx", (column, value) -> Filters.regex(column, (String) value));

        return ops;
    }

    /**
     * A map of operator names to MongoDB filter functions.
     *
     * @return A {@link Map} of operator names to MongoDB filter functions.
     */
    public static final Map<String, BiFunction<String, Object, Bson>> OPERATORS = defineOperators();
}
