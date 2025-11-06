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

    /**
     * Method which parse node to map
     * @param node node to be parsed
     * @return parsed map
     */
    private Set<Map.Entry<String, PropertyData>> parseNodeToMap(Node node) {
        var map = new HashMap<String, PropertyData>();
        for (String colName : node.keys()) {
            PropertyData data = PropertyData.fromValue(node.get(colName));
            if (data != null)
                map.put(colName, data);
        }
        return map.entrySet();
    }

    /**
     * Method which parse edge to map
     * @param relation edge to be parsed
     * @return parsed map
     */
    private Set<Map.Entry<String, PropertyData>> parseRelationToMap(Relationship relation) {
        var map = new HashMap<String, PropertyData>();
        for (String colName : relation.keys()) {
            PropertyData data = PropertyData.fromValue(relation.get(colName));
            if (data != null)
                map.put(colName, data);
        }
        return map.entrySet();
    }


    /**
     * Method responsible for adding record from native result to CachedResult
     * @param builder builder to add records and then build CachedResult
     * @param record is native record from result
     */
    private void addDataToBuilder(CachedResult.Builder builder, org.neo4j.driver.Record record) {
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

    /**
     * Method for parsing ordinal result to Cached result
     * @param result result of some query
     * @return instance of CachedResult
     */
    public CachedResult parseResultAndCache(Result result) {
        var builder = new CachedResult.Builder();
        while (result.hasNext()) {
            var record = result.next();
            builder.addEmptyRecord();
            addDataToBuilder(builder, record);
        }
        return builder.toResult();
    }

    // Parse Main Result

    /**
     * Method which will incrementally compute statistics for result record by record
     * @param builder builder which consumes the data from result
     * @param record native record from result
     */
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

    /**
     * Method which will take result of some ordinal query and consume it
     * @param result is native result of some query
     * @return instance of ConsumedResult
     */
    public ConsumedResult parseResultAndConsume(Result result) {
        var builder = new ConsumedResult.Builder();
        while(result.hasNext()) {
            var record = result.next();
            builder.addRecord();
            consumeDataToBuilder(builder, record);
        }
        return builder.toResult();
    }

    /**
     * Class which represents properties of entities from neo4j graph
     */
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

        /**
         * Static method which will parse value of Value type to instance of this class
         * @param value value gathered from native result
         * @return instance of newly created PropertyData
         */
        public static PropertyData fromValue(Value value) {
            if (value.isNull())
                return null;
            else if (value instanceof BooleanValue boolValue)
                return new PropertyData(boolValue.asBoolean(), "Boolean");
            else if (value instanceof DateValue dateValue)
                return new PropertyData(dateValue.asLocalDate(), "Date");
            else if (value instanceof DurationValue durValue)
                return new PropertyData(durValue.asIsoDuration(), "Duration");
            else if (value instanceof FloatValue floatValue)
                return new PropertyData(floatValue.asDouble(), "Float");
            else if (value instanceof IntegerValue intValue)
                return new PropertyData(intValue.asLong(), "Integer");
            else if (value instanceof ListValue listValue)
                return new PropertyData(listValue.asList(), "List");
            else if (value instanceof LocalDateTimeValue localDateTimeValue)
                return new PropertyData(localDateTimeValue.asLocalDateTime(), "LocalDateTime");
            else if (value instanceof LocalTimeValue localTimeValue)
                return new PropertyData(localTimeValue.asLocalTime(), "LocalTime");
            else if (value instanceof PointValue pointValue)
                return new PropertyData(pointValue.asPoint(), "Point");
            else if (value instanceof StringValue strValue)
                return new PropertyData(strValue.asString(), "String");
            else if (value instanceof DateTimeValue dateTimeValue)
                return new PropertyData(dateTimeValue.asZonedDateTime(), "ZonedDateTime");
            else if (value instanceof TimeValue timeValue)
                return new PropertyData(timeValue.asOffsetTime(), "ZonedTime");
            else
                throw new ClassCastException("Neo4j Value " + value.toString() + "cannot by parsed to object");
        }
    }
}
