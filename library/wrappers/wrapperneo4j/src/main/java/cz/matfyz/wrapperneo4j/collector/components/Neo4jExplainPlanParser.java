package cz.matfyz.wrapperneo4j.collector.components;

import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.collector.components.AbstractExplainPlanParser;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;
import org.neo4j.driver.summary.Plan;
import org.neo4j.driver.summary.ResultSummary;

import java.util.concurrent.TimeUnit;

public class Neo4jExplainPlanParser extends AbstractExplainPlanParser<ResultSummary> {

    public Neo4jExplainPlanParser(WrapperExceptionsFactory exceptionsFactory) {
        super(exceptionsFactory);
    }

    /**
     * Method which saves execution time from explain to data model
     * @param model DataModel to save parsed data
     * @param summary part of explain result
     */
    private void _parseExecutionTime(DataModel model, ResultSummary summary ) {
        long nanoseconds = summary.resultAvailableAfter(TimeUnit.NANOSECONDS);
        model.setResultExecutionTime((double) nanoseconds / (1_000_000));
    }

    /**
     * Method for getting all used labels by main query
     * @param model DataModel to save parsed data
     * @param operator represents one node of explain tree
     */
    private void _parseNodeTableName(DataModel model, Plan operator) {
        String details = operator.arguments().get("Details").asString();
        String tableName = details.split(":")[1];
        model.addTable(tableName);
    }

    /**
     * Method for parsing details to get edges label
     * @param details to be parsed
     * @return name of label as string
     */
    private String _parseRelationDetailsForLabel(String details) {
        StringBuilder buffer = new StringBuilder();
        Boolean isInEdge = null;
        for (char ch : details.toCharArray()) {
            if (isInEdge == null) {
                if (ch == '[')
                    isInEdge = false;
            } else if (!isInEdge){
                if (ch == ':')
                    isInEdge = true;
            } else {
                if (ch == ']')
                    break;
                else
                    buffer.append(ch);
            }
        }
        return buffer.toString();
    }

    /**
     * Method for parsing edge labels used by query
     * @param model to save labels
     * @param operator node of explain tree
     */
    private void _parseRelationTableName(DataModel model, Plan operator) {
        String details = operator.arguments().get("Details").asString();
        String tableName = _parseRelationDetailsForLabel(details);
        model.addTable(tableName);
    }

    /**
     * Method parsing index identifier to tokens
     * @param identifier index identifier created from information such as label, property name and type
     * @return string array of tokens from index
     */
    private String[] _parseIndexIdentifier(String identifier) {
        StringBuilder label = new StringBuilder();
        StringBuilder prop = new StringBuilder();
        boolean afterParenthesis = false;
        for (char ch : identifier.toCharArray()) {
            if (ch == '(') {
                afterParenthesis = true;
            } else if (afterParenthesis) {
                if (ch == ')') {
                    break;
                } else {
                    prop.append(ch);
                }
            } else {
                label.append(ch);
            }
        }

        return new String[] { label.toString(), prop.toString() };
    }

    /**
     * Method for getting index identifier from explain relevant to query
     * @param model DataModel to save data
     * @param operator explain tree node
     */
    private void _parseIndexName(DataModel model, Plan operator) {
        String[] details = operator.arguments().get("Details").asString().split(" ");
        String indexType = details[0];
        String[] indexIdentifiers = _parseIndexIdentifier(details[2].split(":")[1]);

        model.addIndex(indexType + ':' + indexIdentifiers[0] + ':' + indexIdentifiers[1]);
    }

    /**
     * Method for parsing types of different Neo4j operators
     * @param model dataModel to save results
     * @param operator actual explain tree node to be parsed
     */
    private void _parseOperator(DataModel model, Plan operator) {
        if (operator.operatorType().contains("NodeByLabel")) {
            _parseNodeTableName(model, operator);
        } else if (operator.operatorType().contains("RelationshipType")) {
            _parseRelationTableName(model, operator);
        } else if (operator.operatorType().contains("Index")) {
            _parseIndexName(model, operator);
        }

        for (Plan child : operator.children()) {
            _parseOperator(model, child);
        }
    }

    /**
     * Method for parsing explain for important information
     * @param model instance of DataModel where collected information are stored
     * @param plan explain tree to be parsed
     * @throws ParseException is there to implement abstract method
     */
    @Override
    public void parsePlan(ResultSummary plan, DataModel model) throws ParseException {
        _parseExecutionTime(model, plan);
        _parseOperator(model, plan.profile());
    }
}
