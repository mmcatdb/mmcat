package cz.cuni.matfyz.collector.wrappers.program;

import com.fasterxml.jackson.core.JsonProcessingException;
import cz.matfyz.core.collector.DataModel;
import cz.matfyz.core.collector.DataModelException;
import cz.matfyz.abstractwrappers.collector.AbstractWrapper;
import cz.matfyz.abstractwrappers.exception.collector.WrapperException;
import cz.matfyz.wrappermongodb.collector.MongoResources;
import cz.matfyz.wrappermongodb.collector.MongoWrapper;
import cz.cuni.matfyz.collector.wrappers.neo4j.Neo4jResources;
import cz.cuni.matfyz.collector.wrappers.neo4j.Neo4jWrapper;
import cz.matfyz.wrapperpostgresql.collector.PostgresResources;
import cz.matfyz.wrapperpostgresql.collector.PostgresWrapper;
import java.util.logging.Logger;
public class WrappersProgram {

    private static final Logger _logger = Logger.getLogger(WrappersProgram.class.getName());
    private static void mongoTests() throws WrapperException, DataModelException {
        MongoWrapper mongoWrapper = new MongoWrapper(
                new AbstractWrapper.ConnectionData(
                        "localhost",
                        27017,
                        MongoResources.SYSTEM_NAME,
                        "test",
                        "",
                        ""
                )
        );

        DataModel mongoModel = mongoWrapper.executeQuery("db.costumers.find()");
        System.out.println(mongoModel.toJson());

        // It will fail for this command, because we do not support count() function
        //mongoModel = mongoWrapper.executeQuery("db.costumers.find().count()");
        //System.out.println(mongoModel.toJson());

        mongoModel = mongoWrapper.executeQuery("db.costumers.find({}, {customer_id: 1, customer_name: 1})");
        System.out.println(mongoModel.toJson());

        mongoModel = mongoWrapper.executeQuery("db.costumers.find({\"customer_id\": { \"$gt\": 30 }})");
        System.out.println(mongoModel.toJson());

        // It will fail for this command, because we do not support aggregate() function
        //mongoModel = mongoWrapper.executeQuery("db.costumers.aggregate()");
        //System.out.println(mongoModel.toJson());
    }
    public static void main(String[] args) {

        try {

            mongoTests();

            Neo4jWrapper neo4jWrapper = new Neo4jWrapper(
                    new AbstractWrapper.ConnectionData(
                            "localhost",
                            7687,
                            Neo4jResources.SYSTEM_NAME,
                            "neo4j",
                            "neo4j",
                            "password"
                    )
            );

            DataModel neo4jModel = neo4jWrapper.executeQuery("MATCH (n) RETURN n;");
            System.out.println(neo4jModel.toJson());

            PostgresWrapper postgresWrapper = new PostgresWrapper(
                    new AbstractWrapper.ConnectionData(
                            "localhost",
                            5432,
                            PostgresResources.SYSTEM_NAME,
                            "sales",
                            "postgres",
                            "password"
                    )
            );

            DataModel postgresModel = postgresWrapper.executeQuery("SELECT * FROM fact_trendings;");
            System.out.println(postgresModel.toJson());

        } catch (WrapperException | DataModelException e) {
            _logger.severe(e.getMessage());
        }

    }
}
