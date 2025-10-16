package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Signature;

import java.util.Set;

public class SchemaMorphism implements Identified<SchemaMorphism, Signature> {

    /**
     * A morphism should be created only by {@link SchemaCategory}.
     * The reason is that there are some invariants involving multiple objexes that need to be maintained by the category.
     */
    SchemaMorphism(BaseSignature signature, SchemaObjex dom, SchemaObjex cod, Min min, Set<Tag> tags) {
        this.signature = signature;
        setDom(dom);
        setCod(cod);
        this.min = min;
        this.tags = Set.of(tags.toArray(Tag[]::new));
    }

    private final BaseSignature signature;
    /** A unique identifier of the morphism (within one schema category). */
    public BaseSignature signature() {
        return signature;
    }

    private SchemaObjex dom;
    /** The domain objex (i.e., the source of the arrow). */
    public SchemaObjex dom() {
        return dom;
    }

    void setDom(SchemaObjex objex) {
        if (this.dom != null)
            this.dom.morphismsFrom.remove(signature);

        this.dom = objex;
        dom.morphismsFrom.put(signature, this);
    }

    private SchemaObjex cod;
    /** The codomain objex (i.e., the target of the arrow). */
    public SchemaObjex cod() {
        return cod;
    }

    void setCod(SchemaObjex objex) {
        if (this.cod != null)
            this.cod.morphismsTo.remove(signature);

        this.cod = objex;
        cod.morphismsTo.put(signature, this);
    }

    void removeFromObjex() {
        dom.morphismsFrom.remove(signature);
        cod.morphismsTo.remove(signature);
    }

    /** Enum for limiting a morphism cardinality from the bottom. */
    public enum Min {
        ZERO,
        ONE;

        public static Min combine(Min min1, Min min2) {
            return (min1 == Min.ONE || min2 == Min.ONE) ? Min.ONE : Min.ZERO;
        }
    }

    private final Min min;
    /** Cardinality of the morphism - either 1..0 or 1..1. The cardinality in the opposite direction isn't defined. */
    public Min min() {
        return min;
    }

    /** Enum for specifying a morphism type (which were defined in the paper). */
    public enum Tag {
        isa,
        role,
    }

    private final Set<Tag> tags;
    /** Some other qualities of the morphism (e.g., inheritance in the form of the ISA hierarchy). */
    public Set<Tag> tags() {
        return tags;
    }

    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    // Identification

    @Override public BaseSignature identifier() {
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
