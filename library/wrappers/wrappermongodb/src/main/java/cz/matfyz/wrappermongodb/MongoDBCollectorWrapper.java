package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractCollectorWrapper;
import cz.matfyz.abstractwrappers.exception.collector.*;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.wrappermongodb.collector.MongoDBDataCollector;
import cz.matfyz.wrappermongodb.collector.MongoDBExplainPlanParser;
import cz.matfyz.wrappermongodb.collector.MongoDBQueryResultParser;

/**
 * Class representing Wrapper for mongodb database
 */
public final class MongoDBCollectorWrapper implements AbstractCollectorWrapper {

    private final MongoDBProvider provider;
    private final String datasourceIdentifier;

    // TODO: do these need to be persisted as state variables, or are local variables enough? - local is ok for non-state (or just static)
    private final MongoDBQueryResultParser resultParser;

    private final MongoDBExplainPlanParser explainPlanParser;

    public MongoDBCollectorWrapper(MongoDBProvider provider, String datasourceIdentifier) {
        this.provider = provider;
        this.datasourceIdentifier = datasourceIdentifier;
        resultParser = new MongoDBQueryResultParser(provider);
        explainPlanParser = new MongoDBExplainPlanParser();
    }

    @Override
    public final DataModel executeQuery(QueryContent query) throws WrapperException {
        assert query instanceof MongoDBQuery;
        final var mongoQuery = (MongoDBQuery) query;

        final var dataModel = new DataModel(datasourceIdentifier, mongoQuery.toString());

        final var queryResult = provider.getDatabase().getCollection(mongoQuery.collection).aggregate(mongoQuery.pipeline);
        final var queryPlan = queryResult.explain();

        // var inputQuery = queryParser.parseQueryToCommand(mongoQuery.toString());
        // var explainResult = connection.executeWithExplain(inputQuery);

        final var mainResult = resultParser.parseResultAndConsume(queryResult);
        explainPlanParser.parsePlan(queryPlan, dataModel);

        final var dataCollector = new MongoDBDataCollector(dataModel, provider, resultParser);
        dataCollector.collectData(mainResult);

        return dataModel;
    }

}
