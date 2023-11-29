package cz.matfyz.core.schema;

import cz.matfyz.core.category.BaseSignature;
import cz.matfyz.core.category.Morphism;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.identification.Identified;

import java.util.Set;

/**
 * @author pavel.koupil, jachym.bartik
 */
public class SchemaMorphism implements Morphism, Identified<Signature> {
    
    private  final Signature signature;
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

    /**
     * Replace old version of dom/cod by its newer version (which has the same key).
     */
    public void updateObject(SchemaObject object) {
        if (this.dom.equals(object))
            this.dom = object;
        if (this.cod.equals(object))
            this.cod = object;
    }

    @Override
    public Min min() {
        return min;
    }

    public Set<Tag> tags() {
        return tags;
    }

    public boolean isBase() {
        return signature instanceof BaseSignature;
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

    public static record DisconnectedSchemaMorphism(
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
            return toSchemaMorphism(provider.getObject(codKey), provider.getObject(domKey));
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
