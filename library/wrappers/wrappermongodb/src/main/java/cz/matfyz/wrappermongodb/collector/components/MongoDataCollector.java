package cz.matfyz.wrappermongodb.collector.components;

import cz.matfyz.abstractwrappers.exception.collector.ConnectionException;
import cz.matfyz.wrappermongodb.collector.MongoExceptionsFactory;
import cz.matfyz.wrappermongodb.collector.MongoResources;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.core.collector.queryresult.CachedResult;
import cz.matfyz.core.collector.queryresult.ConsumedResult;
import cz.matfyz.abstractwrappers.exception.collector.DataCollectException;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;

import org.bson.Document;

import java.util.*;

/**
 * Class which is responsible for collecting all the statistical data for mongodb wrapper after query is evaluated
 */
public class MongoDataCollector {
    private final MongoConnection _connection;
    private final MongoQueryResultParser _resultParser;

    protected final String _databaseName;
    protected final DataModel _model;


    protected CachedResult executeQuery(Document query) throws DataCollectException {
        try {
            return _resultParser.parseResultAndCache(_connection.executeQuery(query));
        } catch (QueryExecutionException | ParseException e) {
            throw MongoExceptionsFactory.getExceptionsFactory().dataCollectionFailed(e);
        }
    }

    protected ConsumedResult executeQueryAndConsume(Document query) throws DataCollectException {
        try {
            return _resultParser.parseResultAndConsume(_connection.executeQuery(query));
        } catch (QueryExecutionException | ParseException e) {
            throw MongoExceptionsFactory.getExceptionsFactory().dataCollectionFailed(e);
        }
    }





    public MongoDataCollector(
            DataModel dataModel,
            MongoConnection connection,
            MongoQueryResultParser resultParser,
            String databaseName
    ) throws ConnectionException {
        _databaseName = databaseName;
        _model = dataModel;
        _connection = connection;
        _resultParser = resultParser;
    }

    // Save Dataset data

    /**
     * Method which will save page size
     */
    private void _collectPageSize() {
        _model.setPageSize(MongoResources.DefaultSizes.PAGE_SIZE);
    }

    /**
     * Method which will save cache size of dataset
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void _collectCacheDatabaseSize() throws DataCollectException {
        CachedResult stats = executeQuery(MongoResources.getServerStatsCommand());

        if (stats.next()) {
            long size = new Document(stats.getMap("wiredTiger")).get("cache", Document.class).getLong("maximum bytes configured");
            _model.setDatabaseCacheSize(size);
        }
    }

    /**
     * Method which will save all dataset data wrapper gathers
     * @throws DataCollectException when some QueryExecutionException occur during running help queries
     */
    private void _collectDatabaseData() throws DataCollectException {
        CachedResult stats = executeQuery(MongoResources.getDatasetStatsCommand());

        if (stats.next()) {
            long size = stats.getLong("storageSize");
            _model.setDatabaseByteSize(size);
            long sizeInPages = (long) Math.ceil((double)size / _model.getPageSize());
            _model.setDatabaseSizeInPages(sizeInPages);
        }
        _collectCacheDatabaseSize();
    }



    // Save column Data

    /**
     * Method which saves byte size for fields which are of object or string type
     * @param collectionName collection which is used for query
     * @param columnName used column name
     * @param columnType data type of column
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void _collectStringObjectColumnByteSize(String collectionName, String columnName, String columnType) throws DataCollectException {
        CachedResult result = executeQuery(MongoResources.getAvgObjectStringSizeCommand(collectionName, columnName, columnType));
        if (result.next()) {
            int avgByteSize = (int)Math.round(result.getDouble("avg"));
            _model.setColumnTypeByteSize(collectionName, columnName, columnType, avgByteSize);
        }
    }

    /**
     * Method which saves byte size for fields which are of number type
     * @param collectionName collection which is used for query
     * @param columnName used column name
     * @param columnType data type of column
     */
    private void _collectNumberColumnByteSize(String collectionName, String columnName, String columnType) {
        Integer size = MongoResources.DefaultSizes.getAvgColumnSizeByType(columnType);
        if (size != null) {
            _model.setColumnTypeByteSize(collectionName, columnName, columnType, size);
        }
    }

    /**
     * Method which saves average field byte size
     * @param collectionName collection which is used for query
     * @param columnName used column name
     * @param columnType data type of column
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void _collectColumnByteSize(String collectionName, String columnName, String columnType) throws DataCollectException {
        if ("string".equals(columnType) || "object".equals(columnType) || "binData".equals(columnType)) {
            _collectStringObjectColumnByteSize(collectionName, columnName, columnType);
        } else
            _collectNumberColumnByteSize(collectionName, columnName, columnType);
    }

    /**
     * Method which checks if field is required inside collection or no
     * @param options part of query result from which we analyze the fact
     * @param columnName which field we are interested
     * @return true if field is required
     */

    private boolean _isRequiredField(Document options, String columnName) {
        if ("_id".equals(columnName))
            return true;

        if (options.containsKey("validator")) {
            Document validator = options.get("validator", Document.class);
            if (validator.containsKey("$jsonSchema")) {
                Document schema = validator.get("$jsonSchema", Document.class);
                if (schema.containsKey("required")) {
                    List<String> fields = schema.getList("required", String.class);
                    return fields.contains(columnName);
                }
            }
        }
        return false;
    }

