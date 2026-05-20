package cz.matfyz.wrapperneo4j.collector;

import cz.matfyz.core.collector.CachedResult;
import cz.matfyz.core.collector.ConsumedResult;

import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Value;
import org.neo4j.driver.internal.value.*;
import org.neo4j.driver.types.Node;
import org.neo4j.driver.types.Relationship;
import org.neo4j.driver.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class Neo4jQueryResultParser {

    public CachedResult parseResultAndCache(Result result) {
        var builder = new CachedResult.Builder();
        while (result.hasNext()) {
            var record = result.next();
            builder.addEmptyRecord();
            addDataToBuilder(builder, record);
        }
        return builder.build();
    }

    public ConsumedResult parseResultAndConsume(Result result) {
        var builder = new ConsumedResult.Builder();
        while(result.hasNext()) {
            var record = result.next();
            builder.addRecord();
            consumeDataToBuilder(builder, record);
        }
        return builder.toResult();
    }

    /** Adds record from native result to CachedResult. */
    private void addDataToBuilder(CachedResult.Builder builder, Record record) {
        for (Pair<String, Value> pair : record.fields()) {
            if (pair.value() instanceof NodeValue nodeValue) {
                for (Map.Entry<String, PropertyData> entry : parseNodeToMap(nodeValue.asNode())) {
                    PropertyData propData = entry.getValue();
                    builder.toLastRecordAddValue(entry.getKey(), propData.getValue());
                }
            }
            else if (pair.value() instanceof RelationshipValue relationshipValue) {
                for (Map.Entry<String, PropertyData> entry : parseRelationToMap(relationshipValue.asRelationship())) {
                    PropertyData propData = entry.getValue();
                    builder.toLastRecordAddValue(entry.getKey(), propData.getValue());
                }
            }
            else {
                PropertyData propData = PropertyData.fromValue(pair.value());
                if (propData != null) {
                    builder.toLastRecordAddValue(pair.key(), propData.getValue());
                }
            }
        }
    }

    /** Incrementally computes statistics for result record by record. */
    private void consumeDataToBuilder(ConsumedResult.Builder builder, Record record) {
        for (Pair<String, Value> pair : record.fields()) {
            if (pair.value() instanceof NodeValue nodeValue) {
                for (Map.Entry<String, PropertyData> entry : parseNodeToMap(nodeValue.asNode())) {
                    PropertyData propData = entry.getValue();
                    builder.addByteSize(Neo4jResources.DefaultSizes.getAvgColumnSizeByType(propData.getType()));
                    builder.addColumnType(entry.getKey(), propData.getType());
                }
            }
            else if (pair.value() instanceof RelationshipValue relationshipValue) {
                for (Map.Entry<String, PropertyData> entry : parseRelationToMap(relationshipValue.asRelationship())) {
                    PropertyData propData = entry.getValue();
                    builder.addByteSize(Neo4jResources.DefaultSizes.getAvgColumnSizeByType(propData.getType()));
                    builder.addColumnType(entry.getKey(), propData.getType());
                }
            }
            else {
                PropertyData propData = PropertyData.fromValue(pair.value());
                if (propData != null) {
                    builder.addByteSize(Neo4jResources.DefaultSizes.getAvgColumnSizeByType(propData.getType()));
                    builder.addColumnType(pair.key(), propData.getType());
                }
            }
        }
    }

    private Set<Map.Entry<String, PropertyData>> parseNodeToMap(Node node) {
        var map = new HashMap<String, PropertyData>();
        for (String colName : node.keys()) {
            PropertyData data = PropertyData.fromValue(node.get(colName));
            if (data != null)
                map.put(colName, data);
        }
        return map.entrySet();
    }

    private Set<Map.Entry<String, PropertyData>> parseRelationToMap(Relationship relation) {
        var map = new HashMap<String, PropertyData>();
        for (String colName : relation.keys()) {
            PropertyData data = PropertyData.fromValue(relation.get(colName));
            if (data != null)
                map.put(colName, data);
        }
        return map.entrySet();
    }

    /** Represents properties of entities from neo4j graph. */
    private static class PropertyData {

        private final Object value;
        private final String type;

        private PropertyData(Object value, String type) {
            this.value = value;
            this.type = type;
        }

        public Object getValue() {
            return value;
        }
        public String getType() {
            return type;
        }

        private static PropertyData fromValue(Value value) {
            if (value.isNull())
                return null;
            if (value instanceof BooleanValue boolValue)
                return new PropertyData(boolValue.asBoolean(), "Boolean");
            if (value instanceof DateValue dateValue)
                return new PropertyData(dateValue.asLocalDate(), "Date");
            if (value instanceof DurationValue durValue)
                return new PropertyData(durValue.asIsoDuration(), "Duration");
            if (value instanceof FloatValue floatValue)
                return new PropertyData(floatValue.asDouble(), "Float");
            if (value instanceof IntegerValue intValue)
                return new PropertyData(intValue.asLong(), "Integer");
            if (value instanceof ListValue listValue)
                return new PropertyData(listValue.asList(), "List");
            if (value instanceof LocalDateTimeValue localDateTimeValue)
                return new PropertyData(localDateTimeValue.asLocalDateTime(), "LocalDateTime");
            if (value instanceof LocalTimeValue localTimeValue)
                return new PropertyData(localTimeValue.asLocalTime(), "LocalTime");
            if (value instanceof PointValue pointValue)
                return new PropertyData(pointValue.asPoint(), "Point");
            if (value instanceof StringValue strValue)
                return new PropertyData(strValue.asString(), "String");
            if (value instanceof DateTimeValue dateTimeValue)
                return new PropertyData(dateTimeValue.asZonedDateTime(), "ZonedDateTime");
            if (value instanceof TimeValue timeValue)
                return new PropertyData(timeValue.asOffsetTime(), "ZonedTime");

            throw new ClassCastException("Neo4j Value " + value.toString() + "cannot by parsed to object");
        }

    }

}
