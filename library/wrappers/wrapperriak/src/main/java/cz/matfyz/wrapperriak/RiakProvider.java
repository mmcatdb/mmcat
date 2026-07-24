package cz.matfyz.wrapperriak;

import cz.matfyz.abstractwrappers.AbstractDatasourceProvider;
import cz.matfyz.core.exception.OtherException;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakCluster;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.checkerframework.checker.nullness.qual.Nullable;

public class RiakProvider implements AbstractDatasourceProvider {

    final RiakSettings settings;

    private @Nullable RiakClient riakClient;

    public RiakProvider(RiakSettings settings) {
        this.settings = settings;
    }

    // TODO: Should I return RiakCluster or RiakClient
    public RiakCluster getCluster() {
        try {
            if (riakClient == null)
                riakClient = RiakClient.newClient(settings.createConnectionAddress());

            return riakClient.getRiakCluster();
        }
        catch (UnknownHostException e) {
            throw new OtherException(e);
        }
    }

    @Override public boolean isStillValid(Object settings) {
        if (!(settings instanceof RiakSettings riakSettings))
            return false;

        return this.settings.host.equals(riakSettings.host)
            && this.settings.port == riakSettings.port
            && this.settings.isWritable == riakSettings.isWritable
            && this.settings.isQueryable == riakSettings.isQueryable;
    }

    @Override public void close() {
        if (riakClient != null)
            riakClient.cleanup();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RiakSettings(
        String host,
        int port,
        // TODO: String username,
        // TODO: String password,
        boolean isWritable,
        boolean isQueryable,
        boolean isClonable
    ) {
        InetSocketAddress createConnectionAddress() {
            return new InetSocketAddress(host, port);
        }
    }
}
