package cz.cuni.matfyz.wrapperpostgresql;

import cz.cuni.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.statements.AbstractStatement;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLControlWrapper implements AbstractControlWrapper {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PostgreSQLControlWrapper.class);

    private ConnectionProvider connectionProvider;

    public PostgreSQLControlWrapper(ConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
    }

    @Override
    public boolean execute(Collection<AbstractStatement> statements) {
        try (
            Connection connection = connectionProvider.getConnection();
        ) {
            // TODO transactions?
            for (final var statement : statements) {
                try (
                    PreparedStatement preparedStatement = connection.prepareStatement(statement.getContent());
                ) {
                    final var result = preparedStatement.execute();
                    if (!result)
                        return false;
                }
            }

            return true;
        }
        catch (SQLException exception) {
            LOGGER.error("PostgeSQL exception: ", exception);
            return false;
        }
    }

    @Override
    public AbstractDDLWrapper getDDLWrapper() {
        return new PostgreSQLDDLWrapper();
    }

    @Override
    public AbstractICWrapper getICWrapper() {
        return new PostgreSQLICWrapper();
    }

    @Override
    public AbstractDMLWrapper getDMLWrapper() {
        return new PostgreSQLDMLWrapper();
    }

    @Override
    public AbstractPullWrapper getPullWrapper() {
        return new PostgreSQLPullWrapper(connectionProvider);
    }

    @Override
    public AbstractPathWrapper getPathWrapper() {
        return new PostgreSQLPathWrapper();
    }

}