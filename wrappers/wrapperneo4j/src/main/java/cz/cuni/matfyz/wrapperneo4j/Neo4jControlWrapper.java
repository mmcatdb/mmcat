package cz.cuni.matfyz.wrapperneo4j;

import cz.cuni.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractICWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.cuni.matfyz.abstractwrappers.AbstractStatement;

import java.util.Collection;

import org.neo4j.driver.Query;
import org.neo4j.driver.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jachymb.bartik
 */
public class Neo4jControlWrapper implements AbstractControlWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jControlWrapper.class);

    public static final String FROM_NODE_PROPERTY_NAME = "_from";
    public static final String TO_NODE_PROPERTY_NAME = "_to";
    public static final String LABEL_PROPERTY_NAME = "_label";

    private SessionProvider sessionProvider;
    
    public Neo4jControlWrapper(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    @Override
    public boolean execute(Collection<AbstractStatement> statements) {
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

            return true;
        }
        catch (Exception exception) {
            return false;
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