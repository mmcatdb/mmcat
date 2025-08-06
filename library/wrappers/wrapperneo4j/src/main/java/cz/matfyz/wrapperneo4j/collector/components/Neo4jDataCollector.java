package cz.matfyz.wrapperneo4j.collector.components;

import cz.matfyz.abstractwrappers.exception.collector.ConnectionException;
import cz.matfyz.wrapperneo4j.collector.Neo4jResources;
import cz.matfyz.core.collector.CachedResult;
import cz.matfyz.core.collector.ConsumedResult;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.exception.collector.DataCollectException;
import cz.matfyz.abstractwrappers.exception.collector.QueryExecutionException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Class responsible for collecting all statistical data from neo4j
 */
public class Neo4jDataCollector {

    private final Neo4jConnection connection;
    private final Neo4jQueryResultParser resultParser;

    protected final String databaseName;
    protected final DataModel model;


    public Neo4jDataCollector(
            DataModel dataModel,
            Neo4jConnection connection,
            Neo4jQueryResultParser resultParser,
            String databaseName
    ) throws ConnectionException {
        this.databaseName = databaseName;
        this.model = dataModel;
        this.connection = connection;
        this.resultParser = resultParser;
    }


    protected CachedResult executeQuery(String query) throws DataCollectException {
        try {
            return resultParser.parseResultAndCache(connection.executeQuery(query));
        } catch (QueryExecutionException e) {
            throw WrapperExceptionsFactory.getExceptionsFactory().dataCollectionFailed(e);
        }
    }

    protected ConsumedResult executeQueryAndConsume(String query) throws DataCollectException {
        try {
            return resultParser.parseResultAndConsume(connection.executeQuery(query));
        } catch (QueryExecutionException e) {
            throw WrapperExceptionsFactory.getExceptionsFactory().dataCollectionFailed(e);
        }
    }

    /**
     * Method which saves page size used by databases storage engine
     */
    private void collectPageSize() {
        model.database.pageSize = Neo4jResources.DefaultSizes.PAGE_SIZE;
    }

    /**
     * Method responsible for parsing unit which is returned with cache size
     * @param unit string representation of unit
     * @return multiplier which corresponds to specified unit
     * @throws DataCollectException when unit cannot be parsed
     */
    private long parseUnit(String unit) throws DataCollectException {
        if ("B".equals(unit))
            return 1;
        if ("KiB".equals(unit) || "KB".equals(unit) || "K".equals(unit) || "kB".equals(unit) || "kb".equals(unit) || "k".equals(unit))
            return 1024;
        if ("MiB".equals(unit) || "MB".equals(unit) || "M".equals(unit) || "mB".equals(unit) || "mb".equals(unit) || "m".equals(unit))
            return 1048576;
        if ("GiB".equals(unit) || "GB".equals(unit) || "G".equals(unit) || "gB".equals(unit) || "gb".equals(unit) || "g".equals(unit))
            return 1073741824;
        if ("TiB".equals(unit) || "TB".equals(unit))
            return 1099511600000L;
        if ("PiB".equals(unit) || "PB".equals(unit))
            return  1125899900000000L;
        else
            //TODO: update
            throw new DataCollectException("Invalid unit for memory settings: " + unit);
    }

    /**
     * Method responsible for parsing caching size
     * @param size string which contains number and unit
     * @return parsed number which is size in bytes
     * @throws DataCollectException when input cannot be parsed
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

    /**
     * Method which saves the gathered cache size to data model
     * @throws DataCollectException when QueryExecutionException occur in help query evaluation
     */
    private void collectCacheSize() throws DataCollectException {
        CachedResult result = executeQuery(Neo4jResources.getPageCacheSizeQuery());
        if (result.next()) {
            String stringSize = result.getString("value");
            if (!"No Value".equals(stringSize)) {
                model.database.cacheSize = parsePageCacheSize(stringSize);
            }
        }
    }

    /**
     * Method responsible for saving all dataset sizes parameters to data model
     * @throws DataCollectException when it is thrown from some of the help queries
     */
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

    /**
     * Method which saves all dataset data
     * @throws DataCollectException whe some of the help queries produced a problem
     */
    private void collectDatabaseData() throws DataCollectException {
        collectPageSize();
        collectDatabaseSize();
        collectCacheSize();
    }

    // Save ColumnData

    /**
     * Method responsible for saving all column data
     * @param label is label of node or edge from neo4j graph
     * @param property is field for which data we are interested in
     * @param isNode boolean which indicates if we are parsing node property or edge property
     * @throws DataCollectException when some of the help queries fail
     */
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

    /**
     * Method responsible for getting all properties for entity of some label
     * @param query query which defines on what entities we are interested
     * @return list of names for this properties
     * @throws DataCollectException when help query fails
     */
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

    /**
     * Method responsible for saving all column data for entities of specific label
     * @param tableName is label of entities
     * @param isNode indicates if entity is node or edge
     * @throws DataCollectException when some of the help queries fails
     */
    private void collectColumnData(String tableName, boolean isNode) throws DataCollectException {
        List<String> properties = isNode ? getPropertyNames(Neo4jResources.getNodePropertiesForLabelQuery(tableName)) :
                getPropertyNames(Neo4jResources.getEdgePropertiesForLabelQuery(tableName));

        for (String columnName : properties) {
            collectSpecificColumnData(tableName, columnName, isNode);
        }
    }

