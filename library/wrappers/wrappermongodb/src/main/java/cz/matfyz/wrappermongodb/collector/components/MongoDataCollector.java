package cz.matfyz.wrappermongodb.collector.components;

import cz.matfyz.abstractwrappers.exception.collector.ConnectionException;
import cz.matfyz.wrappermongodb.collector.MongoExceptionsFactory;
import cz.matfyz.wrappermongodb.collector.MongoResources;
import cz.matfyz.core.collector.CachedResult;
import cz.matfyz.core.collector.ConsumedResult;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.exception.collector.DataCollectException;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;

import org.bson.Document;

import java.util.*;

/**
 * Class which is responsible for collecting all the statistical data for mongodb wrapper after query is evaluated
 */
public class MongoDataCollector {
    private final MongoConnection connection;
    private final MongoQueryResultParser resultParser;

    protected final String databaseName;
    protected final DataModel model;


    protected CachedResult executeQuery(Document query) throws DataCollectException {
        try {
            return resultParser.parseResultAndCache(connection.executeQuery(query));
        } catch (QueryExecutionException | ParseException e) {
            throw MongoExceptionsFactory.getExceptionsFactory().dataCollectionFailed(e);
        }
    }

    protected ConsumedResult executeQueryAndConsume(Document query) throws DataCollectException {
        try {
            return resultParser.parseResultAndConsume(connection.executeQuery(query));
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
        this.databaseName = databaseName;
        this.model = dataModel;
        this.connection = connection;
        this.resultParser = resultParser;
    }

    // Save Dataset data

    /**
     * Method which will save page size
     */
    private void collectPageSize() {
        model.database.pageSize = MongoResources.DefaultSizes.PAGE_SIZE;
    }

    /**
     * Method which will save cache size of dataset
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void collectCacheDatabaseSize() throws DataCollectException {
        CachedResult stats = executeQuery(MongoResources.getServerStatsCommand());

        if (stats.next()) {
            long size = new Document(stats.getMap("wiredTiger")).get("cache", Document.class).getLong("maximum bytes configured");
            model.database.cacheSize = size;
        }
    }

    /**
     * Method which will save all dataset data wrapper gathers
     * @throws DataCollectException when some QueryExecutionException occur during running help queries
     */
    private void collectDatabaseData() throws DataCollectException {
        CachedResult stats = executeQuery(MongoResources.getDatasetStatsCommand());

        if (stats.next()) {
            long size = stats.getLong("storageSize");
            model.database.sizeInBytes = size;
            long sizeInPages = (long) Math.ceil((double)size / model.database.pageSize);
            model.database.sizeInPages = sizeInPages;
        }
        collectCacheDatabaseSize();
    }



    // Save column Data

    /**
     * Method which saves byte size for fields which are of object or string type
     * @param collectionName collection which is used for query
     * @param columnName used column name
     * @param columnType data type of column
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void collectStringObjectColumnByteSize(String collectionName, String columnName, String columnType) throws DataCollectException {
        CachedResult result = executeQuery(MongoResources.getAvgObjectStringSizeCommand(collectionName, columnName, columnType));
        if (result.next()) {
            int avgByteSize = (int)Math.round(result.getDouble("avg"));
            model.database.getTable(collectionName, true).getColumn(columnName, true).getColumnType(columnType, true).byteSize = avgByteSize;
        }
    }

    /**
     * Method which saves byte size for fields which are of number type
     * @param collectionName collection which is used for query
     * @param columnName used column name
     * @param columnType data type of column
     */
    private void collectNumberColumnByteSize(String collectionName, String columnName, String columnType) {
        Integer size = MongoResources.DefaultSizes.getAvgColumnSizeByType(columnType);
        if (size == null) return;
        model.database.getTable(collectionName, true).getColumn(columnName, true).getColumnType(columnType, true).byteSize = size;
    }

    /**
     * Method which saves average field byte size
     * @param collectionName collection which is used for query
     * @param columnName used column name
     * @param columnType data type of column
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void collectColumnByteSize(String collectionName, String columnName, String columnType) throws DataCollectException {
        if ("string".equals(columnType) || "object".equals(columnType) || "binData".equals(columnType)) {
            collectStringObjectColumnByteSize(collectionName, columnName, columnType);
        } else
            collectNumberColumnByteSize(collectionName, columnName, columnType);
    }

    /**
     * Method which checks if field is required inside collection or no
     * @param options part of query result from which we analyze the fact
     * @param columnName which field we are interested
     * @return true if field is required
     */

    private boolean isRequiredField(Document options, String columnName) {
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
    private void collectColumnMandatory(String collectionName, String columnName) throws DataCollectException {
        CachedResult result = executeQuery(MongoResources.getCollectionInfoCommand(collectionName));
        if (result.next()) {
            boolean isRequired = false;
            if (result.containsCol("options")) {
                isRequired = isRequiredField(new Document(result.getMap("options")), columnName);
            } else {
                isRequired = "_id".equals(columnName);
            }
            model.database.getTable(collectionName, true).getColumn(columnName, true).mandatory = isRequired;
        }
    }

    /**
     * Method which saves fields data type
     * @param collectionName collection which is used for query
     * @param columnName used column name
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void collectColumnType(String collectionName, String columnName) throws DataCollectException {
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
            collectColumnByteSize(collectionName, columnName, entry.getKey());
        }
    }

    /**
     * Collects all field data
     * @param collectionName collection used for query
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void collectColumnData(String collectionName) throws DataCollectException {
        CachedResult result = executeQuery(MongoResources.getFieldsInCollectionCommand(collectionName));

        if (result.next()) {
            List<String> fieldNames = result.getList("allKeys", String.class);

            for (String fieldName : fieldNames) {
                collectColumnType(collectionName, fieldName);
                collectColumnMandatory(collectionName, fieldName);
            }
        }
    }

    // save Table data

    /**
     * Method used for saving all collection data
     * @param collectionName collection used in query
     * @throws DataCollectException some QueryExecutionException occur during running help query
     */
    private void collectTableData(String collectionName) throws DataCollectException {
        CachedResult stats = executeQuery(MongoResources.getCollectionStatsCommand(collectionName));

        if (stats.next()) {
            final long size = stats.getLong("storageSize");
            final var table = model.database.getTable(collectionName, true);
            table.sizeInBytes = size;

            final long sizeInPages = (long) Math.ceil((double)size / model.database.pageSize);
            table.sizeInPages = sizeInPages;

            final long rowCount = stats.getLong("count");
            table.rowCount = rowCount;
        }

        collectColumnData(collectionName);
    }

    // Save Index Data

    /**
     * Method which saves record count for index
     * @param collectionName used collection in query
     * @param indexName used index
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void collectIndexRowCount(String collectionName, String indexName) throws DataCollectException {
        CachedResult result = executeQuery(MongoResources.getIndexRowCountCommand(collectionName, indexName));

        if (result.next()) {
            long count = result.getLong("n");
            model.database.getIndex(indexName, true).rowCount = count;
        }
    }

    /**
     * Method which collects index sizes for specified index
     * @param collectionName used collection
     * @param indexName used index
     * @throws DataCollectException when some QueryExecutionException occur during running help query
     */
    private void collectIndexSizesData(String collectionName, String indexName) throws DataCollectException {
        CachedResult stats = executeQuery(MongoResources.getCollectionStatsCommand(collectionName));
        if (stats.next()) {
            long size = new Document(stats.getMap("indexSizes")).getLong(indexName);
            model.database.getIndex(indexName, true).sizeInBytes = size;
            model.database.getIndex(indexName, true).sizeInPages = (long)Math.ceil((double)size / model.database.pageSize);
        }
    }

    /**
     * Method which collects all index data about all used indexes
     * @param collectionName used collection
     * @throws DataCollectException when some QueryExecutionException occur during running help queries
     */
    private void collectIndexesData(String collectionName) throws DataCollectException {
        for (String indexName : model.database.indexes.keySet()) {
            collectIndexSizesData(collectionName, indexName);
            collectIndexRowCount(collectionName, indexName);
        }
    }

    /**
     * Get collection used by query
     * @return collection name
     * @throws DataCollectException when no such collection was parsed from query
     */
    private String getCollectionName() throws DataCollectException {
        for (String collectionName : model.database.tables.keySet()) {
            return collectionName;
        }
        throw MongoExceptionsFactory.getExceptionsFactory().collectionNotParsed();
    }

    //Save Result data

    /**
     * Method which will save field data for field present in result
     * @param result result of executed main query
     */
    private void collectResultColumnData(ConsumedResult result) {
        for (String colName : result.getColumnNames()) {
            for (String colType : result.getColumnTypes(colName)) {
                if (colType != null) {
                    Integer size = MongoResources.DefaultSizes.getAvgColumnSizeByType(colType);
                    if (size != null)
                        model.result.resultTable.getColumn(colName, true).getColumnType(colType, true).byteSize = size;
                    double ratio = result.getColumnTypeRatio(colName, colType);
                    model.result.resultTable.getColumn(colName, true).getColumnType(colType, true).ratio = ratio;
                }
            }
        }
    }

    /**
     * Method which saves all result data from result
     * @param result result of main query
     */
    private void collectResultData(ConsumedResult result) {
        long size = result.getByteSize();
        model.result.resultTable.sizeInBytes = (size);
        long count = result.getRowCount();
        model.result.resultTable.rowCount = count;

        long sizeInPages = (long)Math.ceil((double) size / model.database.pageSize);
        model.result.resultTable.sizeInPages = sizeInPages;

        collectResultColumnData(result);
    }

    /**
     *  Public method which collects all the statistical data for result
     * @param result result of main query
     */
    public void collectData(ConsumedResult result) throws DataCollectException {
        String collName = getCollectionName();
        collectPageSize();
        collectDatabaseData();
        collectIndexesData(collName);
        collectTableData(collName);
        collectResultData(result);
    }
}
