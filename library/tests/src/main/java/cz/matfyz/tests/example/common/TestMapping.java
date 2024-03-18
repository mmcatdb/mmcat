package cz.matfyz.tests.example.common;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.MappingBuilder;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaBuilder.BuilderObject;

import java.util.Collection;

public class TestMapping {

    public interface AccessPathCreator {
        ComplexProperty create(MappingBuilder builder);
    }

    public interface PrimaryKeyCreator {
        Collection<Signature> create(SchemaCategory schema, Key rootKey);
    }

    private static final PrimaryKeyCreator defaultKeyCreator = (schema, rootKey) -> Mapping.defaultPrimaryKey(schema.getObject(rootKey));

    private final SchemaCategory schema;
    private final Key rootKey;
    private final String kindName;
    private final AccessPathCreator pathCreator;
    private final PrimaryKeyCreator keyCreator;

    public TestMapping(SchemaCategory schema, Key rootKey, String kindName, AccessPathCreator pathCreator, PrimaryKeyCreator keyCreator) {
        this.schema = schema;
        this.rootKey = rootKey;
        this.kindName = kindName;
        this.pathCreator = pathCreator;
        this.keyCreator = keyCreator;
    }

    public TestMapping(SchemaCategory schema, Key rootKey, String kindName, AccessPathCreator pathCreator) {
        this(schema, rootKey, kindName, pathCreator, defaultKeyCreator);
    }

    public TestMapping(SchemaCategory schema, BuilderObject rootObject, String kindName, AccessPathCreator pathCreator) {
        this(schema, rootObject.key(), kindName, pathCreator, defaultKeyCreator);
    }

    private ComplexProperty accessPath;

    public ComplexProperty accessPath() {
        if (accessPath == null)
            accessPath = pathCreator.create(new MappingBuilder());

        return accessPath;
    }

    private Mapping mapping;

    public Mapping mapping() {
        if (mapping == null)
            mapping = new Mapping(schema, rootKey, kindName, accessPath(), keyCreator.create(schema, rootKey));

        return mapping;
    }

}
