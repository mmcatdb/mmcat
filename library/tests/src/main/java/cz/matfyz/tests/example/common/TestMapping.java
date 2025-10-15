package cz.matfyz.tests.example.common;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.AccessPathBuilder;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.utils.Accessor;

import java.util.Collection;

import org.checkerframework.checker.nullness.qual.Nullable;

public class TestMapping {

    public interface AccessPathCreator {
        ComplexProperty create(AccessPathBuilder builder);
    }

    public interface PrimaryKeyCreator {
        Collection<Signature> create(SchemaCategory schema, Key rootKey);
    }

    private final Datasource datasource;
    private final SchemaCategory schema;
    private final Key rootKey;
    private final String kindName;
    private final AccessPathCreator pathCreator;
    private final @Nullable PrimaryKeyCreator keyCreator;

    public TestMapping(Datasource datasource, SchemaCategory schema, Accessor<Key> rootKey, String kindName, AccessPathCreator pathCreator, @Nullable PrimaryKeyCreator keyCreator) {
        this.datasource = datasource;
        this.schema = schema;
        this.rootKey = rootKey.access();
        this.kindName = kindName;
        this.pathCreator = pathCreator;
        this.keyCreator = keyCreator;
    }

    public TestMapping(Datasource datasource, SchemaCategory schema, Accessor<Key> rootKey, String kindName, AccessPathCreator pathCreator) {
        this(datasource, schema, rootKey, kindName, pathCreator, null);
    }

    private @Nullable ComplexProperty accessPath;

    public ComplexProperty accessPath() {
        if (accessPath == null)
            accessPath = pathCreator.create(new AccessPathBuilder());

        return accessPath;
    }

    private @Nullable Mapping mapping;

    public Mapping mapping() {
        if (mapping == null)
            mapping = keyCreator != null
                ? new Mapping(datasource, kindName, schema, rootKey, accessPath(), keyCreator.create(schema, rootKey))
                : Mapping.create(datasource, kindName, schema, rootKey, accessPath());

        return mapping;
    }

}
