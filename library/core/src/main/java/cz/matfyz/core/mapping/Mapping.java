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

    public Mapping(Datasource datasource, String kindName, SchemaCategory category, Key rootKey, ComplexProperty accessPath, Collection<Signature> primaryKey) {
        this.datasource = datasource;
        this.kindName = kindName;
        this.category = category;
        this.rootObject = category.getObject(rootKey);
        this.accessPath = accessPath;
        this.primaryKey = primaryKey;
    }

    public static Mapping create(Datasource datasource, String kindName, SchemaCategory category, Key rootKey, ComplexProperty accessPath) {
        final var rootObject = category.getObject(rootKey);

        return new Mapping(datasource, kindName, category, rootKey, accessPath, defaultPrimaryKey(rootObject));
    }

    private static List<Signature> defaultPrimaryKey(SchemaObject object) {
        return object.ids().isSignatures()
            ? object.ids().toSignatureIds().first().signatures().stream().toList()
            : List.of(Signature.createEmpty());
    }

    public Mapping clone() {
        throw new UnsupportedOperationException("Mapping.clone not implemented");
    }

    private final Datasource datasource;
    public Datasource datasource() {
        return datasource;
    }

    private final String kindName;
    public String kindName() {
        return kindName;
    }

    private final SchemaCategory category;
    public SchemaCategory category() {
        return category;
    }

    private final SchemaObject rootObject;
    public SchemaObject rootObject() {
        return rootObject;
    }

    private final ComplexProperty accessPath;
    public ComplexProperty accessPath() {
        return accessPath;
    }

    private final Collection<Signature> primaryKey;
    public Collection<Signature> primaryKey() {
        return primaryKey;
    }

    // Updating

    public Mapping withSchema(SchemaCategory category, ComplexProperty accessPath, Collection<Signature> primaryKey) {
        return new Mapping(datasource, kindName, category, rootObject.key(), accessPath, primaryKey);
    }

    // Identification

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

    // Debug

    @Override public String toString() {
        return datasource.getUniqueKindIdentifier(kindName);
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
                kindName,
                category,
                rootObjectKey,
                accessPath,
                primaryKey
            );
        }

    }

}
