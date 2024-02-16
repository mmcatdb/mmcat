package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.exception.ExecuteException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Stream;

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

    private Neo4jProvider provider;
    
    public Neo4jControlWrapper(Neo4jProvider provider) {
        this.provider = provider;
    }

    @Override public void execute(Collection<AbstractStatement> statements) {
        try (
            final Session session = provider.getSession();
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
                .map(s -> (AbstractStatement) new Neo4jStatement(s))
                .toList();

            execute(statements);
        }
        catch (IOException e) {
            throw new ExecuteException(e, path);
        }
    }

    // String beforePasswordString = new StringBuilder()
    //     .append("cypher-shell -f ")
    //     .append(path.toString())
    //     .append(" -a bolt://")
    //     .append(Neo4j.HOST)
    //     .append(":")
    //     .append(Neo4j.PORT)
    //     .append(" -u ")
    //     .append(Neo4j.USERNAME)
    //     .append(" -p ")
    //     .toString();

    // LOGGER.info("Executing: " + beforePasswordString + "********");

    // String commandString = beforePasswordString + Neo4j.PASSWORD;
    // Runtime runtime = Runtime.getRuntime();
    // Process process = runtime.exec(commandString);
    // process.waitFor();

    // BufferedReader bufferReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    // LOGGER.info(bufferReader.lines().collect(Collectors.joining("\n")));

    @Override public Neo4jDDLWrapper getDDLWrapper() {
        return new Neo4jDDLWrapper();
    }

    @Override public Neo4jICWrapper getICWrapper() {
        return new Neo4jICWrapper();
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

}