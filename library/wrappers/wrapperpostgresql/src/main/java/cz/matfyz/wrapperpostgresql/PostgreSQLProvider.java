package cz.matfyz.wrapperpostgresql;

import cz.matfyz.core.exception.OtherException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLProvider {

    public final PostgreSQLSettings settings;

    // This class is also meant to be instantiated only once (see the MongoDB wrapper) but it currently doesn't use any caching itself.
    // However, some connection pooling can be added in the future.
    //private Connection connection;

    public PostgreSQLProvider(PostgreSQLSettings settings) {
        this.settings = settings;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(settings.getConnectionString());
        }
        catch (SQLException e) {
            throw new OtherException(e);
        }
    }

}
