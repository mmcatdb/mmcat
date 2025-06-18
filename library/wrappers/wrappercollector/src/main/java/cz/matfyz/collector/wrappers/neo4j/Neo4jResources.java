package cz.cuni.matfyz.collector.wrappers.neo4j;

import java.util.List;

/**
 * Class which contains all help queries used for collecting data and constants for calculating sizes etc.
 */
public class Neo4jResources {
    public static final String SYSTEM_NAME = "Neo4j";
    public static String getExplainPlanQuery(String query) {
        return "profile " + query;
    }
    public static String getNodesOfSpecificLabelQuery(String nodeLabel) { return "match (n:" + nodeLabel + ") return n;"; }

    public static String getEdgesOfSpecificLabelQuery(String edgeLabel) { return "match ()-[e:" + edgeLabel + "]->() return e;"; }
    public static String getIndexDataQuery(String indexType, String label, String property) {
        return "show indexes yield name, type, labelsOrTypes, properties where type = \"" + indexType + "\" and \"" + label + "\" in labelsOrTypes and \"" + property + "\" in properties;";
    }
    public static String getNodeAndPropertyQuery(String nodeLabel, String propertyName) {
        return "match (n:" + nodeLabel + ") return n." + propertyName  + ";";
    }
    public static String getEdgeAndPropertyQuery(String edgeLabel, String propertyName) {
        return "match ()-[e:" + edgeLabel + "]->() return e." + propertyName  + ";";
    }
    public static String getConstraintCountForLabelQuery(String label) {
        return "show constraints yield labelsOrTypes where \"" + label + "\" in labelsOrTypes return count(*) as count;";
    }

    public static String getDatabaseSizesQuery() {
        return "call apoc.monitor.store()";
    }
    public static String getPageCacheSizeQuery() {
        return "show settings yield name, value where name=\"server.memory.pagecache.size\";";
    }
    public static String getNodePropertiesForLabelQuery(String label) {
        return "call apoc.meta.nodeTypeProperties({includeLabels: [\"" + label + "\"]}) yield propertyName;";
    }

    public static String getEdgePropertiesForLabelQuery(String label) {
        return "call apoc.meta.relTypeProperties({rels: [\"" + label + "\"]}) yield propertyName;";
    }
    public static String getNodePropertyTypeAndMandatoryQuery(String label, String propertyName) {
        return "call apoc.meta.nodeTypeProperties({includeLabels: [\"" + label + "\"]}) yield propertyName, propertyTypes, mandatory where propertyName = \"" + propertyName + "\" return propertyName, propertyTypes, mandatory;\n";
    }
    public static String getEdgePropertyTypeAndMandatoryQuery(String label, String propertyName) {
        return "call apoc.meta.relTypeProperties({rels: [\"" + label + "\"]}) yield propertyName, propertyTypes, mandatory where propertyName = \"" + propertyName + "\" return propertyName, propertyTypes, mandatory;\n";
    }
    public static String getIsNodeLabelQuery(String label) {
        return "return apoc.meta.nodes.count([\"" + label + "\"]) > 0 as isNodeLabel;";
    }

    public static String getConnectionLink(String host, int port, String datasetName) {
        return "bolt://" + host + ':' + port + '/' + datasetName;
    }

    public static class DefaultSizes {
        public static int getAvgColumnSizeByValue(Object value) {
            if (value instanceof String || value instanceof List)
                return BIG_PROPERTY_SIZE;
            else
                return SMALL_PROPERTY_SIZE;
        }

        public static int getAvgColumnSizeByType(String type) {
            if ("String".equals(type) || "List".equals(type) || type.contains("Array"))
                return BIG_PROPERTY_SIZE;
            else
                return SMALL_PROPERTY_SIZE;
        }

        public static int NODE_SIZE = 15;
        public static int BIG_PROPERTY_SIZE = 128;
        public static int SMALL_PROPERTY_SIZE = 41;
        public static int EDGE_SIZE = 34;
        public static int PAGE_SIZE = 8192;
    }
}
