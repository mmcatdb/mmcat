package cz.matfyz.wrappermongodb.collector.components;

import cz.matfyz.core.collector.DataModel;
import cz.matfyz.abstractwrappers.collector.components.AbstractExplainPlanParser;
import cz.matfyz.abstractwrappers.exception.collector.ParseException;
import cz.matfyz.abstractwrappers.exception.collector.WrapperExceptionsFactory;
import cz.matfyz.abstractwrappers.exception.collector.WrapperUnsupportedOperationException;
import org.bson.Document;

public class MongoExplainPlanParser extends AbstractExplainPlanParser<Document> {

    public MongoExplainPlanParser(WrapperExceptionsFactory exceptionsFactory) {
        super(exceptionsFactory);
    }

    /**
     * Method which parses collection name of query
     * @param model instance of DataModel where the name will be saved
     * @param command node of explain result
     */
    private void _parseTableNames(DataModel model, Document command) throws WrapperUnsupportedOperationException {
        if (command.containsKey("find")) {
            String collectionName = command.getString("find");
            if (collectionName != null)
                model.addTable(collectionName);
        }

        if (command.containsKey("aggregate")) {
            throw getExceptionsFactory().unsupportedOperation("aggregate");
        }
    }

    /**
     * Method which parses a stage of explain plan
     * @param model instance of DataModel where results are saved
     * @param stage actual stage to be parsed
     */
    private void _parseStage(DataModel model, Document stage) throws ParseException {
        if ("IXSCAN".equals(stage.getString("stage"))) {
            String indexName = stage.getString("indexName");
            if (indexName != null) {
                model.addIndex(indexName);
            }
        }
        if (stage.containsKey("inputStage")) {
            _parseStage(model, stage.get("inputStage", Document.class));
        }
    }

    /**
     * Method for parsing statistics about execution
     * @param model instance of DataModel where will all results be saved
     * @param node document from which the statistics will be parsed (especially the execution time)
     */
    private void _parseExecutionStats(DataModel model, Document node) throws ParseException {
        if (node.getBoolean("executionSuccess")) {
            model.setResultExecutionTime(Double.valueOf(node.getInteger("executionTimeMillis")));
        }
    }

    /**
     * Method for parsing explain plan and consume it into model
     * @param model instance of DataModel where collected information are stored
     * @param plan plan to be parsed
     * @throws ParseException is thrown some parsing problem occur
     */
    @Override
    public void parsePlan(Document plan, DataModel model) throws ParseException, WrapperUnsupportedOperationException {
        _parseTableNames(model, plan.get("command", Document.class));
        _parseExecutionStats(model, plan.get("executionStats", Document.class));
        _parseStage(
                model,
                plan.get("queryPlanner", Document.class)
                        .get("winningPlan", Document.class)
        );
    }
}
