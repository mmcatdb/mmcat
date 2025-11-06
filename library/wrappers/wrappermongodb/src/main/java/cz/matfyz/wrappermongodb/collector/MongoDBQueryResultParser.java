package cz.matfyz.wrappermongodb.collector;


import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.core.collector.CachedResult;
import cz.matfyz.core.collector.ConsumedResult;
import cz.matfyz.wrappermongodb.MongoDBProvider;

import org.bson.BsonValue;
import org.bson.Document;
import org.bson.RawBsonDocument;

import java.util.List;

import com.mongodb.client.AggregateIterable;

public class MongoDBQueryResultParser {

    private final MongoDBProvider provider;

    public MongoDBQueryResultParser(MongoDBProvider provider) {
        this.provider = null;
    }

    /**
     * Method for parsing native result of ordinal query to instance of CachedResult
     * @param result result of some query
     * @return parsed CachedResult instance
     * @throws ParseException is there to implements the abstract method
     */
    public CachedResult parseResultAndCache(Document result) throws ParseException {
        try {
            CachedResult.Builder builder = new CachedResult.Builder();

            if (result.containsKey("cursor"))
                cacheCursorResult(result.get("cursor", Document.class), builder);
            else {
                builder.addEmptyRecord();
                for (var pair : result.entrySet()) {
                    builder.toLastRecordAddValue(pair.getKey(), pair.getValue());
                }
            }

            return builder.toResult();
        } catch (Exception e) {
            throw MongoDBExceptionsFactory.getExceptionsFactory().cacheResultFailed(e);
        }
    }

    /**
     * Method which will parse cursor result to CachedResult
     * @param cursor cursor document from native result
     * @param builder for CachedResult used to build it
     */
    private void cacheCursorResult(Document cursor, CachedResult.Builder builder) {
        if (cursor.containsKey("firstBatch")) {
            addDocumentsToResult(cursor.getList("firstBatch", Document.class), builder);
        } else if (cursor.containsKey("nextBatch")) {
            addDocumentsToResult(cursor.getList("nextBatch", Document.class), builder);
        } else {
            return;
        }

        String collectionName = cursor.getString("ns").split("\\.")[1] ;

        if (collectionName != null) {
            long cursorId = cursor.getLong("id");
            if (cursorId != 0) {
                final var command = MongoDBResources.getNextBatchOfCursorCommand(cursorId, collectionName);
                Document result = provider.getDatabase().runCommand(command);
                cacheCursorResult(result.get("cursor", Document.class), builder);
            }
        }
    }

    /**
     * Method which will fetch all documents from cursor to result
     * @param batch fetched documents
     * @param builder builder responsible for building the result
     */
    private void addDocumentsToResult(List<Document> batch, CachedResult.Builder builder) {
        for (Document document : batch) {
            builder.addEmptyRecord();
            for (var pair : document.entrySet()) {
                builder.toLastRecordAddValue(pair.getKey(), pair.getValue());
            }
        }
    }


    /**
     * Method responsible for consuming result into ConsumedResult
     * @param result is native result of some query
     * @return instance of ConsumedResult
     * @throws ParseException is there to implement abstract method
     */
    public ConsumedResult parseResultAndConsume(Document result) throws ParseException {
        try {
            ConsumedResult.Builder builder = new ConsumedResult.Builder();

            if (result.containsKey("cursor")) {
                consumeCursorResult(result.get("cursor", Document.class), builder);
            } else {
                builder.addRecord();
                RawBsonDocument sizeDoc = RawBsonDocument.parse(result.toJson());
                builder.addByteSize(sizeDoc.getByteBuffer().remaining());
                parseColumnTypes(sizeDoc, builder);
            }

            return builder.toResult();
        } catch (Exception e) {
            throw MongoDBExceptionsFactory.getExceptionsFactory().consumeResultFailed(e);
        }
    }

    public ConsumedResult parseResultAndConsume(AggregateIterable<Document> result) {
        ConsumedResult.Builder builder = new ConsumedResult.Builder();

        consumeDocumentsToResult(result, builder);

        return builder.toResult();
    }

    private void consumeCursorResult(Document cursor, ConsumedResult.Builder builder) {
        if (cursor.containsKey("firstBatch")) {
            consumeDocumentsToResult(cursor.getList("firstBatch", Document.class), builder);
        } else if (cursor.containsKey("nextBatch")) {
            consumeDocumentsToResult(cursor.getList("nextBatch", Document.class), builder);
        } else {
            return;
        }

        String collectionName = cursor.getString("ns").split("\\.")[1] ;

        if (collectionName != null) {
            long cursorId = cursor.getLong("id");
            if (cursorId != 0) {
                final var command = MongoDBResources.getNextBatchOfCursorCommand(cursorId, collectionName);
                Document result = provider.getDatabase().runCommand(command);
                consumeCursorResult(result.get("cursor", Document.class), builder);
            }
        }
    }

    /**
     * Method consuming all documents from cursor to result
     * @param batch fetched documents
     * @param builder builder responsible for building the result
     */
    private void consumeDocumentsToResult(Iterable<Document> batch, ConsumedResult.Builder builder) {
        for (Document document : batch) {
            builder.addRecord();
            RawBsonDocument sizeDoc = RawBsonDocument.parse(document.toJson());
            builder.addByteSize(sizeDoc.getByteBuffer().remaining());
            parseColumnTypes(sizeDoc, builder);
        }
    }

    /**
     * Method which will measure stats about document from result and add them to consumed result
     * @param document from native result
     * @param builder builder responsible for building the result
     */
    private void parseColumnTypes(RawBsonDocument document, ConsumedResult.Builder builder) {
        for (var entry : document.entrySet()) {
            String fieldName = entry.getKey();
            String type = parseType(entry.getValue());
            if (type != null)
                builder.addColumnType(fieldName, type);
        }
    }

    /**
     * Method which parses BsonValue to type it is using
     * @param value the BsonValue to be parsed
     * @return string representation of parsed type
     */
    private String parseType(BsonValue value) {
        if (value.isArray())
            return "array";
        else if (value.isBinary())
            return "binData";
        else if (value.isBoolean())
            return "bool";
        else if (value.isDateTime())
            return "date";
        else if (value.isDecimal128())
            return "decimal";
        else if (value.isDocument())
            return "object";
        else if (value.isDouble())
            return "double";
        else if (value.isInt32())
            return "int";
        else if (value.isInt64())
            return "long";
        else if (value.isObjectId())
            return "objectId";
        else if (value.isString())
            return "string";
        else
            return null;
    }

}
