package cz.matfyz.tests.example.caldotcom;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.DatasourceProvider;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;

public class Datasources {

    public final SchemaCategory schema = Schema.newSchema();

    private static final DatasourceProvider datasourceProvider = new DatasourceProvider("tests", "caldotcom");

    private TestDatasource<MongoDBControlWrapper> mongoDB;
    private TestDatasource<PostgreSQLControlWrapper> postgreSQL;

    public TestDatasource<MongoDBControlWrapper> mongoDB() {
        if (mongoDB == null) {
            mongoDB = createNewMongoDB()
                .addMapping(MongoDB.attendee(schema))
                .addMapping(MongoDB.booking(schema))
                .addMapping(MongoDB.eventType(schema))
                .addMapping(MongoDB.membership(schema))
                .addMapping(MongoDB.role(schema))
                .addMapping(MongoDB.team(schema))
                .addMapping(MongoDB.teamOrgScope(schema))
                .addMapping(MongoDB.user(schema))
                .addMapping(MongoDB.userOnEventType(schema))
                .addMapping(MongoDB.workflow(schema))
                .addMapping(MongoDB.workflowsOnEventTypes(schema));
        }

        return mongoDB;
    }

    public TestDatasource<PostgreSQLControlWrapper> createNewPostgreSQL() {
        return datasourceProvider.createPostgreSQL(PostgreSQL.datasource.identifier, schema, null);
    }

        public TestDatasource<PostgreSQLControlWrapper> postgreSQL() {
        if (postgreSQL == null) {
            postgreSQL = createNewPostgreSQL()
                .addMapping(PostgreSQL.attendee(schema))
                .addMapping(PostgreSQL.booking(schema))
                .addMapping(PostgreSQL.eventType(schema))
                .addMapping(PostgreSQL.membership(schema))
                .addMapping(PostgreSQL.role(schema))
                .addMapping(PostgreSQL.team(schema))
                .addMapping(PostgreSQL.teamOrgScope(schema))
                .addMapping(PostgreSQL.user(schema))
                .addMapping(PostgreSQL.userOnEventType(schema))
                .addMapping(PostgreSQL.workflow(schema))
                .addMapping(PostgreSQL.workflowsOnEventTypes(schema));
        }

        return postgreSQL;
    }

    public TestDatasource<MongoDBControlWrapper> createNewMongoDB() {
        return datasourceProvider.createMongoDB(MongoDB.datasource.identifier, schema, null);
    }

}
