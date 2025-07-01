package cz.matfyz.wrappermongodb.collector;

import cz.matfyz.abstractwrappers.exception.collector.*;
import cz.matfyz.abstractwrappers.querycontent.QueryContent;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.wrappermongodb.MongoDBProvider;
import cz.matfyz.wrappermongodb.MongoDBQuery;
import cz.matfyz.wrappermongodb.collector.components.MongoConnection;
import cz.matfyz.wrappermongodb.collector.components.MongoDataCollector;
import cz.matfyz.wrappermongodb.collector.components.MongoExplainPlanParser;
import cz.matfyz.wrappermongodb.collector.components.MongoQueryResultParser;

import cz.matfyz.abstractwrappers.collector.CollectorWrapper;

/**
 * Class representing Wrapper for mongodb database
 */
public final class MongoDBCollectorWrapper implements CollectorWrapper {

    // TODO: do these need to be persisted as state variables, or are local variables enough? - local is ok for non-state (or just static)
    private final MongoQueryResultParser resultParser;

    private final MongoExplainPlanParser explainPlanParser;

    private final MongoDBProvider provider;

    // private final MongoQueryParser queryParser;

    public MongoDBCollectorWrapper(MongoDBProvider provider) {
        this.provider = provider;
        resultParser = new MongoQueryResultParser();
        explainPlanParser = new MongoExplainPlanParser();
        // queryParser = new MongoQueryParser();
    }

    @Override
    public final DataModel executeQuery(QueryContent query) throws WrapperException {
        assert query instanceof MongoDBQuery;
        final var mongoQuery = (MongoDBQuery)query;

        var dataModel = new DataModel("MongoDB", mongoQuery.toString());
        var connection = new MongoConnection(provider.getDatabase());

        resultParser.setConnection(connection);

        final var queryResult = connection.database().getCollection(mongoQuery.collection).aggregate(mongoQuery.pipeline);
        final var queryPlan = queryResult.explain();


        // var inputQuery = queryParser.parseQueryToCommand(mongoQuery.toString());
        // var explainResult = connection.executeWithExplain(inputQuery);

        var mainResult = resultParser.parseResultAndConsume(queryResult);
        explainPlanParser.parsePlan(queryPlan, dataModel);

        try {
            var dataCollector = new MongoDataCollector(dataModel, connection, resultParser, provider.settings.database());
            dataCollector.collectData(mainResult);

            resultParser.removeConnection();
            return dataModel;
        } catch (ConnectionException e) {
            throw MongoExceptionsFactory.getExceptionsFactory().dataCollectorNotInitialized(e);
        }
    }

}
