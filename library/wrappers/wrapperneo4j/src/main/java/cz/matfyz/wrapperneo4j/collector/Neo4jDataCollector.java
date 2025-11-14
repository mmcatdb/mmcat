package cz.matfyz.wrapperneo4j.collector;

import cz.matfyz.core.collector.CachedResult;
import cz.matfyz.core.collector.ConsumedResult;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.wrapperneo4j.Neo4jProvider;
import cz.matfyz.abstractwrappers.exception.collector.DataCollectException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.neo4j.driver.exceptions.Neo4jException;

/**
 * Class responsible for collecting all statistical data from neo4j
 */
public class Neo4jDataCollector {

    private final DataModel model;
    private final Neo4jProvider provider;
    private final Neo4jQueryResultParser resultParser;

    public Neo4jDataCollector(DataModel model, Neo4jProvider provider, Neo4jQueryResultParser resultParser) {
        this.model = model;
        this.provider = provider;
        this.resultParser = resultParser;
    }

    private CachedResult executeQuery(String query) throws DataCollectException {
        try (
            final var session = provider.getSession();
        ) {
            final var result = session.run(query);
            return resultParser.parseResultAndCache(result);
        } catch (Neo4jException e) {
            throw WrapperExceptionsFactory.getExceptionsFactory().dataCollectionFailed(e);
        }
    }

    private ConsumedResult executeQueryAndConsume(String query) throws DataCollectException {
        try (
            final var session = provider.getSession();
        ) {
            final var result = session.run(query);
            return resultParser.parseResultAndConsume(result);
        } catch (Exception e) {
            throw WrapperExceptionsFactory.getExceptionsFactory().dataCollectionFailed(e);
        }
    }

    public void collectData(ConsumedResult result) throws DataCollectException {
        collectDatabaseData();
        collectIndexData();
        collectTableData();
        collectResultData(result);
    }

    // #region Database

    private void collectDatabaseData() throws DataCollectException {
        collectPageSize();
        collectDatabaseSize();
        collectCacheSize();
    }

    private void collectPageSize() {
        model.database.pageSize = Neo4jResources.DefaultSizes.PAGE_SIZE;
    }

    private long parseUnit(String unit) throws DataCollectException {
        return switch (unit) {
            case "B" -> 1;
            case "KiB", "KB", "K", "kB", "kb", "k" -> 1024;
            case "MiB", "MB", "M", "mB", "mb", "m" -> 1048576;
            case "GiB", "GB", "G", "gB", "gb", "g" -> 1073741824;
            case "TiB", "TB" -> 1099511600000L;
            case "PiB", "PB" -> 1125899900000000L;
            //TODO: update
            default -> throw new DataCollectException("Invalid unit for memory settings: " + unit);
        };
    }

    /**
     * @return cache size in bytes
     */
    private long parsePageCacheSize(String size) throws DataCollectException {
        StringBuilder number = new StringBuilder();
        StringBuilder unit = new StringBuilder();
        boolean isNumber = true;

        for (char ch : size.toCharArray()) {
            if (isNumber && (Character.isDigit(ch) || ch == '.'))
                number.append(ch);
            else if (isNumber && !Character.isDigit(ch)) {
                unit.append(ch);
                isNumber = false;
            } else {
                unit.append(ch);
            }
        }

        return (new BigDecimal(number.toString()).longValue() * parseUnit(unit.toString()));
    }

    private void collectCacheSize() throws DataCollectException {
        CachedResult result = executeQuery(Neo4jResources.getPageCacheSizeQuery());
        if (result.next()) {
            String stringSize = result.getString("value");
            if (!"No Value".equals(stringSize)) {
                model.database.cacheSize = parsePageCacheSize(stringSize);
            }
        }
    }

    private void collectDatabaseSize() throws DataCollectException {
        CachedResult result = executeQuery(Neo4jResources.getDatabaseSizesQuery());
        if (result.next()) {
            long size = result.getLong("totalStoreSize");
            model.database.sizeInBytes = size;
            model.database.sizeInPages = (long) Math.ceil(
                    (double) size / Neo4jResources.DefaultSizes.PAGE_SIZE
            );
        }
    }

    // #endregion
    // #region Table

    private void collectTableData() throws DataCollectException {
        for (String label : model.database.tables.keySet()) {
            boolean isNode = isNodeLabel(label);
            collectTableConstraintCount(label);
            collectTableSizes(label, isNode);
            collectColumnData(label, isNode);
        }
    }

    private void collectTableConstraintCount(String label) throws DataCollectException {
        CachedResult result = executeQuery(Neo4jResources.getConstraintCountForLabelQuery(label));
        if (result.next()) {
            long count = result.getLong("count");
            model.database.getTable(label, true).constraintCount = count;
        }
    }

    private void collectTableSizes(String tableName, boolean isNode) throws DataCollectException {
        PropertiesSizeData sizes = isNode
            ? fetchNodePropertiesSize(Neo4jResources.getNodesOfSpecificLabelQuery(tableName))
            : fetchEdgePropertiesSize(Neo4jResources.getEdgesOfSpecificLabelQuery(tableName));
        long size = sizes.getByteSize();
        long rowCount = sizes.getCount();

        final var table = model.database.getTable(tableName, true);
        table.sizeInBytes = size;
        table.sizeInPages = (long)Math.ceil((double) size / Neo4jResources.DefaultSizes.PAGE_SIZE);
        table.rowCount = rowCount;
    }

