package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.BaseControlWrapper;
import cz.matfyz.abstractwrappers.AbstractCollectorWrapper;
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
     */
    public JsonControlWrapper(JsonProvider provider) {
        super(provider.settings.isWritable(), provider.settings.isQueryable());
        this.provider = provider;
    }

    /**
     * Executes a collection of statements.
     * This method is currently not implemented.
     */
    @Override public void execute(Collection<AbstractStatement> statement) throws ExecuteException {
        throw new UnsupportedOperationException("JsonControlWrapper.execute not implemented.");
    }

    /**
     * Executes a script from the specified file path.
     * This method is currently not implemented.
     */
    @Override public void execute(Path path) throws ExecuteException {
        throw new UnsupportedOperationException("JsonControlWrapper.execute not implemented.");
    }

    /**
     * Returns a Data Definition Language (DDL) wrapper for JSON.
     */
    @Override public JsonDDLWrapper getDDLWrapper() {
        return new JsonDDLWrapper();
    }

    /**
     * Returns an Integrity Constraint (IC) wrapper. This implementation returns an empty wrapper.
     */
    @Override public AbstractICWrapper getICWrapper() {
        return AbstractICWrapper.createEmpty();
    }

    /**
     * Returns a Data Manipulation Language (DML) wrapper for JSON.
     */
    @Override public JsonDMLWrapper getDMLWrapper() {
        return new JsonDMLWrapper();
    }

    /**
     * Returns a pull wrapper for JSON which provides functionalities to pull data from the source.
     */
    @Override public JsonPullWrapper getPullWrapper() {
        return new JsonPullWrapper(provider);
    }

    /**
     * Returns a path wrapper for JSON that handles path operations.
     */
    @Override public JsonPathWrapper getPathWrapper() {
        return new JsonPathWrapper();
    }

    /**
     * Returns a query wrapper for executing queries.
     * This method is currently not implemented.
     */
    @Override public AbstractQueryWrapper getQueryWrapper() {
        throw new UnsupportedOperationException("JsonControlWrapper.getQueryWrapper not implemented.");
    }

    /**
     * Returns an inference wrapper for JSON that provides functionalities for data inference using Spark.
     */
    @Override public JsonInferenceWrapper getInferenceWrapper(String kindName) {
        return new JsonInferenceWrapper(provider, kindName, getSparkSettings());
    }

    @Override public AbstractCollectorWrapper getCollectorWrapper() {
        throw new UnsupportedOperationException("Collector wrapper for this datasource is not integrated yet.");
    }
}
