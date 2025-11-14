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

            return builder.build();
        } catch (Exception e) {
            throw MongoDBExceptionsFactory.getExceptionsFactory().cacheResultFailed(e);
        }
    }

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

    private void addDocumentsToResult(List<Document> batch, CachedResult.Builder builder) {
        for (Document document : batch) {
            builder.addEmptyRecord();
            for (var pair : document.entrySet()) {
                builder.toLastRecordAddValue(pair.getKey(), pair.getValue());
            }
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

    private void consumeDocumentsToResult(Iterable<Document> batch, ConsumedResult.Builder builder) {
        for (Document document : batch) {
            builder.addRecord();
            RawBsonDocument sizeDoc = RawBsonDocument.parse(document.toJson());
            builder.addByteSize(sizeDoc.getByteBuffer().remaining());
            parseColumnTypes(sizeDoc, builder);
        }
    }

    private void parseColumnTypes(RawBsonDocument document, ConsumedResult.Builder builder) {
        for (var entry : document.entrySet()) {
            String fieldName = entry.getKey();
            String type = parseType(entry.getValue());
            if (type != null)
                builder.addColumnType(fieldName, type);
        }
    }

    private String parseType(BsonValue value) {
        if (value.isArray())
            return "array";
        if (value.isBinary())
            return "binData";
        if (value.isBoolean())
            return "bool";
        if (value.isDateTime())
            return "date";
        if (value.isDecimal128())
            return "decimal";
        if (value.isDocument())
            return "object";
        if (value.isDouble())
            return "double";
        if (value.isInt32())
            return "int";
        if (value.isInt64())
            return "long";
        if (value.isObjectId())
            return "objectId";
        if (value.isString())
            return "string";

        return null;
    }

}