    private boolean isNodeLabel(String label) throws DataCollectException {
        CachedResult result = executeQuery(Neo4jResources.getIsNodeLabelQuery(label));
        if (result.next()) {
            return result.getBoolean("isNodeLabel");
        }
        throw new DataCollectException("Invalid result of isNodeLabel query");
    }

    // #endregion
    // #region Column

    private void collectColumnData(String tableName, boolean isNode) throws DataCollectException {
        List<String> properties = isNode
            ? getPropertyNames(Neo4jResources.getNodePropertiesForLabelQuery(tableName))
            : getPropertyNames(Neo4jResources.getEdgePropertiesForLabelQuery(tableName));

        for (String columnName : properties) {
            collectSpecificColumnData(tableName, columnName, isNode);
        }
    }

    private List<String> getPropertyNames(String query) throws DataCollectException {
        List<String> properties = new ArrayList<>();
        CachedResult result = executeQuery(query);
        while (result.next()) {
            String property = result.getString("propertyName");
            if (property != null) {
                properties.add(property);
            }
        }
        return properties;
    }

    private void collectSpecificColumnData(String label, String property, boolean isNode) throws DataCollectException {
        CachedResult result = isNode
            ? executeQuery(Neo4jResources.getNodePropertyTypeAndMandatoryQuery(label, property))
            : executeQuery(Neo4jResources.getEdgePropertyTypeAndMandatoryQuery(label, property));
        if (result.next()) {
            boolean mandatory = result.getBoolean("mandatory");
            model.database.getTable(label, true).getColumn(property, true).mandatory = mandatory;

            for (String type : result.getList("propertyTypes", String.class)) {
                int columnSize = Neo4jResources.DefaultSizes.getAvgColumnSizeByType(type);
                model.database.getTable(label, true).getColumn(property, true).getColumnType(type, true).byteSize = columnSize;
            }
        }
    }

    // #endregion
    // #region Index

    private void collectIndexData() throws DataCollectException {
        for (String inxName : model.database.indexes.keySet()) {
            String[] names = inxName.split(":");

            boolean isNode = isNodeLabel(names[1]);
            collectTableNameFor(names);
            collectIndexSizes(inxName, names, isNode);
        }
    }

    private void collectTableNameFor(String[] indexNames) {
        model.database.addTable(indexNames[1]);
    }

    private void collectIndexSizes(String indexName, String[] indexNames, boolean isNode) throws DataCollectException {
        PropertiesSizeData sizes = isNode
            ? fetchNodePropertiesSize(Neo4jResources.getNodeAndPropertyQuery(indexNames[1], indexNames[2]))
            : fetchEdgePropertiesSize(Neo4jResources.getEdgeAndPropertyQuery(indexNames[1], indexNames[2]));
        long size = (long) Math.ceil((double) (sizes.getByteSize()) / 3);
        long rowCount = sizes.getCount();

        model.database.getIndex(indexName, true).rowCount = rowCount;
        model.database.getIndex(indexName, true).sizeInBytes = size;
        model.database.getIndex(indexName, true).sizeInPages = (long) Math.ceil((double) size / Neo4jResources.DefaultSizes.PAGE_SIZE);
    }

    /**
     * Calculates sizes of nodes of specific label.
     * @return array of size 2, where first number is byteSize of nodes in the collection and second one is their count
     */
    private PropertiesSizeData fetchNodePropertiesSize(String fetchQuery) throws DataCollectException {
        ConsumedResult result = executeQueryAndConsume(fetchQuery);
        return new PropertiesSizeData(
            result.getRowCount() * Neo4jResources.DefaultSizes.NODE_SIZE + result.getByteSize(),
            result.getRowCount()
        );
    }

    /**
     * Calculates sizes of edges of specific label.
     * @return array of size 2, where first number is byteSize of edges in the collection and second one is their count
     */
    private PropertiesSizeData fetchEdgePropertiesSize(String fetchQuery) throws DataCollectException {
        ConsumedResult result = executeQueryAndConsume(fetchQuery);
        return new PropertiesSizeData(
            result.getRowCount() * Neo4jResources.DefaultSizes.EDGE_SIZE + result.getByteSize(),
            result.getRowCount()
        );
    }

    // #endregion
    // #region Result

    private void collectResultData(ConsumedResult result) {
        long size = result.getByteSize();
        model.result.resultTable.sizeInBytes = (size);

        long count = result.getRowCount();
        model.result.resultTable.rowCount = count;

        long sizeInPages = (long) Math.ceil((double) size / Neo4jResources.DefaultSizes.PAGE_SIZE);
        model.result.resultTable.sizeInPages = sizeInPages;

        for (String colName : result.getColumnNames()) {
            for (String type : result.getColumnTypes(colName)) {
                model.result.resultTable.getColumn(colName, true).getColumnType(type, true).byteSize = Neo4jResources.DefaultSizes.getAvgColumnSizeByType(type);
                double ratio = result.getColumnTypeRatio(colName, type);
                model.result.resultTable.getColumn(colName, true).getColumnType(type, true).ratio = ratio;
            }
        }
    }

    // #endregion

    private static class PropertiesSizeData {

        private final long byteSize;
        private final long count;

        public PropertiesSizeData(long byteSize, long count) {
            this.byteSize = byteSize;
            this.count = count;
        }

        public long getByteSize() {
            return byteSize;
        }

        public long getCount() {
            return count;
        }

    }

}
