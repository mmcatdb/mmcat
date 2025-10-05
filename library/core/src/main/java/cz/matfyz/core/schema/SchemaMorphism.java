package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Signature;

import java.util.Set;

public class SchemaMorphism implements Identified<SchemaMorphism, Signature> {

    /** Enum for limiting a morphism cardinality from the bottom. */
    public enum Min {
        ZERO,
        ONE;

        public static Min combine(Min min1, Min min2) {
            return (min1 == Min.ONE || min2 == Min.ONE) ? Min.ONE : Min.ZERO;
        }
    }

    /** Enum for specifying a morphism type (which were defined in the paper). */
    public enum Tag {
        isa,
        role,
    }

    SchemaMorphism(Signature signature, SchemaObjex dom, SchemaObjex cod, Min min, Set<Tag> tags) {
        this.signature = signature;
        this.dom = dom;
        this.cod = cod;
        this.min = min;
        this.tags = Set.of(tags.toArray(Tag[]::new));
    }

    private final Signature signature;
    /** A unique identifier of the morphism (within one schema category). */
    public Signature signature() {
        return signature;
    }

    public boolean isBase() {
        return signature instanceof BaseSignature;
    }

    private SchemaObjex dom;
    /** The domain objex (i.e., the source of the arrow). */
    public SchemaObjex dom() {
        return dom;
    }

    private SchemaObjex cod;
    /** The codomain objex (i.e., the target of the arrow). */
    public SchemaObjex cod() {
        return cod;
    }

    private final Min min;
    /** Cardinality of the morphism - either 1..0 or 1..1. The cardinality in the opposite direction isn't defined. */
    public Min min() {
        return min;
    }

    private final Set<Tag> tags;
    /** Some other qualities of the morphism (e.g., inheritance in the form of the ISA hierarchy). */
    public Set<Tag> tags() {
        return tags;
    }

    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    /**
     * Replace old version of dom/cod by its newer version (which has the same key).
     */
    void updateObjex(SchemaObjex objex) {
        if (this.dom.equals(objex))
            this.dom = objex;
        if (this.cod.equals(objex))
            this.cod = objex;
    }

    // Identification

    @Override public Signature identifier() {
        return signature;
    }

    @Override public boolean equals(Object other) {
        return other instanceof SchemaMorphism schemaMorphism && signature.equals(schemaMorphism.signature);
    }

    @Override public int hashCode() {
        return signature.hashCode();
    }

    // Debug

    @Override public String toString() {
        return "(" + dom.key() + ")--[" + signature + "]->(" + cod.key() + ")";
    }

}
