package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.abstractwrappers.BaseControlWrapper;
import cz.matfyz.abstractwrappers.exception.ExecuteException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Collection;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgreSQLControlWrapper extends BaseControlWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLControlWrapper.class);

    @Override protected DatasourceType getType() {
        return DatasourceType.postgresql;
    }

    private final PostgreSQLProvider provider;

    public PostgreSQLControlWrapper(PostgreSQLProvider provider) {
        super(provider.settings.isWritable(), provider.settings.isQueryable());
        this.provider = provider;
    }

    @Override public void execute(Collection<AbstractStatement> statements) {
        try (
            Connection connection = provider.getConnection();
        ) {
            // TODO transactions?
            for (final var statement : statements) {
                try (
                    PreparedStatement preparedStatement = connection.prepareStatement(statement.getContent());
                ) {
                    LOGGER.info("Execute PostgreSQL statement:\n{}", preparedStatement);
                    preparedStatement.execute();
                }
            }
        }
        catch (Exception e) {
            throw new ExecuteException(e, statements);
        }
    }

    @Override public void execute(Path path) {
        try {
            String script = Files.readString(path);
            // Split the queries by the ; character, followed by any number of whitespaces and newline.
            final var statements = Stream.of(script.split(";\\s*\n"))
                .map(String::strip)
                .filter(s -> !s.isBlank())
                .map(s -> (AbstractStatement) StringStatement.create(s))
                .toList();

            execute(statements);
        }
        catch (IOException e) {
            throw new ExecuteException(e, path);
        }
    }

    @Override public PostgreSQLDDLWrapper getDDLWrapper() {
        return new PostgreSQLDDLWrapper();
    }

    @Override public PostgreSQLICWrapper getICWrapper() {
        return new PostgreSQLICWrapper();
    }

    @Override public PostgreSQLDMLWrapper getDMLWrapper() {
        return new PostgreSQLDMLWrapper();
    }

    @Override public PostgreSQLPullWrapper getPullWrapper() {
        return new PostgreSQLPullWrapper(provider);
    }

    @Override public PostgreSQLPathWrapper getPathWrapper() {
        return new PostgreSQLPathWrapper();
    }

    @Override public PostgreSQLQueryWrapper getQueryWrapper() {
        return new PostgreSQLQueryWrapper();
    }

    @Override public AbstractInferenceWrapper getInferenceWrapper(String kindName) {
        throw new UnsupportedOperationException("PostgreSQLControlWrapper.getInferenceWrapper not implemented.");
    }

}
