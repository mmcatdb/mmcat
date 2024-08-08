package cz.matfyz.core.mapping;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObject;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public class Mapping implements Comparable<Mapping> {

    private final SchemaCategory category;
    private final SchemaObject rootObject;

    private String kindName;
    private ComplexProperty accessPath;
    private Collection<Signature> primaryKey;

    public Mapping(SchemaCategory category, Key rootKey, String kindName, ComplexProperty accessPath, Collection<Signature> primaryKey) {
        this.category = category;
        this.rootObject = category.getObject(rootKey);
        this.kindName = kindName;
        this.accessPath = accessPath;
        this.primaryKey = primaryKey;
    }

    public static Mapping create(SchemaCategory category, Key rootKey, String kindName, ComplexProperty accessPath) {
        final var rootObject = category.getObject(rootKey);

        return new Mapping(category, rootKey, kindName, accessPath, defaultPrimaryKey(rootObject));
    }

    public static List<Signature> defaultPrimaryKey(SchemaObject object) {
        return object.ids().isSignatures()
            ? object.ids().toSignatureIds().first().signatures().stream().toList()
            : List.of(Signature.createEmpty());
    }

    public Mapping clone() {
        throw new UnsupportedOperationException("Mapping.clone not implemented");
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
        // This guarantees uniqueness in one logical model, however mappings between different logical models are never compared.
        return kindName.compareTo(other.kindName);
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

        public Mapping toMapping(SchemaCategory category) {
            return new Mapping(
                category,
                rootObjectKey,
                kindName,
                accessPath,
                primaryKey
            );
        }

    }

}
