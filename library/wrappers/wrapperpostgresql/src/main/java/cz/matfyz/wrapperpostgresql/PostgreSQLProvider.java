package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractDatasourceProvider;
import cz.matfyz.core.exception.OtherException;

import java.sql.Connection;
import java.sql.SQLException;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.checkerframework.checker.nullness.qual.Nullable;

public class PostgreSQLProvider implements AbstractDatasourceProvider {

    final PostgreSQLSettings settings;

    // The dataSource itself handles connection pooling so there should be only one dataSource (with given connection string) per application.
    // This also means that there should be at most one instance of this class so it should be cached somewhere.
    private @Nullable HikariDataSource dataSource;

    public PostgreSQLProvider(PostgreSQLSettings settings) {
        this.settings = settings;
    }

    public Connection getConnection() {
        try {
            if (dataSource == null) {
                final var config = new HikariConfig();
                config.setJdbcUrl(settings.createConnectionString());
                config.setReadOnly(!settings.isWritable);
                dataSource = new HikariDataSource(config);
            }

            return dataSource.getConnection();
        }
        catch (SQLException e) {
            throw new OtherException(e);
        }
    }

    @Override public boolean isStillValid(Object settings) {
        if (!(settings instanceof PostgreSQLSettings postgreSqlSettings))
            return false;

        return this.settings.host.equals(postgreSqlSettings.host)
            && this.settings.port.equals(postgreSqlSettings.port)
            && this.settings.database.equals(postgreSqlSettings.database)
            && this.settings.isWritable == postgreSqlSettings.isWritable
            && this.settings.isQueryable == postgreSqlSettings.isQueryable;
    }

    @Override public void close() {
        if (dataSource != null)
            dataSource.close();
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
