package cz.matfyz.wrapperneo4j.collector;

import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import org.neo4j.driver.summary.Plan;
import org.neo4j.driver.summary.ResultSummary;

import java.util.concurrent.TimeUnit;

public class Neo4jExplainPlanParser {

    /**
     * Parsing explain for important information.
     * @param plan explain tree to be parsed
     */
    public void parsePlan(ResultSummary plan, DataModel model) throws ParseException {
        parseExecutionTime(model, plan);
        parseOperator(model, plan.profile());
    }

    private void parseExecutionTime(DataModel model, ResultSummary summary ) {
        long nanoseconds = summary.resultAvailableAfter(TimeUnit.NANOSECONDS);
        model.result.executionTimeMillis = (double) nanoseconds / (1_000_000);
    }

    private void parseOperator(DataModel model, Plan operator) {
        if (operator.operatorType().contains("NodeByLabel")) {
            parseNodeTableName(model, operator);
        } else if (operator.operatorType().contains("RelationshipType")) {
            parseRelationTableName(model, operator);
        } else if (operator.operatorType().contains("Index")) {
            parseIndexName(model, operator);
        }

        for (Plan child : operator.children()) {
            parseOperator(model, child);
        }
    }

    private void parseNodeTableName(DataModel model, Plan operator) {
        String details = operator.arguments().get("Details").asString();
        String tableName = details.split(":")[1];
        model.database.addTable(tableName);
    }

    private void parseRelationTableName(DataModel model, Plan operator) {
        String details = operator.arguments().get("Details").asString();
        String tableName = parseRelationDetailsForLabel(details);
        model.database.addTable(tableName);
    }

    private String parseRelationDetailsForLabel(String details) {
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

    private void parseIndexName(DataModel model, Plan operator) {
        String[] details = operator.arguments().get("Details").asString().split(" ");
        String indexType = details[0];
        String[] indexIdentifiers = parseIndexIdentifier(details[2].split(":")[1]);

        model.database.addIndex(indexType + ':' + indexIdentifiers[0] + ':' + indexIdentifiers[1]);
    }

    private String[] parseIndexIdentifier(String identifier) {
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

}
