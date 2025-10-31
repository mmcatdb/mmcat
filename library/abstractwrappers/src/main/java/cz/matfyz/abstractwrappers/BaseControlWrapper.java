package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.AbstractInferenceWrapper.SparkSettings;
import cz.matfyz.abstractwrappers.exception.ConfigurationException;
import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class BaseControlWrapper implements AbstractControlWrapper {

    private final boolean isWritable;
    private final boolean isQueryable;

    protected BaseControlWrapper(boolean isWritable, boolean isQueryable) {
        this.isWritable = isWritable;
        this.isQueryable = isQueryable;
    }

    public boolean isWritable() {
        return isWritable;
    }

    public boolean isQueryable() {
        return isQueryable;
    }

    private @Nullable SparkSettings sparkSettings;

    protected SparkSettings getSparkSettings() {
        if (sparkSettings == null)
            throw ConfigurationException.missingSparkSettings();

        return sparkSettings;
    }

    public BaseControlWrapper enableSpark(SparkSettings sparkSettings) {
        this.sparkSettings = sparkSettings;
        return this;
    }


    protected abstract DatasourceType getType();

    /**
     * Convenience method for creating a provider with a single datasource. Useful for testing or simple use cases where only one datasource is needed.
     */
    public DefaultControlWrapperProvider createProvider(String datasourceIdentifier) {
        return new DefaultControlWrapperProvider()
            .setControlWrapper(new Datasource(getType(), datasourceIdentifier), this);
    }

    /**
     * Convenience shortcut for the convenience method for creating a provider with a single datasource.
     * The datasource identifier is automatically generated each time to ensure uniqueness.
     */
    public DefaultControlWrapperProvider createProvider() {
        return createProvider(createNextUniqueIdentifier());
    }

    private static int lastUniqueIdentifier = 0;
    private static synchronized String createNextUniqueIdentifier() {
        return "datasource-" + lastUniqueIdentifier++;
    }

    /**
     * A provider that generates control wrappers for datasources.
     * In general, many algorithms need (a) one or more datasources to operate on and (b) a control wrapper for each of these datasources.
     * This class enables us to create the wrappers in advance and then pass them to the algorithms (because creating them usually involves some configuration, caching, etc.).
     */
    public interface ControlWrapperProvider {

        Collection<Datasource> getDatasources();

        AbstractControlWrapper getControlWrapper(Datasource datasource);

    }

    public static class DefaultControlWrapperProvider implements ControlWrapperProvider {

        private Map<Datasource, AbstractControlWrapper> wrappers = new TreeMap<>();

        public DefaultControlWrapperProvider setControlWrapper(Datasource datasource, AbstractControlWrapper wrapper) {
            wrappers.put(datasource, wrapper);
            return this;
        }

        @Override public Collection<Datasource> getDatasources() {
            return wrappers.keySet();
        }

        @Override public AbstractControlWrapper getControlWrapper(Datasource datasource) {
            final var wrapper = wrappers.get(datasource);
            if (wrapper == null)
                throw new IllegalArgumentException("No wrapper for datasource " + datasource.identifier);

            return wrapper;
        }

    }

}
