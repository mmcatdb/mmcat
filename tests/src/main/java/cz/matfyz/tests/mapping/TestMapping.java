package cz.matfyz.tests.mapping;

import cz.matfyz.core.category.Signature;
import cz.matfyz.core.mapping.ComplexProperty;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.Collection;

public class TestMapping {

    interface AccessPathCreator {
        ComplexProperty create();
    }

    interface PrimaryKeyCreator {
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
    
    private ComplexProperty accessPath;

    public ComplexProperty accessPath() {
        if (accessPath == null)
            accessPath = pathCreator.create();

        return accessPath;
    }

    private Mapping mapping;

    public Mapping mapping() {
        if (mapping == null)
            mapping = new Mapping(schema, rootKey, kindName, accessPath(), keyCreator.create(schema, rootKey));

        return mapping;
    }

    // protected Collection<Signature> createPrimaryKey() {
    //     return Mapping.defaultPrimaryKey(schema.getObject(rootKey));
    // }

    // private SchemaCategory schema;
    // private Key rootKey;
    // private String kindName;

    // protected TestMapping(SchemaCategory schema, Key rootKey, String kindName) {
    //     this.schema = schema;
    //     this.rootKey = rootKey;
    //     this.kindName = kindName;
    // }
    
    // private ComplexProperty accessPath;

    // public ComplexProperty accessPath() {
    //     if (accessPath == null)
    //         accessPath = createAccessPath();

    //     return accessPath;
    // }

    // protected abstract ComplexProperty createAccessPath();

    // private Mapping mapping;

    // public Mapping mapping() {
    //     if (mapping == null)
    //         mapping = new Mapping(schema, rootKey, kindName, accessPath(), createPrimaryKey());

    //     return mapping;
    // }

    // protected Collection<Signature> createPrimaryKey() {
    //     return Mapping.defaultPrimaryKey(schema.getObject(rootKey));
    // }

}
