package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.abstractwrappers.BaseControlWrapper;
import cz.matfyz.abstractwrappers.collector.AbstractCollectorWrapper.ConnectionData;
import cz.matfyz.abstractwrappers.exception.ExecuteException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.wrapperneo4j.collector.Neo4jCollectorWrapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Neo4jControlWrapper extends BaseControlWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jControlWrapper.class);

    @Override protected DatasourceType getType() {
        return DatasourceType.neo4j;
    }

    static final String FROM_NODE_PROPERTY_PREFIX = "_from.";
    static final String TO_NODE_PROPERTY_PREFIX = "_to.";

    private final Neo4jProvider provider;

    public Neo4jControlWrapper(Neo4jProvider provider) {
        super(provider.settings.isWritable(), provider.settings.isQueryable());
        this.provider = provider;
    }

    @Override public void execute(Collection<AbstractStatement> statements) {
        try (
            Session session = provider.getSession();
        ) {
            // TODO transactions?
            for (final var statement : statements) {
                if (statement.equals(AbstractStatement.createEmpty()))
                    continue;

                final var query = new Query(statement.getContent());
                session.run(query);
            }
        }
        catch (Exception e) {
            throw new ExecuteException(e, statements);
        }
    }

    /**
     * The point of this method is that the neo4j driver doesn't allow to run whole script at one. So we have to split it manually.
     * This is of course not ideal since we don't want to parse the whole thing. But close enough.
     */
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

    @Override public Neo4jDDLWrapper getDDLWrapper() {
        return new Neo4jDDLWrapper();
    }

    @Override public AbstractICWrapper getICWrapper() {
        return AbstractICWrapper.createEmpty();
    }

    @Override public Neo4jDMLWrapper getDMLWrapper() {
        return new Neo4jDMLWrapper();
    }

    @Override public Neo4jPullWrapper getPullWrapper() {
        return new Neo4jPullWrapper(provider);
    }

    @Override public Neo4jPathWrapper getPathWrapper() {
        return new Neo4jPathWrapper();
    }

    @Override public Neo4jQueryWrapper getQueryWrapper() {
        return new Neo4jQueryWrapper();
    }

    @Override public AbstractInferenceWrapper getInferenceWrapper() {
        throw new UnsupportedOperationException("Neo4jControlWrapper.getInferenceWrapper not implemented.");
    }

    @Override public Neo4jCollectorWrapper getCollectorWrapper() {
        return new Neo4jCollectorWrapper(new ConnectionData(
            provider.settings.host(),
            Integer.parseInt(provider.settings.port()),
            getType().name(),
            provider.settings.database(),
            provider.settings.username(),
            provider.settings.password()));
    }

}
