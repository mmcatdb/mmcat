package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractDatasourceProvider;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

public class Neo4jProvider implements AbstractDatasourceProvider {

    public final Neo4jSettings settings;

    // The driver itself handles connection pooling so there should be only one driver (with given connection string) per application.
    // This also means that there should be at most one instance of this class so it should be cached somewhere.
    private Driver driver;

    public Neo4jProvider(Neo4jSettings settings) {
        this.settings = settings;
    }

    public Session getSession() {
        if (driver == null)
            driver = GraphDatabase.driver(settings.createConnectionString(), settings.createAuthToken());

        return driver.session(SessionConfig.forDatabase(settings.database));
    }

    public boolean isStillValid(Object settings) {
        if (!(settings instanceof Neo4jSettings neo4jSettings))
            return false;

        return this.settings.host.equals(neo4jSettings.host)
            && this.settings.port.equals(neo4jSettings.port)
            && this.settings.database.equals(neo4jSettings.database)
            && this.settings.isWritable == neo4jSettings.isWritable
            && this.settings.isQueryable == neo4jSettings.isQueryable;
    }

    public void close() {
        if (driver != null)
            driver.close();
    }

    public record Neo4jSettings(
        String host,
        String port,
        String database,
        String username,
        String password,
        boolean isWritable,
        boolean isQueryable
    ) {

        String createConnectionString() {
            return new StringBuilder()
                .append("bolt://")
                .append(host)
                .append(":")
                .append(port)
                .toString();
        }

        AuthToken createAuthToken() {
            return AuthTokens.basic(username, password);
        }

    }

}
