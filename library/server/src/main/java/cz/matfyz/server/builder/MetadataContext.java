package cz.matfyz.server.builder;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.evolution.Version;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.entity.schema.SchemaObjectWrapper.Position;

import java.util.Map;
import java.util.TreeMap;

public class MetadataContext {

    private Id id;

    public Id getId() {
        return id;
    }

    public MetadataContext setId(Id id) {
        this.id = id;
        return this;
    }

    private Version version;

    public Version getVersion() {
        return version;
    }

    public MetadataContext setVersion(Version version) {
        this.version = version;
        return this;
    }

    private final Map<Key, Position> positions = new TreeMap<>();

    public Position getPosition(Key key) {
        return positions.get(key);
    }

    public MetadataContext setPosition(Key key, Position position) {
        positions.put(key, position);
        return this;
    }

}
