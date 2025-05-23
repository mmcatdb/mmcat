package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractDatasourceProvider;
import cz.matfyz.core.exception.OtherException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class PostgreSQLProvider implements AbstractDatasourceProvider {

    public final PostgreSQLSettings settings;

    // This class is also meant to be instantiated only once (see the MongoDB wrapper) but it currently doesn't use any caching itself.
    // However, some connection pooling can be added in the future.

    public PostgreSQLProvider(PostgreSQLSettings settings) {
        this.settings = settings;
    }

    public Connection getConnection() {
        try {
            return DriverManager.getConnection(settings.createConnectionString());
        }
        catch (SQLException e) {
            throw new OtherException(e);
        }
    }

    public boolean isStillValid(Object settings) {
        // We always create a new connection so we don't need to cache anything.
        return false;
    }

    public void close() {
        // We don't need to close anything because we don't cache anything.
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PostgreSQLSettings(
        String host,
        String port,
        String database,
        String username,
        String password,
        boolean isWritable,
        boolean isQueryable,
        boolean isClonable
    ) {

        String createConnectionString() {
            return new StringBuilder()
                .append("jdbc:postgresql://")
                .append(host)
                .append(":")
                .append(port)
                .append("/")
                .append(database)
                .append("?user=")
                .append(username)
                .append("&password=")
                .append(password)
                .toString();
        }

    }

}
