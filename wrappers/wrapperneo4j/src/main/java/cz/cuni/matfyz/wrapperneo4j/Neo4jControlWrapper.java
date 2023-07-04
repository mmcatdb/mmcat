package cz.cuni.matfyz.wrapperneo4j;

import cz.cuni.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractStatement;
import cz.cuni.matfyz.abstractwrappers.exception.ExecuteException;

import java.util.Collection;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class Neo4jControlWrapper implements AbstractControlWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jControlWrapper.class);

    static final String TYPE = "neo4j";

    static final String FROM_NODE_PROPERTY_PREFIX = "_from.";
    static final String TO_NODE_PROPERTY_PREFIX = "_to.";

    private SessionProvider sessionProvider;
    
    public Neo4jControlWrapper(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    @Override
    public void execute(Collection<AbstractStatement> statements) {
        try (
            final Session session = sessionProvider.getSession();
        ) {
            // TODO transactions?
            for (final var statement : statements) {
                if (statement.equals(Neo4jStatement.createEmpty()))
                    continue;

                final var query = new Query(statement.getContent());
                session.run(query);
            }
        }
        catch (Exception e) {
            throw new ExecuteException(e, statements);
        }
    }

    @Override
    public AbstractDDLWrapper getDDLWrapper() {
        return new Neo4jDDLWrapper();
    }

    @Override
    public AbstractICWrapper getICWrapper() {
        return new Neo4jICWrapper();
    }

    @Override
    public AbstractDMLWrapper getDMLWrapper() {
        return new Neo4jDMLWrapper();
    }

    @Override
    public AbstractPullWrapper getPullWrapper() {
        return new Neo4jPullWrapper(sessionProvider);
    }

    @Override
    public AbstractPathWrapper getPathWrapper() {
        return new Neo4jPathWrapper();
    }

}