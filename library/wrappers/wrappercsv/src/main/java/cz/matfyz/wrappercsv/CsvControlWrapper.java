package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.BaseControlWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.abstractwrappers.exception.ExecuteException;

import java.nio.file.Path;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A control wrapper for CSV files that provides various operations such as DDL, DML, and inference
 * functionalities. This class extends {@link BaseControlWrapper} and implements {@link AbstractControlWrapper}.
 */
public class CsvControlWrapper extends BaseControlWrapper implements AbstractControlWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvControlWrapper.class);

    private final CsvProvider provider;

    /**
     * Constructs a new {@code CsvControlWrapper} with the specified CSV provider.
     *
     * @param provider the CSV provider that supplies settings and configurations for this wrapper.
     */
    public CsvControlWrapper(CsvProvider provider) {
        super(provider.settings.isWritable(), provider.settings.isQueryable());
        this.provider = provider;
    }

    /**
     * Executes a collection of statements. This method is currently not implemented.
     *
     * @param statement a collection of {@link AbstractStatement} objects to be executed.
     * @throws ExecuteException always thrown as this method is not implemented.
     */
    @Override
    public void execute(Collection<AbstractStatement> statement) throws ExecuteException {
        throw new UnsupportedOperationException("CsvControlWrapper.execute not implemented.");
    }

    /**
     * Executes a script from the specified file path. This method is currently not implemented.
     *
     * @param path the file path to the script to be executed.
     * @throws ExecuteException always thrown as this method is not implemented.
     */
    @Override
    public void execute(Path path) throws ExecuteException {
        throw new UnsupportedOperationException("CsvControlWrapper.execute not implemented.");
    }

    /**
     * Returns a Data Definition Language (DDL) wrapper for CSV.
     *
     * @return an instance of {@link CsvDDLWrapper}.
     */
    @Override
    public CsvDDLWrapper getDDLWrapper() {
        return new CsvDDLWrapper();
    }

    /**
     * Returns an Integrity Constraint (IC) wrapper. This implementation returns an empty wrapper.
     *
     * @return an instance of {@link AbstractICWrapper} that is empty.
     */
    @Override
    public AbstractICWrapper getICWrapper() {
        return AbstractICWrapper.createEmpty();
    }

    /**
     * Returns a Data Manipulation Language (DML) wrapper for CSV.
     *
     * @return an instance of {@link CsvDMLWrapper}.
     */
    @Override
    public CsvDMLWrapper getDMLWrapper() {
        return new CsvDMLWrapper();
    }

    /**
     * Returns a pull wrapper for CSV which provides functionalities to pull data from the source.
     *
     * @return an instance of {@link CsvPullWrapper}.
     */
    @Override
    public CsvPullWrapper getPullWrapper() {
        return new CsvPullWrapper(provider);
    }

    /**
     * Returns a path wrapper for CSV that handles file path operations.
     *
     * @return an instance of {@link CsvPathWrapper}.
     */
    @Override
    public CsvPathWrapper getPathWrapper() {
        return new CsvPathWrapper();
    }

    /**
     * Returns a query wrapper for executing queries. This method is currently not implemented.
     *
     * @return nothing, as this method always throws an exception.
     * @throws UnsupportedOperationException always thrown as this method is not implemented.
     */
    @Override
    public AbstractQueryWrapper getQueryWrapper() {
        throw new UnsupportedOperationException("CsvControlWrapper.getQueryWrapper not implemented.");
    }

    /**
     * Returns an inference wrapper for CSV that provides functionalities for data inference using Spark.
     *
     * @param sparkSettings the settings for Spark to be used in the inference process.
     * @return an instance of {@link CsvInferenceWrapper}.
     */
    @Override
    public CsvInferenceWrapper getInferenceWrapper(SparkSettings sparkSettings) {
        return new CsvInferenceWrapper(provider, sparkSettings);
    }

}