    /**
     * Method which saves fact if field is mandatory
     * @param collectionName collection which is used for query
     * @param columnName used column name
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void _collectColumnMandatory(String collectionName, String columnName) throws DataCollectException {
        CachedResult result = executeQuery(MongoResources.getCollectionInfoCommand(collectionName));
        if (result.next()) {
            if (result.containsCol("options")) {
                boolean isRequired = _isRequiredField(new Document(result.getMap("options")), columnName);
                _model.setColumnMandatory(collectionName, columnName, isRequired);
            } else {
                boolean isRequired = "_id".equals(columnName);
                _model.setColumnMandatory(collectionName, columnName, isRequired);
            }
        }
    }

    /**
     * Method which saves fields data type
     * @param collectionName collection which is used for query
     * @param columnName used column name
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void _collectColumnType(String collectionName, String columnName) throws DataCollectException {
        CachedResult result = executeQuery(MongoResources.getFieldTypeCommand(collectionName, columnName));
        List<Map.Entry<String, Integer>> types = new ArrayList<>();
        int maxCount = 0;

        while (result.next()) {
            int count = result.getInt("count");
            String type = new Document(result.getMap("_id")).getString("fieldType");
            types.add(Map.entry(type, count));
            maxCount += count;
        }

        for (var entry : types) {
            _collectColumnByteSize(collectionName, columnName, entry.getKey());
        }
    }

    /**
     * Collects all field data
     * @param collectionName collection used for query
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void _collectColumnData(String collectionName) throws DataCollectException {
        CachedResult result = executeQuery(MongoResources.getFieldsInCollectionCommand(collectionName));

        if (result.next()) {
            List<String> fieldNames = result.getList("allKeys", String.class);

            for (String fieldName : fieldNames) {
                _collectColumnType(collectionName, fieldName);
                _collectColumnMandatory(collectionName, fieldName);
            }
        }
    }

    // save Table data

    /**
     * Method used for saving all collection data
     * @param collectionName collection used in query
     * @throws DataCollectException some QueryExecutionException occur during running help query
     */
    private void _collectTableData(String collectionName) throws DataCollectException {
        CachedResult stats = executeQuery(MongoResources.getCollectionStatsCommand(collectionName));

        if (stats.next()) {
            long size = stats.getLong("storageSize");
            _model.setTableByteSize(collectionName, size);
            long sizeInPages = (long) Math.ceil((double)size / _model.getPageSize());
            _model.setTableSizeInPages(collectionName, sizeInPages);

            long rowCount = stats.getLong("count");
            _model.setTableRowCount(collectionName, rowCount);
        }

        _collectColumnData(collectionName);
    }

    // Save Index Data

    /**
     * Method which saves record count for index
     * @param collectionName used collection in query
     * @param indexName used index
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void _collectIndexRowCount(String collectionName, String indexName) throws DataCollectException {
        CachedResult result = executeQuery(MongoResources.getIndexRowCountCommand(collectionName, indexName));

        if (result.next()) {
            long count = result.getLong("n");
            _model.setIndexRowCount(indexName, count);
        }
    }

    /**
     * Method which collects index sizes for specified index
     * @param collectionName used collection
     * @param indexName used index
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void _collectIndexSizesData(String collectionName, String indexName) throws DataCollectException {
        CachedResult stats = executeQuery(MongoResources.getCollectionStatsCommand(collectionName));
        if (stats.next()) {
            int size = new Document(stats.getMap("indexSizes")).getInteger(indexName);
            _model.setIndexByteSize(indexName, size);
            _model.setIndexSizeInPages(indexName, (long)Math.ceil((double)size / _model.getPageSize()));
        }
    }

    /**
     * Method which collects all index data about all used indexes
     * @param collectionName used collection
     * @throws DataCollectException when some QueryExecutionException occur during running help queries
     */
    private void _collectIndexesData(String collectionName) throws DataCollectException {
        for (String indexName : _model.getIndexNames()) {
            _collectIndexSizesData(collectionName, indexName);
            _collectIndexRowCount(collectionName, indexName);
        }
    }

    /**
     * Get collection used by query
     * @return collection name
     * @throws DataCollectException when no such collection was parsed from query
     */
    private String _getCollectionName() throws DataCollectException {
        for (String collectionName : _model.getTableNames()) {
            return collectionName;
        }
        throw MongoExceptionsFactory.getExceptionsFactory().collectionNotParsed();
    }

    //Save Result data

    /**
     * Method which will save field data for field present in result
     * @param result result of executed main query
     */
    private void _collectResultColumnData(ConsumedResult result) {
        for (String colName : result.getColumnNames()) {
            for (String colType : result.getColumnTypes(colName)) {
                if (colType != null) {
                    Integer size = MongoResources.DefaultSizes.getAvgColumnSizeByType(colType);
                    if (size != null)
                        _model.setResultColumnTypeByteSize(colName, colType, size);
                    double ratio = result.getColumnTypeRatio(colName, colType);
                    _model.setResultColumnTypeRatio(colName, colType, ratio);
                }
            }
        }
    }

    /**
     * Method which saves all result data from result
     * @param result result of main query
     */
    private void _collectResultData(ConsumedResult result) {
        long size = result.getByteSize();
        _model.setResultByteSize(size);
        long count = result.getRowCount();
        _model.setResultRowCount(count);

        long sizeInPages = (long)Math.ceil((double) size / _model.getPageSize());
        _model.setResultSizeInPages(sizeInPages);

        _collectResultColumnData(result);
    }

    /**
     *  Public method which collects all the statistical data for result
     * @param result result of main query
     */
    public void collectData(ConsumedResult result) throws DataCollectException {
        String collName = _getCollectionName();
        _collectPageSize();
        _collectDatabaseData();
        _collectIndexesData(collName);
        _collectTableData(collName);
        _collectResultData(result);
    }
}
