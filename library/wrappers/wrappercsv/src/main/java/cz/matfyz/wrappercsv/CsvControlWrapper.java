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

public class CsvControlWrapper extends BaseControlWrapper implements AbstractControlWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(CsvControlWrapper.class);

    private final CsvProvider provider;

    public CsvControlWrapper(CsvProvider provider) {
        super(provider.settings.isWritable(), provider.settings.isQueryable());
        this.provider = provider;
    }

    @Override public void execute(Collection<AbstractStatement> statement) throws ExecuteException {
        throw new UnsupportedOperationException("CsvControlWrapper.execute not implemented.");
    }

    @Override public void execute(Path path) throws ExecuteException {
        throw new UnsupportedOperationException("CsvControlWrapper.execute not implemented.");
    }

    @Override public CsvDDLWrapper getDDLWrapper() {
        return new CsvDDLWrapper();
    }

    @Override public AbstractICWrapper getICWrapper() {
        return AbstractICWrapper.createEmpty();
    }

    @Override public CsvDMLWrapper getDMLWrapper() {
        return new CsvDMLWrapper();
    }

    @Override public CsvPullWrapper getPullWrapper() {
        return new CsvPullWrapper(provider);
    }

    @Override public CsvPathWrapper getPathWrapper() {
        return new CsvPathWrapper();
    }

    @Override public AbstractQueryWrapper getQueryWrapper() {
        throw new UnsupportedOperationException("CsvControlWrapper.getQueryWrapper not implemented.");
    }

    @Override public CsvInferenceWrapper getInferenceWrapper(SparkSettings sparkSettings) {
        return new CsvInferenceWrapper(provider, sparkSettings);
    }

}
