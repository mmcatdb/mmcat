package cz.matfyz.wrappercsv;

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
 * A control wrapper for CSV files that provides various operations such as DDL, DML, and inference
 * functionalities. This class extends {@link BaseControlWrapper} and implements {@link AbstractControlWrapper}.
 */
public class CsvControlWrapper extends BaseControlWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvControlWrapper.class);

    @Override protected DatasourceType getType() {
        return DatasourceType.csv;
    }

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
     * @param statement a collection of {@link AbstractStatement} statements to be executed.
     */
    @Override public void execute(Collection<AbstractStatement> statement) throws ExecuteException {
        throw new UnsupportedOperationException("CsvControlWrapper.execute not implemented.");
    }

    /**
     * Executes a script from the specified file path. This method is currently not implemented.
     *
     * @param path the file path to the script to be executed.
     */
    @Override public void execute(Path path) throws ExecuteException {
        throw new UnsupportedOperationException("CsvControlWrapper.execute not implemented.");
    }

    /**
     * Returns a Data Definition Language (DDL) wrapper for CSV.
     */
    @Override public CsvDDLWrapper getDDLWrapper() {
        return new CsvDDLWrapper();
    }

    /**
     * Returns an Integrity Constraint (IC) wrapper. This implementation returns an empty wrapper.
     */
    @Override public AbstractICWrapper getICWrapper() {
        return AbstractICWrapper.createEmpty();
    }

    /**
     * Returns a Data Manipulation Language (DML) wrapper for CSV.
     */
    @Override public CsvDMLWrapper getDMLWrapper() {
        return new CsvDMLWrapper();
    }

    /**
     * Returns a pull wrapper for CSV which provides functionalities to pull data from the source.
     */
    @Override public CsvPullWrapper getPullWrapper() {
        return new CsvPullWrapper(provider);
    }

    /**
     * Returns a path wrapper for CSV that handles file path operations.
     */
    @Override public CsvPathWrapper getPathWrapper() {
        return new CsvPathWrapper();
    }

    /**
     * Returns a query wrapper for executing queries. This method is currently not implemented.
     */
    @Override public AbstractQueryWrapper getQueryWrapper() {
        throw new UnsupportedOperationException("CsvControlWrapper.getQueryWrapper not implemented.");
    }

    /**
     * Returns an inference wrapper for CSV that provides functionalities for data inference using Spark.
     */
    @Override public CsvInferenceWrapper getInferenceWrapper() {
        return new CsvInferenceWrapper(provider, getSparkSettings());
    }

    @Override public CollectorWrapper getCollectorWrapper() {
        throw new UnsupportedOperationException("Collector wrapper for this datasource is not integrated yet.");
    }
}
