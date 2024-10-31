package cz.matfyz.wrappermongodb.inference;

import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.Model;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.Type;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import shaded.parquet.it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RecordToPropertiesMap implements FlatMapFunction<Document, RecordSchemaDescription> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecordToPropertiesMap.class);

    private final String collectionName;

    public RecordToPropertiesMap(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override public Iterator<RecordSchemaDescription> call(Document document) throws Exception {
        // TODO: USE FAST UTIL!
        ObjectArrayList<RecordSchemaDescription> result = new ObjectArrayList<>();

        document.forEach((key, value) -> {
            appendProperty(collectionName + '/' + key, value, 1, result, true);
        });

        return result.iterator();
    }

    private RecordSchemaDescription buildProperty(String key, Object value, int firstShare, ObjectArrayList<RecordSchemaDescription> result) {
        RecordSchemaDescription schema = buildPropertySchemaDescription(key, value, firstShare, 1);

        if (value instanceof Map) {
            appendProperties(key, ((Map<String, Object>) value).entrySet(), result);
        } else if (value instanceof List) {
            appendArrayElements(key, (List<Object>) value, result);
        }

        return schema;
    }

    private void appendProperty(String key, Object value, int firstShare, ObjectArrayList<RecordSchemaDescription> result, boolean addThisProperty) {
        if (addThisProperty) {
            RecordSchemaDescription schema = buildPropertySchemaDescription(key, value, firstShare, 1);
            result.add(schema);
        }

        if (value instanceof Map) {
            appendProperties(key, ((Map<String, Object>) value).entrySet(), result);
        } else if (value instanceof List) {
            appendArrayElements(key, (List<Object>) value, result);
        }
    }

    private RecordSchemaDescription buildPropertySchemaDescription(String key, Object value, /*boolean processChildren,*/ int firstShare, int totalShare) {
        RecordSchemaDescription result = new RecordSchemaDescription();
        result.setName(key);
        result.setUnique(Char.UNKNOWN);
        result.setId(Char.UNKNOWN);
        result.setShareFirst(firstShare);
        result.setShareTotal(totalShare);

        int types = Type.OBJECT;

        result.setModels(Model.DOC);
        if (key.equals("_id") || key.endsWith("/_id")) {
            result.setId(Char.TRUE);
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
        } else if (value instanceof List) {
            types = types | Type.ARRAY;
        } else if (value != null) {
            LOGGER.error("Invalid data type");
        }

        result.setTypes(types);

        return result;
    }

    private void appendProperties(String parentName, Set<Map.Entry<String, Object>> nestedProperties, ObjectArrayList<RecordSchemaDescription> result) {
        parentName += '/';

        for (Map.Entry<String, Object> value : nestedProperties) {
            String hierarchicalName = parentName + value.getKey();
            appendProperty(hierarchicalName, value.getValue(), 1, result, true);
        }
    }

    private void appendArrayElements(String parentName, List<Object> elements, ObjectArrayList<RecordSchemaDescription> result) {
        ObjectArrayList<RecordSchemaDescription> xxx = new ObjectArrayList<>(5);
        xxx.add(0, null);
        xxx.add(1, null);
        xxx.add(2, null);
        xxx.add(3, null);
        xxx.add(4, null);

        int visitedModels = 0;
        int index = 0;

        boolean visited = false;
        String hierarchicalName = parentName + "/_";
        for (Object value : elements) {

            if (value instanceof Number) {
                visited = (visitedModels & Type.NUMBER) == Type.NUMBER;
                if (!visited) {
                    visitedModels = visitedModels | Type.NUMBER;
                }
                index = 0;
            } else if (value instanceof Boolean) {
                visited = (visitedModels & Type.BOOLEAN) == Type.BOOLEAN;
                if (!visited) {
                    visitedModels = visitedModels | Type.BOOLEAN;
                }
                index = 1;
            } else if (value instanceof ObjectId || value instanceof String) {
                visited = (visitedModels & Type.STRING) == Type.STRING;
                if (!visited) {
                    visitedModels = visitedModels | Type.STRING;
                }
                index = 2;
            } else if (value instanceof Map) {
                visited = (visitedModels & Type.MAP) == Type.MAP;
                if (!visited) {
                    visitedModels = visitedModels | Type.MAP;
                }
                index = 3;
            } else if (value instanceof List) {
                visited = (visitedModels & Type.ARRAY) == Type.ARRAY;
                if (!visited) {
                    visitedModels = visitedModels | Type.ARRAY;
                }
                index = 4;
            } else if (value != null) {
//                LOGGER.error("Invalid data type");
            }

            if (visited) {
                // nevytvaret nove property, ale udelat mapu
                RecordSchemaDescription property = xxx.get(index);
                property.setShareTotal(property.getShareTotal() + 1);

                // jen pro array a map tohle dál, což je obsaženo v té funkci
                appendProperty(hierarchicalName, value, 1, result, false);
            } else {
                RecordSchemaDescription property = buildProperty(hierarchicalName, value, 1, result);
                xxx.add(index, property);
            }
        }

        for (RecordSchemaDescription property : xxx) {
            if (property != null) {
                result.add(property);
            }
        }

//        result.addAll(xxx);
//        return result;
    }

//    private /*List*/ ObjectArrayList<RecordSchemaDescription> convertMapChildren(Set<Map.Entry<String, Object>> t1) {
//        ObjectArrayList/*List*/<RecordSchemaDescription> children = new /*ArrayList*/ ObjectArrayList<>();
////        Set<RecordSchemaDescription> children = new HashSet<>();
//        for (Map.Entry<String, Object> value : t1) {
//            children.add(buildPropertySchemaDescription(value.getKey(), value.getValue(), 1, 1));
//        }
//        Collections.sort(children);
//        return children;
//    }
//    private ObjectArrayList/*List*/<RecordSchemaDescription> convertArrayChildren(ObjectArrayList<Object> t1) {
//        ObjectArrayList/*List*/<RecordSchemaDescription> children = new ObjectArrayList/*ArrayList*/<>();
//        Set<Object> visited = new HashSet<>();
//        for (Object value : t1) {
//            if (value == null) {
//                children.add(buildPropertySchemaDescription("_", value, 1, 1));
//            } else if (visited.stream().anyMatch(v -> value.getClass().isInstance(v))) {
//                children.add(buildPropertySchemaDescription("_", value, 0, 1));
//            } else {
//                visited.add(value);
//                children.add(buildPropertySchemaDescription("_", value, 1, 1));
//            }
//
//        }
//        return children;
//    }
}
