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

    private static ObjectArrayList<RecordSchemaDescription> convertMapChildren(Set<Map.Entry<String, Object>> t1) {
        ObjectArrayList<RecordSchemaDescription> children = new ObjectArrayList<>();
        for (Map.Entry<String, Object> value : t1) {
            children.add(process(value.getKey(), value.getValue(), true, true));
        }
        Collections.sort(children);
        return children;
    }

    private static ObjectArrayList<RecordSchemaDescription> convertArrayChildren(List<Object> t1) {
        ObjectArrayList<RecordSchemaDescription> children = new ObjectArrayList<>();
        RecordSchemaDescription firstElement = null;

        for (Object value : t1) {
            RecordSchemaDescription currentElement = process(RecordSchemaDescription.ROOT_SYMBOL, value, true, firstElement == null);

            if (firstElement == null) {
                firstElement = currentElement;
                children.add(currentElement);
            } else if (firstElement.compareTo(currentElement) != 0) {
                children.add(currentElement);
            }
        }

        return children;
    }
}
