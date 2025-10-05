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
import org.apache.spark.sql.Row;
import org.bson.types.ObjectId;
import shaded.parquet.it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RecordToPropertiesMap implements FlatMapFunction<Row, RecordSchemaDescription> {

    private final String collectionName;

    public RecordToPropertiesMap(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override public Iterator<RecordSchemaDescription> call(Row row) throws Exception {
        // TODO: USE FAST UTIL!
        ObjectArrayList<RecordSchemaDescription> result = new ObjectArrayList<>();

        for (final String key : row.schema().fieldNames()) {
            // TODO this might need to be checked ... not sure whether Row behaves the same ways as the good old bson Document.
            final var value = row.getAs(key);
            appendProperty(collectionName + '/' + key, value, 1, result, true);
        }

        return result.iterator();
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

        result.setModels(Model.DOC);
        if (key.equals("_id") || key.endsWith("/_id")) {
            result.setId(Char.TRUE);
            result.setUnique(Char.TRUE);
        }

        final int types = Type.OBJECT | INDEX_TO_TYPE_VALUE[getTypeIndex(value)];
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
        final ObjectArrayList<RecordSchemaDescription> xxx = new ObjectArrayList<>(5);
        xxx.add(0, null);
        xxx.add(1, null);
        xxx.add(2, null);
        xxx.add(3, null);
        xxx.add(4, null);

        int visitedModels = 0;
        final String hierarchicalName = parentName + "/_";

        for (final Object value : elements) {
            final int index = getTypeIndex(value);
            final int typeValue = INDEX_TO_TYPE_VALUE[index];
            final boolean visited = (visitedModels & typeValue) == typeValue;

            if (visited) {
                // nevytvaret nove property, ale udelat mapu
                RecordSchemaDescription property = xxx.get(index);
                property.setShareTotal(property.getShareTotal() + 1);

                // jen pro array a map tohle dál, což je obsaženo v té funkci
                appendProperty(hierarchicalName, value, 1, result, false);
            } else {
                visitedModels = visitedModels | typeValue;

                RecordSchemaDescription property = buildProperty(hierarchicalName, value, 1, result);
                xxx.add(index, property);
            }
        }

        for (RecordSchemaDescription property : xxx)
            if (property != null)
                result.add(property);
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

    private static int getTypeIndex(Object value) {
        // TODO: Take care of null values in the data. (For now we deal with them as if they were string)
        if (value == null)
            return 2;

        return switch (value) {
            case Number number -> 0;
            case Boolean bool -> 1;
            case String string -> 2;
            case ObjectId objectId -> 2;
            case Map<?, ?> map -> 3;
            case List<?> list -> 4;
            default -> throw new IllegalArgumentException("Invalid data type");
        };
    }

    private static final int[] INDEX_TO_TYPE_VALUE = new int[] {
        Type.NUMBER,
        Type.BOOLEAN,
        Type.STRING,
        Type.MAP,
        Type.ARRAY
    };

}
