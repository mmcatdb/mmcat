package cz.matfyz.tests.example.benchmark.caldotcom;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.DatasourceProvider;
import cz.matfyz.tests.example.common.TestDatasource;
import cz.matfyz.wrappermongodb.MongoDBControlWrapper;
import cz.matfyz.wrapperneo4j.Neo4jControlWrapper;
import cz.matfyz.wrapperpostgresql.PostgreSQLControlWrapper;

public class Datasources {

    public final SchemaCategory schema = Schema.newSchema();

    private static final DatasourceProvider datasourceProvider = new DatasourceProvider("tests", "benchmark_caldotcom");

    private TestDatasource<PostgreSQLControlWrapper> postgreSQL;
    private TestDatasource<MongoDBControlWrapper> mongoDB;
    private TestDatasource<Neo4jControlWrapper> neo4j;

    public TestDatasource<PostgreSQLControlWrapper> createNewPostgreSQL() {
        return datasourceProvider.createPostgreSQL(PostgreSQL.datasource.identifier, schema, null);
    }

    public TestDatasource<PostgreSQLControlWrapper> postgreSQL() {
        if (postgreSQL == null) {
            postgreSQL = createNewPostgreSQL()
            .addMapping(PostgreSQL.team(schema))
            .addMapping(PostgreSQL.role(schema))
            .addMapping(PostgreSQL.attribute(schema))
            .addMapping(PostgreSQL.attributeOption(schema))
            .addMapping(PostgreSQL.user(schema))
            .addMapping(PostgreSQL.membership(schema))
            .addMapping(PostgreSQL.teamOrgScope(schema))
            .addMapping(PostgreSQL.attributeToUser(schema))
            .addMapping(PostgreSQL.verifiedEmail(schema))
            .addMapping(PostgreSQL.schedule(schema))
            .addMapping(PostgreSQL.eventType(schema))
            .addMapping(PostgreSQL.availability(schema))
            .addMapping(PostgreSQL.outOfOffice(schema))
            .addMapping(PostgreSQL.hostGroup(schema))
            .addMapping(PostgreSQL.eventHost(schema))
            .addMapping(PostgreSQL.userOnEventType(schema))
            .addMapping(PostgreSQL.feature(schema))
            .addMapping(PostgreSQL.userFeatures(schema))
            .addMapping(PostgreSQL.teamFeatures(schema))
            .addMapping(PostgreSQL.workflow(schema))
            .addMapping(PostgreSQL.workflowStep(schema))
            .addMapping(PostgreSQL.workflowsOnEventTypes(schema))
            .addMapping(PostgreSQL.workflowsOnTeams(schema))
            .addMapping(PostgreSQL.booking(schema))
            .addMapping(PostgreSQL.attendee(schema));
        }

        return postgreSQL;
    }

    public TestDatasource<MongoDBControlWrapper> createNewMongoDB() {
        return datasourceProvider.createMongoDB(MongoDB.datasource.identifier, schema, null);
    }

    public TestDatasource<MongoDBControlWrapper> mongoDB() {
        if (mongoDB == null) {
            mongoDB = createNewMongoDB()
                .addMapping(MongoDB.team(schema))
                .addMapping(MongoDB.user(schema))
                .addMapping(MongoDB.schedule(schema))
                .addMapping(MongoDB.eventType(schema))
                .addMapping(MongoDB.hostGroup(schema))
                .addMapping(MongoDB.feature(schema))
                .addMapping(MongoDB.workflow(schema))
                .addMapping(MongoDB.booking(schema));
            }

        return mongoDB;
    }

    public TestDatasource<Neo4jControlWrapper> createNewNeo4j() {
        return datasourceProvider.createNeo4j(Neo4j.datasource.identifier, schema, null);
    }

    public TestDatasource<Neo4jControlWrapper> neo4j() {
        if (neo4j == null) {
            neo4j = createNewNeo4j()
                .addMapping(Neo4j.team(schema))
                .addMapping(Neo4j.team_parent(schema))
                .addMapping(Neo4j.role(schema))
                .addMapping(Neo4j.team_role(schema))
                .addMapping(Neo4j.attribute(schema))
                .addMapping(Neo4j.team_attribute(schema))
                .addMapping(Neo4j.attributeOption(schema))
                .addMapping(Neo4j.attribute_option(schema))
                .addMapping(Neo4j.user(schema))
                .addMapping(Neo4j.membership(schema))
                .addMapping(Neo4j.membership_user(schema))
                .addMapping(Neo4j.membership_team(schema))
                .addMapping(Neo4j.membership_role(schema))
                .addMapping(Neo4j.team_org(schema))
                .addMapping(Neo4j.attributeToUser(schema))
                .addMapping(Neo4j.verifiedEmail(schema))
                .addMapping(Neo4j.user_email(schema))
                .addMapping(Neo4j.team_email(schema))
                .addMapping(Neo4j.schedule(schema))
                .addMapping(Neo4j.user_schedule(schema))
                .addMapping(Neo4j.eventType(schema))
                .addMapping(Neo4j.team_eventType(schema))
                .addMapping(Neo4j.eventType_owner(schema))
                .addMapping(Neo4j.eventType_parent(schema))
                .addMapping(Neo4j.schedule_eventType(schema))
                .addMapping(Neo4j.availability(schema))
                .addMapping(Neo4j.availability_user(schema))
                .addMapping(Neo4j.availability_eventType(schema))
                .addMapping(Neo4j.availability_schedule(schema))
                .addMapping(Neo4j.outOfOffice(schema))
                .addMapping(Neo4j.hostGroup(schema))
                .addMapping(Neo4j.hostGroup_eventType(schema))
                .addMapping(Neo4j.eventHost(schema))
                .addMapping(Neo4j.hostGroup_host(schema))
                .addMapping(Neo4j.host_user(schema))
                .addMapping(Neo4j.host_member(schema))
                .addMapping(Neo4j.host_eventType(schema))
                .addMapping(Neo4j.userOnEventType(schema))
                .addMapping(Neo4j.feature(schema))
                .addMapping(Neo4j.userFeatures(schema))
                .addMapping(Neo4j.teamFeatures(schema))
                .addMapping(Neo4j.workflow(schema))
                .addMapping(Neo4j.workflow_user(schema))
                .addMapping(Neo4j.workflow_team(schema))
                .addMapping(Neo4j.workflowStep(schema))
                .addMapping(Neo4j.workflow_step(schema))
                .addMapping(Neo4j.workflowsOnEventTypes(schema))
                .addMapping(Neo4j.workflowsOnTeams(schema))
                .addMapping(Neo4j.booking(schema))
                .addMapping(Neo4j.booking_user(schema))
                .addMapping(Neo4j.booking_eventType(schema))
                .addMapping(Neo4j.attendee(schema))
                .addMapping(Neo4j.booking_attendee(schema));
            }

        return neo4j;
    }

}
