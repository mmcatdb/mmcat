package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Morphism;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.identification.Identified;

import java.util.Set;

/**
 * @author pavel.koupil, jachym.bartik
 */
public class SchemaMorphism implements Morphism, Identified<Signature> {
    
    private Signature signature;
    public final String label;
    private SchemaObject dom;
    private SchemaObject cod;
    private Min min;

    public final String iri;
    public final String pimIri;
    private final Set<Tag> tags;

    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    public static Min combineMin(Min min1, Min min2) {
        return (min1 == Min.ONE && min2 == Min.ONE) ? Min.ONE : Min.ZERO;
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

    @Override
    public SchemaObject dom() {
        return dom;
    }

    @Override
    public SchemaObject cod() {
        return cod;
    }

    @Override
    public Min min() {
        return min;
    }

    public Set<Tag> tags() {
        return tags;
    }

    public boolean isBase() {
        return signature.isBase();
    }

    @Override
    public Signature signature() {
        return signature;
    }

    @Override
    public Signature identifier() {
        return signature;
    }

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

}
