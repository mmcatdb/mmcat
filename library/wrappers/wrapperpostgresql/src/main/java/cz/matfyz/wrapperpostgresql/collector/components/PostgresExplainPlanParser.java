package cz.matfyz.wrapperpostgresql.collector.components;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.collector.components.AbstractExplainPlanParser;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;

import java.util.List;
import java.util.Map;

public class PostgresExplainPlanParser extends AbstractExplainPlanParser<String> {


    public PostgresExplainPlanParser(WrapperExceptionsFactory exceptionsFactory) {
        super(exceptionsFactory);
    }

    /**
     * Method which saves the execution time of query to model
     * @param root explain trees root
     * @param model model to save data
     */
    private void _saveExecTime(Map<String, Object> root, DataModel model) {
        Object result = root.get("Execution Time");
        if (result instanceof Double time) {
            model.setResultExecutionTime(time);
        }
    }

    /**
     * Method which parser table names from explain tree to model
     * @param node explain trees node
     * @param model model to save data
     */
    private void _parseTableName(Map<String, Object> node, DataModel model) {
        if (node.get("Relation Name") instanceof String tableName) {
            model.addTable(tableName);
        }
    }

    /**
     * Method which parses index names from explain tree to model
     * @param node explain trees node
     * @param model model to save data
     */
    private void _parseIndexName(Map<String, Object> node, DataModel model) {
        if (node.get("Index Name") instanceof String relName) {
            model.addIndex(relName);
        }
    }

    /**
     * Method which parses tho root of the explain tree
     * @param root root of the explain tree
     * @param model model to save data
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void _parseTree(Map<String, Object> root, DataModel model) {
        if (root.containsKey("Execution Time")) {
            _saveExecTime(root, model);
        }
        if (root.containsKey("Plan") && root.get("Plan") instanceof Map node) {
            _parseSubTree(node, model);
        }
    }

    /**
     * Method which recursively parses the subtree of explain result
     * @param root actual node of explain tree to be parsed
     * @param model model to save important data
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void _parseSubTree(Map<String, Object> root, DataModel model) {
        if (root.get("Node Type") instanceof String nodeType) {
            if (nodeType.contains("Seq Scan")) {
                _parseTableName(root, model);
            } else if (nodeType.contains("Index Scan")) {
                _parseIndexName(root, model);
            }

            if (root.containsKey("Plans") && root.get("Plans") instanceof List list) {
                for(Object o: list) {
                    if (o instanceof Map node) {
                        _parseSubTree(node, model);
                    }
                }
            }
        }
    }


    /**
     * Method which parse explain tree and important data saves to DataModel
     * @param model instance of DataModel where collected information are stored
     * @param explainTree explain tree to be parsed
     * @throws ParseException when JsonProcessingException occurs during the process
     */
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void parsePlan(String explainTree, DataModel model) throws ParseException {
        try {
            ObjectMapper objectMapper = new ObjectMapper();

            List result = objectMapper.readValue(explainTree, List.class);

            for (Object plan: result) {
                if (plan instanceof Map root) {
                    _parseTree(root, model);
                }
            }
        } catch (JsonProcessingException e) {
            throw new ParseException(e);
        }
    }


}
