package cz.matfyz.wrappermongodb.inference;

import cz.matfyz.core.rsd.*;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public abstract class MapMongoRecord {

    private MapMongoRecord() {}

    private static final Logger LOGGER = LoggerFactory.getLogger(MapMongoRecord.class);

    public static RecordSchemaDescription process(String key, Object value, boolean processChildren, boolean firstOccurrence) {
        RecordSchemaDescription result = new RecordSchemaDescription();
        result.setName(key);
        result.setUnique(Char.UNKNOWN);
        result.setId(Char.UNKNOWN);
        result.setShareTotal(1);
        result.setShareFirst(firstOccurrence ? 1 : 0);
//        result.setShare(new Share(1, firstOccurrence ? 1 : 0));

        int types = Type.OBJECT;

        result.setModels(Model.DOC);
//        result.addType(Type.OBJECT);
        if (key.equals("_id") || key.endsWith("/_id")) {
            result.setId(Char.TRUE);        //if the name of the property is _id, then it has to be unique identifier (MongoDB convention)
            result.setUnique(Char.TRUE);
        }
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
        // Set<RecordSchemaDescription> children = new HashSet<>();

        for (Map.Entry<String, Object> value : t1)
            children.add(process(value.getKey(), value.getValue(), true, true));

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
