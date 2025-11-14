package cz.matfyz.wrappermongodb.collector;

import cz.matfyz.core.collector.DataModel;
import org.bson.Document;

public class MongoDBExplainPlanParser {

    /**
     * Parses and consumes explain plan.
     */
    public void parsePlan(Document plan, DataModel model) {
        parseTableNames(model, plan.get("command", Document.class));
        parseExecutionStats(model, plan.get("executionStats", Document.class));
        parseStage(model, plan.get("queryPlanner", Document.class).get("winningPlan", Document.class));
    }

    private void parseTableNames(DataModel model, Document command) {
        String collectionName = null;
        if (command.containsKey("find")) {
            collectionName = command.getString("find");
        } else if (command.containsKey("aggregate")) {
            collectionName = command.getString("aggregate");
        }

        model.database.addTable(collectionName);
    }

    private void parseExecutionStats(DataModel model, Document node) {
        if (node.getBoolean("executionSuccess")) {
            model.result.executionTimeMillis = Double.valueOf(node.getInteger("executionTimeMillis")); // TODO: change to getDouble?
        }
    }

    private void parseStage(DataModel model, Document stage) {
        if ("IXSCAN".equals(stage.getString("stage"))) {
            String indexName = stage.getString("indexName");
            if (indexName != null) {
                model.database.addIndex(indexName);
            }
        }
        if (stage.containsKey("inputStage")) {
            parseStage(model, stage.get("inputStage", Document.class));
        }
    }

}