    /**
     * Method which is responsible for calculating sizes of nodes of specific label
     * @param fetchQuery defines on which nodes we are interested
     * @return array of size 2, where first number is byteSize of nodes in the collection and second one is their count
     * @throws DataCollectException when help query fails
     */
    private PropertiesSizeData fetchNodePropertiesSize(String fetchQuery) throws DataCollectException {
        ConsumedResult result = executeQueryAndConsume(fetchQuery);
        return new PropertiesSizeData(
                result.getRowCount() * Neo4jResources.DefaultSizes.NODE_SIZE + result.getByteSize(),
                result.getRowCount()
        );
    }

    /**
     * Method which is responsible for calculating sizes of edges of specific label
     * @param fetchQuery defines on which edges we are interested
     * @return array of size 2, where first number is byteSize of edges in the collection and second one is their count
     * @throws DataCollectException when help query fails
     */
    private PropertiesSizeData fetchEdgePropertiesSize(String fetchQuery) throws DataCollectException {
        ConsumedResult result = executeQueryAndConsume(fetchQuery);
        return new PropertiesSizeData(
                result.getRowCount() * Neo4jResources.DefaultSizes.EDGE_SIZE + result.getByteSize(),
                result.getRowCount()
        );
    }

    /**
     * Method responsible for saving how much constraints were used on entities of specified label
     * @param label specifies entities we are interested in
     * @throws DataCollectException when help query fails
     */
    private void collectTableConstraintCount(String label) throws DataCollectException {
        CachedResult result = executeQuery(Neo4jResources.getConstraintCountForLabelQuery(label));
        if (result.next()) {
            long count = result.getLong("count");
            model.database.getTable(label, true).constraintCount = count;
        }
    }

    /**
     * Method responsible for saving sizes of entities of specific labels
     * @param tableName is label of entities we are interested in
     * @param isNode indicates if we are interested in nodes or edges
     * @throws DataCollectException when some of the help queries fails
     */
    private void collectTableSizes(String tableName, boolean isNode) throws DataCollectException {
        PropertiesSizeData sizes = isNode ? fetchNodePropertiesSize(Neo4jResources.getNodesOfSpecificLabelQuery(tableName)) : fetchEdgePropertiesSize(Neo4jResources.getEdgesOfSpecificLabelQuery(tableName));
        long size = sizes.getByteSize();
        long rowCount = sizes.getCount();

        final var table = model.database.getTable(tableName, true);
        table.sizeInBytes = size;
        table.sizeInPages = (long)Math.ceil((double) size / Neo4jResources.DefaultSizes.PAGE_SIZE);
        table.rowCount = rowCount;
    }

    /**
     * Method which is responsible deciding if entity of specified label is node or edge
     * @param label specified label
     * @return true if label of entity is node label
     * @throws DataCollectException when result of help query is empty
     */
    private boolean _isNodeLabel(String label) throws DataCollectException {
        CachedResult result = executeQuery(Neo4jResources.getIsNodeLabelQuery(label));
        if (result.next()) {
            return result.getBoolean("isNodeLabel");
        }
        throw new DataCollectException("Invalid result of isNodeLabel query");
    }

    /**
     * Method which saves all data for entities used by query
     * @throws DataCollectException when invalid label is used
     */
    private void collectTableData() throws DataCollectException {
        for (String label : model.database.tables.keySet()) {
            boolean isNode = _isNodeLabel(label);
            collectTableConstraintCount(label);
            collectTableSizes(label, isNode);
            collectColumnData(label, isNode);
        }
    }

    /**
     * Method which gets labels over which is this index build
     * @param indexNames specify index by its name
     */
    private void collectTableNameFor(String[] indexNames) {
        model.database.addTable(indexNames[1]);
    }

    /**
     * Method which is responsible for saving index sizes specified by index name over entities
     * @param indexName identifier of index
     * @param indexNames identifier split to array for usage of concrete tokens
     * @param isNode indicates whether the entity is node or edge
     * @throws DataCollectException when some of the help queries will fail
     */
    private void collectIndexSizes(String indexName, String[] indexNames, boolean isNode) throws DataCollectException {
        PropertiesSizeData sizes = isNode ? fetchNodePropertiesSize(Neo4jResources.getNodeAndPropertyQuery(indexNames[1], indexNames[2])) :
                fetchEdgePropertiesSize(Neo4jResources.getEdgeAndPropertyQuery(indexNames[1], indexNames[2]));
        long size = (long) Math.ceil((double) (sizes.getByteSize()) / 3);
        long rowCount = sizes.getCount();

        model.database.getIndex(indexName, true).rowCount = rowCount;
        model.database.getIndex(indexName, true).sizeInBytes = size;
        model.database.getIndex(indexName, true).sizeInPages =
                (long) Math.ceil((double) size / Neo4jResources.DefaultSizes.PAGE_SIZE);
    }

    /**
     * Method for saving all data about used indexes
     * @throws DataCollectException when some of the labels are invalid
     */
    private void collectIndexData() throws DataCollectException {
        for (String inxName : model.database.indexes.keySet()) {
            String[] names = inxName.split(":");

            boolean isNode = _isNodeLabel(names[1]);
            collectTableNameFor(names);
            collectIndexSizes(inxName, names, isNode);
        }
    }

    /**
     * Method which is saving all statistics about result data to dataModel
     * @param result result of main query
     */
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

    /**
     * Public method which triggers collecting of all statistical data and returns result as instance of DataModel
     * @param result main query result
     * @throws DataCollectException when some of the help queries fails or invalid labels were used
     */
    public void collectData(ConsumedResult result) throws DataCollectException {
        collectDatabaseData();
        collectIndexData();
        collectTableData();
        collectResultData(result);
    }

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
