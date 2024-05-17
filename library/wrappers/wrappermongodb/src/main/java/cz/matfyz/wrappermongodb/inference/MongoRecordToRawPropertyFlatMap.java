package cz.matfyz.wrappermongodb.inference;

import cz.matfyz.core.rsd.RawProperty;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MongoRecordToRawPropertyFlatMap {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoRecordToRawPropertyFlatMap.class);

    public static Iterator<RawProperty> process(String collectionName, Document t, boolean loadSchema, boolean loadData) {
        return new MongoRecordToRawPropertyFlatMap(loadSchema, loadData).process(collectionName, t);
    }

    private final boolean loadSchema;
    private final boolean loadData;


    private MongoRecordToRawPropertyFlatMap(boolean loadSchema, boolean loadData) {
        this.loadSchema = loadSchema;
        this.loadData = loadData;
    }

    public Iterator<RawProperty> process(String collectionName, Document t) {
        List<RawProperty> result = new ArrayList<>(objectToRawProperties(collectionName, new Document(), true));

        t.forEach((key, value) -> {
            result.addAll(objectToRawProperties(collectionName + '/' + key, value, true));
        });

        return result.iterator();
    }


    private Collection<? extends RawProperty> mapToRawProperties(String key, Set<Map.Entry<String, Object>> t1) {
        List<RawProperty> result = new ArrayList<>();

        for (Map.Entry<String, Object> value : t1) {
            String hierarchicalName = key + '/' + value.getKey();
            result.addAll(objectToRawProperties(hierarchicalName, value.getValue(), true));
        }

        return result;
    }

    private Collection<? extends RawProperty> arrayToRawProperties(String key, List<Object> t1) {
        List<RawProperty> result = new ArrayList<>();
        Set<Object> visited = new HashSet<>();

        for (Object value : t1) {
            String hierarchicalName = key + "/_";
            if (visited.stream().anyMatch(v -> value.getClass().isInstance(v))) {
                result.addAll(objectToRawProperties(hierarchicalName, value, false));
            } else {
                visited.add(value);
                result.addAll(objectToRawProperties(hierarchicalName, value, true));
            }
        }

        return result;
    }

    private Collection<? extends RawProperty> objectToRawProperties(String key, Object value, boolean firstOccurrence) {
        List<RawProperty> result = new ArrayList<>();
        RecordSchemaDescription schema = loadSchema ? MapMongoRecord.process(key, value, false, true) : null;
        if (value instanceof Map) {
            result.add(new RawProperty(key, null, schema, 1, firstOccurrence ? 1 : 0));
            result.addAll(mapToRawProperties(key, ((Map<String, Object>) value).entrySet()));
        } else if (value instanceof List) {
            result.add(new RawProperty(key, null, schema, 1, firstOccurrence ? 1 : 0));
            result.addAll(arrayToRawProperties(key, (List<Object>) value));
        } else {
            Object data = loadData ? value : null;
            result.add(new RawProperty(key, data, schema, 1, firstOccurrence ? 1 : 0));
        }

        return result;
    }

}
