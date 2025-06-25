package cz.matfyz.wrappermongodb.collector.components;


import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;
import cz.matfyz.wrappermongodb.collector.MongoExceptionsFactory;
import cz.matfyz.wrappermongodb.collector.MongoResources;
import cz.matfyz.core.collector.queryresult.CachedResult;
import cz.matfyz.core.collector.queryresult.ConsumedResult;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.RawBsonDocument;

import java.util.List;

public class MongoQueryResultParser {

    private MongoConnection _connection = null;

    public void setConnection(MongoConnection connection) {
        if (_connection == null)
            _connection = connection;
    }

    public void removeConnection() {
        if (_connection != null)
            _connection = null;
    }

    // Parse Result

    /**
     * Method which parses BsonValue to type it is using
     * @param value the BsonValue to be parsed
     * @return string representation of parsed type
     */
    private String _parseType(BsonValue value) {
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

    /**
     * Method which will fetch all documents from cursor to result
     * @param batch fetched documents
     * @param builder builder responsible for building the result
     */
    private void _addDocumentsToResult(List<Document> batch, CachedResult.Builder builder) {
        for (Document document : batch) {
            builder.addEmptyRecord();
            for (var pair : document.entrySet()) {
                builder.toLastRecordAddValue(pair.getKey(), pair.getValue());
            }
        }
    }

    /**
     * Method which will parse cursor result to CachedResult
     * @param cursor cursor document from native result
     * @param builder for CachedResult used to build it
     */
    private void _cacheCursorResult(Document cursor, CachedResult.Builder builder ) throws QueryExecutionException {
        if (cursor.containsKey("firstBatch")) {
            _addDocumentsToResult(cursor.getList("firstBatch", Document.class), builder);
        } else if (cursor.containsKey("nextBatch")) {
            _addDocumentsToResult(cursor.getList("nextBatch", Document.class), builder);
        } else {
            return;
        }

        String collectionName = cursor.getString("ns").split("\\.")[1] ;

        if (collectionName != null) {
            long cursorId = cursor.getLong("id");
            if (cursorId != 0 && _connection != null) {
                Document result = _connection.executeQuery(MongoResources.getNextBatchOfCursorCommand(cursorId, collectionName));
                _cacheCursorResult(result.get("cursor", Document.class), builder);
            }
        }
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
                _cacheCursorResult(result.get("cursor", Document.class), builder);
            else {
                builder.addEmptyRecord();
                for (var pair : result.entrySet()) {
                    builder.toLastRecordAddValue(pair.getKey(), pair.getValue());
                }
            }
            return builder.toResult();
        } catch (QueryExecutionException e) {
            throw MongoExceptionsFactory.getExceptionsFactory().cacheResultFailed(e);
        }
    }

    /**
     * Method which will measure stats about document from result and add them to consumed result
     * @param document from native result
     * @param builder builder responsible for building the result
     */
    private void _parseColumnTypes(RawBsonDocument document, ConsumedResult.Builder builder) {
        for (var entry : document.entrySet()) {
            String fieldName = entry.getKey();
            String type = _parseType(entry.getValue());
            if (type != null)
                builder.addColumnType(fieldName, type);
        }
    }

    /**
     * Method consuming all documents from cursor to result
     * @param batch fetched documents
     * @param builder builder responsible for building the result
     */
    private void _consumeDocumentsToResult(List<Document> batch, ConsumedResult.Builder builder) {
        for (Document document : batch) {
            builder.addRecord();
            RawBsonDocument sizeDoc = RawBsonDocument.parse(document.toJson());
            builder.addByteSize(sizeDoc.getByteBuffer().remaining());
            _parseColumnTypes(sizeDoc, builder);
        }
    }

    private void _consumeCursorResult(Document cursor, ConsumedResult.Builder builder ) throws QueryExecutionException {
        if (cursor.containsKey("firstBatch")) {
            _consumeDocumentsToResult(cursor.getList("firstBatch", Document.class), builder);
        } else if (cursor.containsKey("nextBatch")) {
            _consumeDocumentsToResult(cursor.getList("nextBatch", Document.class), builder);
        } else {
            return;
        }

        String collectionName = cursor.getString("ns").split("\\.")[1] ;

        if (collectionName != null) {
            long cursorId = cursor.getLong("id");
            if (cursorId != 0 && _connection != null) {
                Document result = _connection.executeQuery(MongoResources.getNextBatchOfCursorCommand(cursorId, collectionName));
                _consumeCursorResult(result.get("cursor", Document.class), builder);
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
                _consumeCursorResult(result.get("cursor", Document.class), builder);
            } else {
                builder.addRecord();
                RawBsonDocument sizeDoc = RawBsonDocument.parse(result.toJson());
                builder.addByteSize(sizeDoc.getByteBuffer().remaining());
                _parseColumnTypes(sizeDoc, builder);
            }
            return builder.toResult();
        } catch (QueryExecutionException e) {
            throw MongoExceptionsFactory.getExceptionsFactory().consumeResultFailed(e);
        }
    }
}
