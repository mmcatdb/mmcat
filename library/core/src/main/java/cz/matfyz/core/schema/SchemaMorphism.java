package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Identified;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;

import java.util.Set;

/**
 * @author pavel.koupil, jachym.bartik
 */
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
        projection,
        key,
    }

    private final Signature signature;
    public final String label;
    private SchemaObject dom;
    private SchemaObject cod;
    private final Min min;

    public final String iri;
    public final String pimIri;
    private final Set<Tag> tags;

    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    private SchemaMorphism(Signature signature, SchemaObject dom, SchemaObject cod, Min min, String label, String iri, String pimIri, Set<Tag> tags) {
        this.signature = signature;
        this.dom = dom;
        this.cod = cod;
        this.min = min;
        this.label = label;
        this.iri = iri;
        this.pimIri = pimIri;
        this.tags = Set.of(tags.toArray(Tag[]::new));
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

    public Min min() {
        return min;
    }

    public Set<Tag> tags() {
        return tags;
    }

    public boolean isBase() {
        return signature instanceof BaseSignature;
    }

    public Signature signature() {
        return signature;
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

    public static class Builder {

        private String label = "";
        private String iri = "";
        private String pimIri = "";
        private Set<Tag> tags = Set.of();

        public SchemaMorphism fromArguments(Signature signature, SchemaObject dom, SchemaObject cod, Min min) {
            return new SchemaMorphism(
                signature,
                dom,
                cod,
                min,
                this.label,
                this.iri,
                this.pimIri,
                this.tags
            );
        }

        public Builder label(String label) {
            this.label = label;
            return this;
        }

        public Builder iri(String iri) {
            this.iri = iri;
            return this;
        }

        public Builder pimIri(String pimIri) {
            this.pimIri = pimIri;
            return this;
        }

        public Builder tags(Set<Tag> tags) {
            this.tags = tags;
            return this;
        }

    }

    public record DisconnectedSchemaMorphism(
        Signature signature,
        String label,
        Key domKey,
        Key codKey,
        Min min,
        String iri,
        String pimIri,
        Set<Tag> tags
    ) {

        public interface SchemaObjectProvider {
            SchemaObject getObject(Key key);
        }

        public SchemaMorphism toSchemaMorphism(SchemaObjectProvider provider) {
            return toSchemaMorphism(provider.getObject(domKey), provider.getObject(codKey));
        }

        public SchemaMorphism toSchemaMorphism(SchemaObject dom, SchemaObject cod) {
            return new SchemaMorphism.Builder()
                .label(this.label)
                .iri(this.iri)
                .pimIri(this.pimIri)
                .tags(this.tags != null ? this.tags : Set.of())
                .fromArguments(signature, dom, cod, min);
        }

    }

}
