package cz.matfyz.wrapperjson.inference;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.matfyz.core.rsd.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import java.util.*;

/**
 * An abstract class responsible for processing a JSON record represented as key-value pairs and
 * converting it into a {@link RecordSchemaDescription} structure.
 */
public abstract class MapJsonRecord {

    private MapJsonRecord() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(MapJsonRecord.class);

    /**
     * Processes a given key-value pair and produces a {@link RecordSchemaDescription}
     * representing the schema of the JSON record.
     *
     * @param key the key representing the field name in the JSON record.
     * @param value the value associated with the key, which can be of various data types.
     * @param processChildren a boolean indicating whether to process child elements if the value is a complex type (e.g., Map or List).
     * @param firstOccurrence a boolean indicating whether this is the first occurrence of the value.
     * @return a {@link RecordSchemaDescription} representing the structure and schema inferred from the input key-value pair.
     */
    public static RecordSchemaDescription process(String key, Object value, boolean processChildren, boolean firstOccurrence) {
        RecordSchemaDescription result = new RecordSchemaDescription();
        result.setName(key);
        result.setUnique(Char.UNKNOWN);
        result.setId(Char.UNKNOWN);
        //result.setShare(new Share(1, firstOccurrence ? 1 : 0));

        int types = Type.OBJECT;

        result.setModels(Model.DOC);
//        result.addType(Type.OBJECT);
        if (value instanceof Number) {
            types = types | Type.NUMBER;
        } else if (value instanceof Boolean) {
            types = types | Type.BOOLEAN;
        } else if (value instanceof ObjectId || value instanceof String) {
            types = types | Type.STRING;
        } else if (value instanceof Map) {
            types = types | Type.MAP;
            if (processChildren) {
                result.setChildren(convertMapChildren(((Map<String, Object>) value).entrySet()));
            }
        } else if (value instanceof List) {
            types = types | Type.ARRAY;
            if (processChildren) {
                result.setChildren(convertArrayChildren((List<Object>) value));
            }
        } else if (value != null) {
            LOGGER.error("Invalid data type");
        }

        result.setTypes(types);

        return result;
    }

    /**
     * Converts a set of map entries into a list of {@link RecordSchemaDescription} objects representing
     * the children of a complex type.
     *
     * @param t1 a set of map entries representing the children of a complex type.
     * @return an {@link ObjectArrayList} of {@link RecordSchemaDescription} objects.
     */
    private static ObjectArrayList<RecordSchemaDescription> convertMapChildren(Set<Map.Entry<String, Object>> t1) {
        ObjectArrayList<RecordSchemaDescription> children = new ObjectArrayList<>();
        for (Map.Entry<String, Object> value : t1) {
            children.add(process(value.getKey(), value.getValue(), true, true));
        }
        Collections.sort(children);
        return children;
    }

    /**
     * Converts a list of objects into a list of {@link RecordSchemaDescription} objects representing
     * the children of an array type.
     *
     * @param t1 a list of objects representing the elements of an array type.
     * @return an {@link ObjectArrayList} of {@link RecordSchemaDescription} objects.
     */
    private static ObjectArrayList<RecordSchemaDescription> convertArrayChildren(List<Object> t1) {
        ObjectArrayList<RecordSchemaDescription> children = new ObjectArrayList<>();
        Set<Object> visited = new HashSet<>();
        for (Object value : t1) {
            if (value == null) {
                children.add(process("_", value, true, true));
            } else if (visited.stream().anyMatch(v -> value.getClass().isInstance(v))) {
                children.add(process("_", value, true, false));
            } else {
                visited.add(value);
                children.add(process("_", value, true, true));
            }
        }
        return children;
    }
}
