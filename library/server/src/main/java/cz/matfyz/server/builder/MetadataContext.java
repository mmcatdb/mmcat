package cz.matfyz.server.builder;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.Position;

import java.util.Map;
import java.util.TreeMap;

import org.checkerframework.checker.nullness.qual.Nullable;

public class MetadataContext {

    private @Nullable Id id;

    public @Nullable Id getId() {
        return id;
    }

    public MetadataContext setId(Id id) {
        this.id = id;
        return this;
    }

    private @Nullable Version version;

    public @Nullable Version getVersion() {
        return version;
    }

    public MetadataContext setVersion(Version version) {
        this.version = version;
        return this;
    }

    private @Nullable Version systemVersion;

    public @Nullable Version getSystemVersion() {
        return systemVersion;
    }

    public MetadataContext setSystemVersion(Version systemVersion) {
        this.systemVersion = systemVersion;
        return this;
    }

    private Map<Key, Position> positions = new TreeMap<>();

    public @Nullable Position getPosition(Key key) {
        return positions.get(key);
    }

    public MetadataContext setPosition(Key key, Position position) {
        positions.put(key, position);
        return this;
    }

    public MetadataContext setPostitions(Map<Key, Position> positions) {
        this.positions = positions;
        return this;
    }

}
