package cz.matfyz.wrappermongodb.collector.components;

import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperUnsupportedOperationException;
import org.bson.Document;

public class MongoExplainPlanParser {

    /**
     * Method which parses collection name of query
     * @param model instance of DataModel where the name will be saved
     * @param command node of explain result
     */
    private void parseTableNames(DataModel model, Document command) throws WrapperUnsupportedOperationException {
        String collectionName = null;
        if (command.containsKey("find")) {
            collectionName = command.getString("find");
        } else if (command.containsKey("aggregate")) {
            collectionName = command.getString("aggregate");
        }

        model.addTable(collectionName);
    }

    /**
     * Method which parses a stage of explain plan
     * @param model instance of DataModel where results are saved
     * @param stage actual stage to be parsed
     */
    private void parseStage(DataModel model, Document stage) throws ParseException {
        if ("IXSCAN".equals(stage.getString("stage"))) {
            String indexName = stage.getString("indexName");
            if (indexName != null) {
                model.addIndex(indexName);
            }
        }
        if (stage.containsKey("inputStage")) {
            parseStage(model, stage.get("inputStage", Document.class));
        }
    }

    /**
     * Method for parsing statistics about execution
     * @param model instance of DataModel where will all results be saved
     * @param node document from which the statistics will be parsed (especially the execution time)
     */
    private void parseExecutionStats(DataModel model, Document node) throws ParseException {
        if (node.getBoolean("executionSuccess")) {
            model.executionTimeMillis = Double.valueOf(node.getInteger("executionTimeMillis")); // TODO: change to getDouble?
        }
    }

    /**
     * Method for parsing explain plan and consume it into model
     * @param model instance of DataModel where collected information are stored
     * @param plan plan to be parsed
     * @throws ParseException is thrown some parsing problem occur
     */
    public void parsePlan(Document plan, DataModel model) throws ParseException, WrapperUnsupportedOperationException {
        parseTableNames(model, plan.get("command", Document.class));
        parseExecutionStats(model, plan.get("executionStats", Document.class));
        parseStage(
                model,
                plan.get("queryPlanner", Document.class)
                    .get("winningPlan", Document.class)
        );
    }
}
