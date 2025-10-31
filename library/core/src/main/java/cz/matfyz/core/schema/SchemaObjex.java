package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.Signature;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SchemaObjex implements Identified<SchemaObjex, Key> {

    /**
     * An objex should be created only by {@link SchemaCategory}.
     * The reason is that there are some invariants involving multiple objexes that need to be maintained by the category.
     */
    SchemaObjex(Key key, ObjexIds ids, boolean isEntity) {
        this.key = key;
        this.ids = ids;
        this.superId = Set.copyOf(ids.collectAllSignatures());
    }

    private final Key key;
    /** A unique identifier of the object (within one schema category). */
    public Key key() {
        return key;
    }

    private final ObjexIds ids;
    /** Each id is a set of signatures so that the correspondig set of attributes can unambiguosly identify this object (candidate key).
     *
     * @deprecated
     */
    public ObjexIds ids() {
        return ids;
    }

    private final Collection<Signature> superId;
    /** A union of all ids (super key). */
    public Collection<Signature> superId() {
        return superId;
    }

    /** Managed by {@link SchemaCategory}. */
    boolean isEntity;

    /**
     * There are two types of objexes - entites and properties.
     * Entities have outgoing morphisms, properties do not.
     * Entities have either signature identifier(s) or a generated identifier. Properties are identified by their value.
     */
    public boolean isEntity() {
        return isEntity;
    }

    public boolean hasSignatureId() {
        return ids.isSignatures();
    }

    public boolean hasGeneratedId() {
        return ids.isGenerated();
    }

    /** All base morphisms starting in this objex. Managed by {@link SchemaMorphism}. */
    final Map<BaseSignature, SchemaMorphism> morphismsFrom = new TreeMap<>();
    /** All base morphisms ending in this objex. Managed by {@link SchemaMorphism}. */
    final Map<BaseSignature, SchemaMorphism> morphismsTo = new TreeMap<>();

    public Collection<SchemaMorphism> from() {
        return morphismsFrom.values();
    }

    public Collection<SchemaMorphism> to() {
        return morphismsTo.values();
    }

    public SchemaMorphism from(BaseSignature signature) {
        return morphismsFrom.get(signature);
    }

    public SchemaMorphism to(BaseSignature signature) {
        return morphismsTo.get(signature);
    }

    // Identification

    @Override public Key identifier() {
        return key;
    }

    @Override public boolean equals(Object other) {
        return other instanceof SchemaObjex schemaObject && key.equals(schemaObject.key);
    }

    @Override public int hashCode() {
        return key.hashCode();
    }

    // Debug

    @Override public String toString() {
        return "O: " + key;
    }

}
