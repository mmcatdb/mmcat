package cz.matfyz.core.mapping;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class Mapping implements Comparable<Mapping> {

    private final Datasource datasource;
    private final SchemaCategory category;
    private final SchemaObject rootObject;
    private final String kindName;
    private final ComplexProperty accessPath;
    private final Collection<Signature> primaryKey;

    public Mapping(Datasource datasource, SchemaCategory category, Key rootKey, String kindName, ComplexProperty accessPath, Collection<Signature> primaryKey) {
        this.datasource = datasource;
        this.category = category;
        this.rootObject = category.getObject(rootKey);
        this.kindName = kindName;
        this.accessPath = accessPath;
        this.primaryKey = primaryKey;
    }

    public static Mapping create(Datasource datasource, SchemaCategory category, Key rootKey, String kindName, ComplexProperty accessPath) {
        final var rootObject = category.getObject(rootKey);

        return new Mapping(datasource, category, rootKey, kindName, accessPath, defaultPrimaryKey(rootObject));
    }

    private static List<Signature> defaultPrimaryKey(SchemaObject object) {
        return object.ids().isSignatures()
            ? object.ids().toSignatureIds().first().signatures().stream().toList()
            : List.of(Signature.createEmpty());
    }

    public Mapping clone() {
        throw new UnsupportedOperationException("Mapping.clone not implemented");
    }

    public Datasource datasource() {
        return datasource;
    }

    public SchemaCategory category() {
        return category;
    }

    public SchemaObject rootObject() {
        return rootObject;
    }

    public ComplexProperty accessPath() {
        return accessPath;
    }

    public String kindName() {
        return kindName;
    }

    public Collection<Signature> primaryKey() {
        return primaryKey;
    }

    @Override public boolean equals(Object other) {
        if (this == other)
            return true;

        return other instanceof Mapping otherMapping && compareTo(otherMapping) == 0;
    }

    @Override public int compareTo(Mapping other) {
        final int datasourceComparison = datasource.compareTo(other.datasource);
        return datasourceComparison != 0
            ? datasourceComparison
            : kindName.compareTo(other.kindName);
    }

    public record SerializedMapping(
        Key rootObjectKey,
        List<Signature> primaryKey,
        String kindName,
        ComplexProperty accessPath
    ) implements Serializable {

        public static SerializedMapping fromMapping(Mapping mapping) {
            return new SerializedMapping(
                mapping.rootObject().key(),
                mapping.primaryKey().stream().toList(),
                mapping.kindName(),
                mapping.accessPath()
            );
        }

        public Mapping toMapping(Datasource datasource, SchemaCategory category) {
            return new Mapping(
                datasource,
                category,
                rootObjectKey,
                kindName,
                accessPath,
                primaryKey
            );
        }

    }

}
