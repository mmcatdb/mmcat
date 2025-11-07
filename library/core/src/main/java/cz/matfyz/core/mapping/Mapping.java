package cz.matfyz.core.mapping;

import cz.matfyz.core.datasource.Datasource;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObjex;
import cz.matfyz.core.utils.IterableUtils;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class Mapping implements Comparable<Mapping> {

    public Mapping(Datasource datasource, String kindName, SchemaCategory category, Key rootKey, ComplexProperty accessPath, Collection<Signature> primaryKey) {
        this.datasource = datasource;
        this.kindName = kindName;
        this.category = category;
        this.rootObjex = category.getObjex(rootKey);
        this.accessPath = accessPath;
        this.primaryKey = primaryKey;
    }

    public static Mapping create(Datasource datasource, String kindName, SchemaCategory category, Key rootKey, ComplexProperty accessPath) {
        final var rootObjex = category.getObjex(rootKey);

        return new Mapping(datasource, kindName, category, rootKey, accessPath, createDefaultPrimaryKey(rootObjex));
    }

    private static Collection<Signature> createDefaultPrimaryKey(SchemaObjex objex) {
        assert !objex.ids().isEmpty() : "Can't create default primary key - objex has no IDs.";
        final var firstId = objex.ids().first();
        return IterableUtils.toList(firstId.signatures());
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

    private final SchemaObjex rootObjex;
    public SchemaObjex rootObjex() {
        return rootObjex;
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

    public Mapping withSchemaAndPath(SchemaCategory category, ComplexProperty accessPath) {
        return new Mapping(datasource, kindName, category, rootObjex.key(), accessPath, primaryKey);
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
        Key rootObjexKey,
        List<Signature> primaryKey,
        String kindName,
        ComplexProperty accessPath
    ) implements Serializable {

        public static SerializedMapping fromMapping(Mapping mapping) {
            return new SerializedMapping(
                mapping.rootObjex().key(),
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
                rootObjexKey,
                accessPath,
                primaryKey
            );
        }

    }

}
