package cz.matfyz.wrappermongodb.collector;

import cz.matfyz.core.collector.CachedResult;
import cz.matfyz.core.collector.ConsumedResult;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.abstractwrappers.exception.collector.DataCollectException;

import org.bson.Document;

import java.util.*;

/**
 * Class which is responsible for collecting all the statistical data for mongodb wrapper after query is evaluated
 */
public class MongoDBDataCollector {

    private final DataModel model;
    private final MongoDBProvider provider;
    private final MongoDBQueryResultParser resultParser;

    public MongoDBDataCollector(DataModel model, MongoDBProvider provider, MongoDBQueryResultParser resultParser) {
        this.model = model;
        this.provider = provider;
        this.resultParser = resultParser;
    }

    /**
     * Collects all the statistical data for result.
     * @param result result of main query
     */
    public void collectData(ConsumedResult result) throws DataCollectException {
        String collectionName = getCollectionName();
        collectPageSize();
        collectDatabaseData();
        collectTableData(collectionName);
        collectColumnData(collectionName);
        collectIndexData(collectionName);
        collectResultData(result);
    }

    private CachedResult executeQuery(Document query) throws DataCollectException {
        try {
            final var result = provider.getDatabase().runCommand(query);
            return resultParser.parseResultAndCache(result);
        } catch (Exception e) {
            throw MongoDBExceptionsFactory.getExceptionsFactory().dataCollectionFailed(e);
        }
    }

    private String getCollectionName() throws DataCollectException {
        for (String collectionName : model.database.tables.keySet()) {
            return collectionName;
        }
        throw MongoDBExceptionsFactory.getExceptionsFactory().collectionNotParsed();
    }

    // #region Database

    private void collectPageSize() {
        model.database.pageSize = MongoDBResources.DefaultSizes.PAGE_SIZE;
    }

    private void collectCacheDatabaseSize() throws DataCollectException {
        CachedResult stats = executeQuery(MongoDBResources.getServerStatsCommand());

        if (stats.next()) {
            long size = new Document(stats.getMap("wiredTiger")).get("cache", Document.class).getLong("maximum bytes configured");
            model.database.cacheSize = size;
        }
    }

    private void collectDatabaseData() throws DataCollectException {
        CachedResult stats = executeQuery(MongoDBResources.getDatasetStatsCommand());

        if (stats.next()) {
            long size = stats.getLong("storageSize");
            model.database.sizeInBytes = size;
            long sizeInPages = (long) Math.ceil((double)size / model.database.pageSize);
            model.database.sizeInPages = sizeInPages;
        }
        collectCacheDatabaseSize();
    }

    // #endregion
    // #region Table

    private void collectTableData(String collectionName) throws DataCollectException {
        CachedResult stats = executeQuery(MongoDBResources.getCollectionStatsCommand(collectionName));

        if (stats.next()) {
            final long size = stats.getLong("storageSize");
            final var table = model.database.getTable(collectionName, true);
            table.sizeInBytes = size;

            final long sizeInPages = (long) Math.ceil((double)size / model.database.pageSize);
            table.sizeInPages = sizeInPages;

            final long rowCount = stats.getLong("count");
            table.rowCount = rowCount;
        }
    }

    // #endregion
    // #region Column

    private void collectColumnData(String collectionName) throws DataCollectException {
        CachedResult result = executeQuery(MongoDBResources.getFieldsInCollectionCommand(collectionName));

        if (result.next()) {
            List<String> fieldNames = result.getList("allKeys", String.class);

            for (String fieldName : fieldNames) {
                collectColumnType(collectionName, fieldName);
                collectColumnMandatory(collectionName, fieldName);
            }
        }
    }

    private void collectColumnType(String collectionName, String columnName) throws DataCollectException {
        CachedResult result = executeQuery(MongoDBResources.getFieldTypeCommand(collectionName, columnName));
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

    private void collectColumnByteSize(String collectionName, String columnName, String columnType) throws DataCollectException {
        if ("string".equals(columnType) || "object".equals(columnType) || "binData".equals(columnType)) {
            collectStringObjectColumnByteSize(collectionName, columnName, columnType);
        } else
            collectNumberColumnByteSize(collectionName, columnName, columnType);
    }

    private void collectStringObjectColumnByteSize(String collectionName, String columnName, String columnType) throws DataCollectException {
        CachedResult result = executeQuery(MongoDBResources.getAvgObjectStringSizeCommand(collectionName, columnName, columnType));
        if (result.next()) {
            int avgByteSize = (int)Math.round(result.getDouble("avg"));
            model.database.getTable(collectionName, true).getColumn(columnName, true).getColumnType(columnType, true).byteSize = avgByteSize;
        }
    }

    private void collectNumberColumnByteSize(String collectionName, String columnName, String columnType) {
        Integer size = MongoDBResources.DefaultSizes.getAvgColumnSizeByType(columnType);
        if (size == null) return;
        model.database.getTable(collectionName, true).getColumn(columnName, true).getColumnType(columnType, true).byteSize = size;
    }

    private void collectColumnMandatory(String collectionName, String columnName) throws DataCollectException {
        CachedResult result = executeQuery(MongoDBResources.getCollectionInfoCommand(collectionName));
        if (result.next()) {
            boolean isRequired = result.containsCol("options")
                ? isFieldRequired(new Document(result.getMap("options")), columnName)
                : "_id".equals(columnName);

            model.database.getTable(collectionName, true).getColumn(columnName, true).mandatory = isRequired;
        }
    }

    /**
     * @param options part of query result from which we analyze the fact
     */
    private boolean isFieldRequired(Document options, String columnName) {
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

    // #endregion
    // #region Index

    private void collectIndexData(String collectionName) throws DataCollectException {
        for (String indexName : model.database.indexes.keySet()) {
            collectIndexSizesData(collectionName, indexName);
            collectIndexRowCount(collectionName, indexName);
        }
    }

    private void collectIndexSizesData(String collectionName, String indexName) throws DataCollectException {
        CachedResult stats = executeQuery(MongoDBResources.getCollectionStatsCommand(collectionName));
        if (stats.next()) {
            long size = new Document(stats.getMap("indexSizes")).getLong(indexName);
            model.database.getIndex(indexName, true).sizeInBytes = size;
            model.database.getIndex(indexName, true).sizeInPages = (long)Math.ceil((double)size / model.database.pageSize);
        }
    }

    private void collectIndexRowCount(String collectionName, String indexName) throws DataCollectException {
        CachedResult result = executeQuery(MongoDBResources.getIndexRowCountCommand(collectionName, indexName));

        if (result.next()) {
            long count = result.getLong("n");
            model.database.getIndex(indexName, true).rowCount = count;
        }
    }

    // #endregion
    // #region Result

    /**
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

    private void collectResultColumnData(ConsumedResult result) {
        for (String colName : result.getColumnNames()) {
            for (String colType : result.getColumnTypes(colName)) {
                if (colType != null) {
                    Integer size = MongoDBResources.DefaultSizes.getAvgColumnSizeByType(colType);
                    if (size != null)
                        model.result.resultTable.getColumn(colName, true).getColumnType(colType, true).byteSize = size;
                    double ratio = result.getColumnTypeRatio(colName, colType);
                    model.result.resultTable.getColumn(colName, true).getColumnType(colType, true).ratio = ratio;
                }
            }
        }
    }

    // #endregion

}
