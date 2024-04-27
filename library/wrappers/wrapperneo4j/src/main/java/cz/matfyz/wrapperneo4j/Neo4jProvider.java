package cz.matfyz.wrapperneo4j;

import org.neo4j.driver.AuthToken;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import org.neo4j.driver.SessionConfig;

/**
 * @author jachymb.bartik
 */
public class Neo4jProvider {

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
