package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;

import java.util.Set;

public class SchemaMorphism implements Identified<SchemaMorphism, Signature> {

    public enum Min {
        ZERO,
        ONE;

        public static Min combine(Min min1, Min min2) {
            return (min1 == Min.ONE || min2 == Min.ONE) ? Min.ONE : Min.ZERO;
        }
    }

    public enum Tag {
        isa,
        role,
    }

    /** A unique identifier of the morphism (within one schema category). */
    private final Signature signature;
    /** A user-readable label. */
    public final String label;
    /** Cardinality of the morphism - either 1..0 or 1..1. The cardinality in the opposite direction isn't defined. */
    private final Min min;
    /** Some other qualities of the morphism (e.g., inheritance in the form of the ISA hierarchy). */
    private final Set<Tag> tags;
    /** The domain object (i.e., the source of the arrow). */
    private SchemaObject dom;
    /** The codomain object (i.e., the target of the arrow). */
    private SchemaObject cod;

    public SchemaMorphism(Signature signature, String label, Min min, Set<Tag> tags, SchemaObject dom, SchemaObject cod) {
        this.signature = signature;
        this.dom = dom;
        this.cod = cod;
        this.min = min;
        this.label = label;
        this.tags = Set.of(tags.toArray(Tag[]::new));
    }

    public Signature signature() {
        return signature;
    }

    public boolean isBase() {
        return signature instanceof BaseSignature;
    }

    public Min min() {
        return min;
    }

    public Set<Tag> tags() {
        return tags;
    }

    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    public SchemaObject dom() {
        return dom;
    }

    public SchemaObject cod() {
        return cod;
    }

    /**
     * Replace old version of dom/cod by its newer version (which has the same key).
     */
    public void updateObject(SchemaObject object) {
        if (this.dom.equals(object))
            this.dom = object;
        if (this.cod.equals(object))
            this.cod = object;
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

    // Identification

    public record DisconnectedSchemaMorphism(
        Signature signature,
        String label,
        Key domKey,
        Key codKey,
        Min min,
        Set<Tag> tags
    ) {

        public interface SchemaObjectProvider {
            SchemaObject getObject(Key key);
        }

        public SchemaMorphism toSchemaMorphism(SchemaObjectProvider provider) {
            return toSchemaMorphism(provider.getObject(domKey), provider.getObject(codKey));
        }

        public SchemaMorphism toSchemaMorphism(SchemaObject dom, SchemaObject cod) {
            final Set<Tag> tags = this.tags != null ? this.tags : Set.of();

            return new SchemaMorphism(signature, this.label, min, tags, dom, cod);
        }

    }

}
