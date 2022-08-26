package cz.cuni.matfyz.wrapperpostgresql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLConnectionProvider implements ConnectionProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLConnectionProvider.class);

    private PostgreSQLSettings settings;

    // This class is also meant to be instantiated only once (see the MongoDB wrapper) but it currently doesn't use any caching itself.
    // However, some connection pooling can be added in the future.
    //private Connection connection;

    public PostgreSQLConnectionProvider(PostgreSQLSettings settings) {
        this.settings = settings;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(settings.getConnectionString());
        }
        catch (SQLException exception) {
            LOGGER.error("Cannot create connection to PostgreSQL.", exception);
        }

        return null;
    }

}