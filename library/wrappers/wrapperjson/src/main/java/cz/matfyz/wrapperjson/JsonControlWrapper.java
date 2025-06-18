package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.BaseControlWrapper;
import cz.matfyz.abstractwrappers.collector.CollectorWrapper;
import cz.matfyz.abstractwrappers.exception.ExecuteException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.nio.file.Path;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A control wrapper for JSON files that provides various operations such as DDL, DML, and inference functionalities.
 * This class extends {@link BaseControlWrapper} and implements {@link AbstractControlWrapper}.
 */
public class JsonControlWrapper extends BaseControlWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonControlWrapper.class);

    @Override protected DatasourceType getType() {
        return DatasourceType.json;
    }

    private final JsonProvider provider;

    /**
     * Constructs a new {@code JsonControlWrapper} with the specified JSON provider.
     *
     * @param provider the JSON provider that supplies settings and configurations for this wrapper.
     */
    public JsonControlWrapper(JsonProvider provider) {
        super(provider.settings.isWritable(), provider.settings.isQueryable());
        this.provider = provider;
    }

    /**
     * Executes a collection of statements. This method is currently not implemented.
     *
     * @param statement a collection of {@link AbstractStatement} statements to be executed.
     * @throws ExecuteException always thrown as this method is not implemented.
     */
    @Override public void execute(Collection<AbstractStatement> statement) throws ExecuteException {
        throw new UnsupportedOperationException("JsonControlWrapper.execute not implemented.");
    }

    /**
     * Executes a script from the specified file path. This method is currently not implemented.
     *
     * @param path the file path to the script to be executed.
     * @throws ExecuteException always thrown as this method is not implemented.
     */
    @Override public void execute(Path path) throws ExecuteException {
        throw new UnsupportedOperationException("JsonControlWrapper.execute not implemented.");
    }

    /**
     * Returns a Data Definition Language (DDL) wrapper for JSON.
     *
     * @return an instance of {@link JsonDDLWrapper}.
     */
    @Override public JsonDDLWrapper getDDLWrapper() {
        return new JsonDDLWrapper();
    }

    /**
     * Returns an Integrity Constraint (IC) wrapper. This implementation returns an empty wrapper.
     *
     * @return an instance of {@link AbstractICWrapper} that is empty.
     */
    @Override public AbstractICWrapper getICWrapper() {
        return AbstractICWrapper.createEmpty();
    }

    /**
     * Returns a Data Manipulation Language (DML) wrapper for JSON.
     *
     * @return an instance of {@link JsonDMLWrapper}.
     */
    @Override public JsonDMLWrapper getDMLWrapper() {
        return new JsonDMLWrapper();
    }

    /**
     * Returns a pull wrapper for JSON which provides functionalities to pull data from the source.
     *
     * @return an instance of {@link JsonPullWrapper}.
     */
    @Override public JsonPullWrapper getPullWrapper() {
        return new JsonPullWrapper(provider);
    }

    /**
     * Returns a path wrapper for JSON that handles path operations.
     *
     * @return an instance of {@link JsonPathWrapper}.
     */
    @Override public JsonPathWrapper getPathWrapper() {
        return new JsonPathWrapper();
    }

    /**
     * Returns a query wrapper for executing queries. This method is currently not implemented.
     *
     * @return nothing, as this method always throws an exception.
     * @throws UnsupportedOperationException always thrown as this method is not implemented.
     */
    @Override public AbstractQueryWrapper getQueryWrapper() {
        throw new UnsupportedOperationException("JsonControlWrapper.getQueryWrapper not implemented.");
    }

    /**
     * Returns an inference wrapper for JSON that provides functionalities for data inference using Spark.
     *
     * @param sparkSettings the settings for Spark to be used in the inference process.
     * @return an instance of {@link JsonInferenceWrapper}.
     */
    @Override public JsonInferenceWrapper getInferenceWrapper() {
        return new JsonInferenceWrapper(provider, getSparkSettings());
    }

    @Override public CollectorWrapper getCollectorWrapper() {
        throw new UnsupportedOperationException("Collector wrapper for this datasource is not integrated yet.");
    }
}
