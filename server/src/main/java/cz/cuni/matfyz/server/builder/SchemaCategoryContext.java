package cz.cuni.matfyz.server.builder;

import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.evolution.Version;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.utils.Position;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author jachym.bartik
 */
public class SchemaCategoryContext {

    private Id id;

    public Id getId() {
        return id;
    }

    public SchemaCategoryContext setId(Id id) {
        this.id = id;
        return this;
    }

    private Version version;

    public Version getVersion() {
        return version;
    }

    public SchemaCategoryContext setVersion(Version version) {
        this.version = version;
        return this;
    }

    private final Map<Key, Position> positions = new TreeMap<>();

    public Position getPosition(Key key) {
        return positions.get(key);
    }

    public SchemaCategoryContext setPosition(Key key, Position position) {
        positions.put(key, position);
        return this;
    }

    private final Map<Key, SchemaObject> objects = new TreeMap<>();

    public SchemaObject getObject(Key key) {
        return objects.get(key);
    }

    public SchemaCategoryContext setObject(SchemaObject object) {
        objects.put(object.key(), object);
        return this;
    }

}
