package cz.matfyz.wrapperjsonld;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.BaseControlWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.abstractwrappers.exception.ExecuteException;

import java.nio.file.Path;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonLdControlWrapper extends BaseControlWrapper implements AbstractControlWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonLdControlWrapper.class);

    private final JsonLdProvider provider;

    public JsonLdControlWrapper(JsonLdProvider provider) {
        super(provider.settings.isWritable(), provider.settings.isQueryable());
        this.provider = provider;
    }

    @Override public void execute(Collection<AbstractStatement> statement) throws ExecuteException {
        throw new UnsupportedOperationException("JsonLdControlWrapper.execute not implemented.");
    }

    @Override public void execute(Path path) throws ExecuteException {
        throw new UnsupportedOperationException("JsonLdControlWrapper.execute not implemented.");
    }

    @Override public AbstractDDLWrapper getDDLWrapper() {
        throw new UnsupportedOperationException("JsonLdControlWrapper.getDDLWrapper not implemented.");
    }

    @Override public AbstractICWrapper getICWrapper() {
        throw new UnsupportedOperationException("JsonLdControlWrapper.getICWrapper not implemented.");
    }

    @Override public AbstractDMLWrapper getDMLWrapper() {
        throw new UnsupportedOperationException("JsonLdControlWrapper.getDMLWrapper not implemented.");
    }

    @Override public AbstractPullWrapper getPullWrapper() {
        throw new UnsupportedOperationException("JsonLdControlWrapper.getPullWrapper not implemented.");
    }

    @Override public AbstractPathWrapper getPathWrapper() {
        return new JsonLdPathWrapper();
    }

    @Override public AbstractQueryWrapper getQueryWrapper() {
        throw new UnsupportedOperationException("JsonLdControlWrapper.getQueryWrapper not implemented.");
    }

    @Override public AbstractInferenceWrapper getInferenceWrapper(SparkSettings sparkSettings) {
        throw new UnsupportedOperationException("JsonLdControlWrapper.getInferenceWrapper not implemented.");
    }

}
