package cz.matfyz.wrappercsv.inference.helpers;

import cz.matfyz.core.rsd.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.*;

public enum MapCSVRecord {
    INSTANCE;

    private static final Logger LOGGER = LoggerFactory.getLogger(MapCSVRecord.class);

    public RecordSchemaDescription process(String key, Object value, boolean processChildren, boolean firstOccurrence) {
        RecordSchemaDescription result = new RecordSchemaDescription();
        result.setName(key);
        result.setUnique(Char.UNKNOWN);
        result.setId(Char.UNKNOWN);
        //result.setShare(new Share(1, firstOccurrence ? 1 : 0));

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
        } else if (value instanceof String) {
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

    private ObjectArrayList<RecordSchemaDescription> convertMapChildren(Set<Map.Entry<String, Object>> t1) {
        ObjectArrayList<RecordSchemaDescription> children = new ObjectArrayList<>();
        //List<RecordSchemaDescription> children = new ArrayList<>();
//        Set<RecordSchemaDescription> children = new HashSet<>();
        for (Map.Entry<String, Object> value : t1) {
            children.add(process(value.getKey(), value.getValue(), true, true));
        }
        //Collections.sort(children);
        return children;
    }

    private ObjectArrayList<RecordSchemaDescription> convertArrayChildren(List<Object> t1) {
        ObjectArrayList<RecordSchemaDescription> children = new ObjectArrayList<>();
        //List<RecordSchemaDescription> children = new ArrayList<>();
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
