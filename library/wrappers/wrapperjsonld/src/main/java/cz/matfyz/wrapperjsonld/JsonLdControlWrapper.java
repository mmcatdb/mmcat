package cz.matfyz.wrapperjsonld;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractInferenceWrapper;
import cz.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.BaseControlWrapper;
import cz.matfyz.abstractwrappers.exception.ExecuteException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.nio.file.Path;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonLdControlWrapper extends BaseControlWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonLdControlWrapper.class);

    @Override protected DatasourceType getType() {
        return DatasourceType.jsonld;
    }

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

    @Override public AbstractInferenceWrapper getInferenceWrapper() {
        throw new UnsupportedOperationException("JsonLdControlWrapper.getInferenceWrapper not implemented.");
    }

}
