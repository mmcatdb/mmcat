package cz.matfyz.wrapperpostgresql.collector;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;

import java.util.List;
import java.util.Map;

public class PostgreSQLExplainPlanParser {

    /**
     * Parses explain tree and important data saves to DataModel.
     */
    @SuppressWarnings({"unchecked"})
    public void parsePlan(String explainTree, DataModel model) throws ParseException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            List<?> result = objectMapper.readValue(explainTree, List.class);

            for (Object plan : result) {
                if (plan instanceof Map root) {
                    parseTree(root, model);
                }
            }
        } catch (JsonProcessingException e) {
            throw new ParseException(e);
        }
    }

    @SuppressWarnings({"unchecked"})
    private void parseTree(Map<String, Object> root, DataModel model) {
        if (root.containsKey("Execution Time"))
            saveQueryExecutionTime(root, model);
        if (root.containsKey("Plan") && root.get("Plan") instanceof Map node)
            parseSubTree(node, model);
    }

    private void saveQueryExecutionTime(Map<String, Object> root, DataModel model) {
        Object result = root.get("Execution Time");
        if (result instanceof Double time) {
            model.result.executionTimeMillis = time;
        }
    }

    /** Recursively parses the subtree of explain result. */
    @SuppressWarnings({"unchecked"})
    private void parseSubTree(Map<String, Object> root, DataModel model) {
        if (root.get("Node Type") instanceof String nodeType) {
            if (nodeType.contains("Seq Scan")) {
                parseTableName(root, model);
            } else if (nodeType.contains("Index Scan")) {
                parseIndexName(root, model);
            }

            if (root.containsKey("Plans") && root.get("Plans") instanceof List list) {
                for (Object o: list) {
                    if (o instanceof Map node) {
                        parseSubTree(node, model);
                    }
                }
            }
        }
    }

    private void parseTableName(Map<String, Object> node, DataModel model) {
        if (node.get("Relation Name") instanceof String tableName)
            model.database.addTable(tableName);
    }

    private void parseIndexName(Map<String, Object> node, DataModel model) {
        if (node.get("Index Name") instanceof String relName)
            model.database.addIndex(relName);
    }

}
