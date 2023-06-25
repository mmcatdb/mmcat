package cz.cuni.matfyz.wrapperpostgresql;

import cz.cuni.matfyz.core.exception.OtherException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLConnectionProvider implements ConnectionProvider {

    private PostgreSQLSettings settings;

    // This class is also meant to be instantiated only once (see the MongoDB wrapper) but it currently doesn't use any caching itself.
    // However, some connection pooling can be added in the future.
    //private Connection connection;

    public PostgreSQLConnectionProvider(PostgreSQLSettings settings) {
        this.settings = settings;
    }

    @Override
    public Connection getConnection() {
        try {
            return DriverManager.getConnection(settings.getConnectionString());
        }
        catch (SQLException e) {
            throw new OtherException(e);
        }
    }

}