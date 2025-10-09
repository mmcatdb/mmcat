package cz.matfyz.wrappermongodb.inference;

import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.RecordSchemaDescription;

import java.util.Iterator;

import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.sql.Row;
import scala.collection.Seq;
import scala.jdk.CollectionConverters;
import shaded.parquet.it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RecordToHeuristicsMap implements FlatMapFunction<Row, PropertyHeuristics> {

    private final String collectionName;

    public RecordToHeuristicsMap(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override public Iterator<PropertyHeuristics> call(Row row) {
        final var result = new ObjectArrayList<PropertyHeuristics>();

        appendHeuristics(collectionName, row, result);

        return result.iterator();
    }

    private void appendHeuristics(String key, Object value, ObjectArrayList<PropertyHeuristics> result) {
        result.add(PropertyHeuristics.createForKeyValuePair(key, value));

        if (value instanceof Row row)
            appendMapHeuristics(key, row, result);
        else if (value instanceof Seq seq)
            appendListHeuristics(key, seq, result);
    }

    private void appendMapHeuristics(String parentName, Row row,  ObjectArrayList<PropertyHeuristics> result) {
        for (final String key : row.schema().fieldNames()) {
            final String hierarchicalName = parentName + "/" + key;
            final var value = row.getAs(key);
            appendHeuristics(hierarchicalName, value, result);
        }
    }

    private void appendListHeuristics(String parentName, Seq<Object> seq,  ObjectArrayList<PropertyHeuristics> result) {
        final var list = CollectionConverters.SeqHasAsJava(seq).asJava();

        final String hierarchicalName = parentName + "/" + RecordSchemaDescription.ROOT_SYMBOL;

        for (final Object value : list)
            appendHeuristics(hierarchicalName, value, result);
    }

}
