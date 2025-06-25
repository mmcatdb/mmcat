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
import cz.matfyz.wrappermongodb.collector.queryparser.MongoQueryParser;

import cz.matfyz.abstractwrappers.collector.CollectorWrapper;

/**
 * Class representing Wrapper for mongodb database
 */
public final class MongoDBCollectorWrapper implements CollectorWrapper {

    // TODO: do these need to be persisted as state variables, or are local variables enough?
    private final MongoQueryResultParser resultParser;

    private final MongoExplainPlanParser explainPlanParser;

    private final MongoDBProvider provider;

    private final MongoQueryParser queryParser;

    public MongoDBCollectorWrapper(MongoDBProvider provider) {
        this.provider = provider;
        resultParser = new MongoQueryResultParser();
        explainPlanParser = new MongoExplainPlanParser();
        queryParser = new MongoQueryParser(MongoExceptionsFactory.getExceptionsFactory());
    }

    @Override
    public final DataModel executeQuery(QueryContent query) throws WrapperException {
        assert query instanceof MongoDBQuery;
        final var mongoQuery = (MongoDBQuery)query;

        var dataModel = DataModel.CreateForQuery(mongoQuery.toString(), "MongoDB", provider.settings.database());
        var connection = new MongoConnection(provider.getDatabase());

        resultParser.setConnection(connection);

        // Bson a = mongoQuery.pipeline.get(0); // TODO change parsing - how to convert to document from bson?

        var inputQuery = queryParser.parseQueryToCommand(mongoQuery.toString());
        var explainResult = connection.executeWithExplain(inputQuery);

        var mainResult = resultParser.parseResultAndConsume(explainResult.result());
        explainPlanParser.parsePlan(explainResult.plan(), dataModel);

        try {
            var dataCollector = new MongoDataCollector(dataModel, connection, resultParser, provider.settings.database());
            dataCollector.collectData(mainResult);

            ((MongoQueryResultParser)resultParser).removeConnection();
            return dataModel;
        } catch (ConnectionException e) {
            throw MongoExceptionsFactory.getExceptionsFactory().dataCollectorNotInitialized(e);
        }
    }

}
