package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractControlWrapper;
import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractDMLWrapper;
import cz.matfyz.abstractwrappers.AbstractICWrapper;
import cz.matfyz.abstractwrappers.AbstractPathWrapper;
import cz.matfyz.abstractwrappers.AbstractPullWrapper;
import cz.matfyz.abstractwrappers.AbstractQueryWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.BaseControlWrapper;
import cz.matfyz.abstractwrappers.exception.ExecuteException;
import cz.matfyz.abstractwrappers.utils.GenericICWrapper;

import java.nio.file.Path;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: make sure that there is only one Mapping for each Logical Model
public class JsonControlWrapper extends BaseControlWrapper implements AbstractControlWrapper {

    @SuppressWarnings({ "java:s1068", "unused" })
    private static final Logger LOGGER = LoggerFactory.getLogger(JsonControlWrapper.class);

    private final JsonProvider provider;

    public JsonControlWrapper(JsonProvider provider) {
        super(provider.settings.isWritable(), provider.settings.isQueryable());
        this.provider = provider;
    }

    @Override public void execute(Collection<AbstractStatement> statement) throws ExecuteException {
        throw new UnsupportedOperationException("JsonControlWrapper.execute not implemented.");
    }

    @Override public void execute(Path path) throws ExecuteException {
        throw new UnsupportedOperationException("JsonControlWrapper.execute not implemented.");
    }
    
    @Override public JsonDDLWrapper getDDLWrapper() {
        return new JsonDDLWrapper();
    }

    @Override public GenericICWrapper getICWrapper() {
        return new GenericICWrapper();
    }

    @Override public JsonDMLWrapper getDMLWrapper() {
        return new JsonDMLWrapper();
    }

    @Override public JsonPullWrapper getPullWrapper() {
        return new JsonPullWrapper(provider);
    }

    @Override public AbstractPathWrapper getPathWrapper() {
        return new JsonPathWrapper();
    }

    @Override public AbstractQueryWrapper getQueryWrapper() {
        throw new UnsupportedOperationException("JsonControlWrapper.getQueryWrapper not implemented.");
    }

}
