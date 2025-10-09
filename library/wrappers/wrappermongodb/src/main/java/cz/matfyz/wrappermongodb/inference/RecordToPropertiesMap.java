package cz.matfyz.wrappermongodb.inference;

import cz.matfyz.core.rsd.Char;
import cz.matfyz.core.rsd.RecordSchemaDescription;
import cz.matfyz.core.rsd.DataType;
import cz.matfyz.core.rsd.DataType.DataTypeMap;

import java.util.Iterator;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Row;
import scala.collection.Seq;
import scala.jdk.CollectionConverters;
import shaded.parquet.it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RecordToPropertiesMap implements FlatMapFunction<Row, RecordSchemaDescription> {

    private final String collectionName;

    public RecordToPropertiesMap(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override public Iterator<RecordSchemaDescription> call(Row row) {
        final var result = new ObjectArrayList<RecordSchemaDescription>();

        processMapChildren(collectionName, row, result);

        return result.iterator();
    }

    private void appendProperty(String key, Object value, ObjectArrayList<RecordSchemaDescription> result) {
        final var type = MapMongoDocument.getType(value);
        result.add(buildPropertySchemaDescription(key, type));

        appendPropertyChildren(key, value, result);
    }

    private void appendPropertyChildren(String key, Object value, ObjectArrayList<RecordSchemaDescription> result) {
        if (value instanceof Row row)
            processMapChildren(key, row, result);
        else if (value instanceof Seq seq)
            processArrayChildren(key, seq, result);
    }

    private void processMapChildren(String parentName, Row row, ObjectArrayList<RecordSchemaDescription> result) {
        for (final String key : row.schema().fieldNames()) {
            final var value = row.getAs(key);
            final String hierarchicalName = parentName + "/" + key;
            appendProperty(hierarchicalName, value, result);
        }
    }

    private void processArrayChildren(String parentName, Seq<Object> seq, ObjectArrayList<RecordSchemaDescription> result) {
        final var list = CollectionConverters.SeqHasAsJava(seq).asJava();
        final var typeMap = new DataTypeMap<RecordSchemaDescription>();

        final String hierarchicalName = parentName + "/" + RecordSchemaDescription.ROOT_SYMBOL;

        for (final Object value : list) {
            final int type = MapMongoDocument.getType(value);
            final var property = typeMap.get(type);
            if (property == null)
                typeMap.set(type, buildPropertySchemaDescription(hierarchicalName, type));
            else
                property.setShareTotal(property.getShareTotal() + 1);

            appendPropertyChildren(hierarchicalName, value, result);
        }

        typeMap.forEach(property -> {
            if (property != null)
                result.add(property);
        });
    }

    private static RecordSchemaDescription buildPropertySchemaDescription(String key, int type) {
        final var isId = (key.equals("_id") || key.endsWith("/_id")) ? Char.TRUE : Char.UNKNOWN;
        final var result = new RecordSchemaDescription(
            key,
            isId,
            isId,
            1,
            1
        );

        result.setTypes(DataType.OBJECT | type);

        return result;
    }

}
