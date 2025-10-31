package cz.matfyz.wrapperjson.inference;

import cz.matfyz.core.rsd.PropertyHeuristics;
import cz.matfyz.core.rsd.RecordSchemaDescription;

import java.util.Iterator;

import javax.sql.rowset.serial.SerialBlob;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BinaryNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.spark.api.java.function.FlatMapFunction;
import shaded.parquet.it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class RecordToHeuristicsMap implements FlatMapFunction<ObjectNode, PropertyHeuristics> {

    /** The name of the collection being processed, used as a prefix for the keys. */
    private final String collectionName;

    public RecordToHeuristicsMap(String collectionName) {
        this.collectionName = collectionName;
    }

    @Override public Iterator<PropertyHeuristics> call(ObjectNode document) {
        final var result = new ObjectArrayList<PropertyHeuristics>();

        appendHeuristics(collectionName, document, result);

        return result.iterator();
    }

    private void appendHeuristics(String key, JsonNode value, ObjectArrayList<PropertyHeuristics> result) {
        final var parsed = parseValue(value);
        result.add(PropertyHeuristics.createForKeyValuePair(key, parsed));

        if (value.getNodeType() == JsonNodeType.OBJECT)
            appendMapHeuristics(key, (ObjectNode) value, result);
        else if (value.getNodeType() == JsonNodeType.ARRAY)
            appendListHeuristics(key, (ArrayNode) value, result);
    }

    private void appendMapHeuristics(String parentName, ObjectNode object,  ObjectArrayList<PropertyHeuristics> result) {
        object.properties().forEach(entry -> {
            final String hierarchicalName = parentName + "/" + entry.getKey();
            appendHeuristics(hierarchicalName, entry.getValue(), result);
        });
    }

    private void appendListHeuristics(String parentName, ArrayNode items,  ObjectArrayList<PropertyHeuristics> result) {
        final String hierarchicalName = parentName + "/" + RecordSchemaDescription.ROOT_SYMBOL;

        for (final var item : items)
            appendHeuristics(hierarchicalName, item, result);
    }

    static Object parseValue(JsonNode value) {
        return switch (value.getNodeType()) {
            case NULL -> null;
            case NUMBER -> value.numberValue();
            case BOOLEAN -> value.booleanValue();
            case STRING -> value.textValue();
            case BINARY -> {
                try {
                    final byte[] bytes = ((BinaryNode) value).binaryValue();
                    yield new SerialBlob(bytes);
                }
                catch (Exception e) {
                    // This should never happen, as the byte array is always valid.
                    yield null;
                }
            }
            // These are not primitive values that the heuristics can handle, so we return null.
            case OBJECT, ARRAY -> null;
            default -> null;
        };
    }

}
