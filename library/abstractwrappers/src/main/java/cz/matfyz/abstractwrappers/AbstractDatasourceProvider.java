package cz.matfyz.abstractwrappers;

public interface AbstractDatasourceProvider {

    // Once created, the datasource connection should be stable, even if the credentials change. However, if the host or port changes, we have to obviously create a new connection.
    // We can therefore distinguish two types of changes: stable (credentials, label, ...) and unstable (host, port, ...). However, each datasource is different - e.g., PostgreSQL creates a new connection for each query, while MongoDB uses connection pooling. So, for each datasource, we have to decide separately.
    // A stable change doesn't affect the current connection. An unstable change completely invalidates it.
    // Notice there isn't any middle ground, because we don't try to reconnect or something like that. If we were, the stable changes would be divided to:
    //  - "Non" changes (e.g., the label changes but the connection is still valid).
    //  - "Real" changes which affect the possible reconnections (e.g., the password) or qualities of it (e.g., the queriability).
    // In that case, we would prefer to keep old the connection for some time (to allow the background tasks to finish) while creating a new one (which would be used for new tasks). We would like to keep the other settings too, i.e., this provider. After some time, we would just close all remaining connections and discard the provider.

    /**
     * Returns whether the provider can still be used with the new settings. I.e., whether the change from the current settings to the new settings is stable.
     */
    public abstract boolean isStillValid(Object newSettings);

    /**
     * If the provider is no longer needed, we should close it to free up the resources.
     */
    public abstract void close();

}
