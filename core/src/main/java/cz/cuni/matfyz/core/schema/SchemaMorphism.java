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
    public String label;
    private SchemaObject dom;
    private SchemaObject cod;
    private Min min;
    private Max max;

    public String iri;
    public String pimIri;
    private Set<Tag> tags;

    public boolean hasTag(Tag tag) {
        return tags.contains(tag);
    }

    public static Min combineMin(Min min1, Min min2) {
        return (min1 == Min.ONE && min2 == Min.ONE) ? Min.ONE : Min.ZERO;
    }

    public static Max combineMax(Max max1, Max max2) {
        return (max1 == Max.ONE && max2 == Max.ONE) ? Max.ONE : Max.STAR;
    }

    private SchemaCategory category;

    /*
    public static SchemaMorphism dual(SchemaMorphism morphism) {
        return SchemaMorphism.dual(morphism, 1, 1);
    }
    */

    /*
    public SchemaMorphism createDual(Min min, Max max) {
        SchemaMorphism result = new SchemaMorphism(signature.dual(), cod, dom, min, max);
        return result;
    }
    */

    //private SchemaMorphism(Signature signature, SchemaObject dom, SchemaObject cod, Min min, Max max)
    private SchemaMorphism(SchemaObject dom, SchemaObject cod) {
        //this.signature = signature;
        this.dom = dom;
        this.cod = cod;
        //this.min = min;
        //this.max = max;
    }

    public void setCategory(SchemaCategory category) {
        this.category = category;
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

    @Override
    public Max max() {
        return max;
    }

    public Set<Tag> tags() {
        return tags;
    }

    public boolean isArray() {
        return max == Max.STAR;
    }

    public boolean isBase() {
        return signature.isBase();
    }

    @Override
    public SchemaMorphism dual() {
        return category.dual(signature);
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

        public SchemaMorphism fromArguments(Signature signature, SchemaObject dom, SchemaObject cod, Min min, Max max, String label) {
            var morphism = new SchemaMorphism(dom, cod);
            morphism.signature = signature;
            morphism.min = min;
            morphism.max = max;
            morphism.label = label;

            return morphism;
        }

        public SchemaMorphism fromDual(SchemaMorphism dualMorphism, Min min, Max max) {
            var dom = dualMorphism.cod;
            var cod = dualMorphism.dom;
            return fromArguments(dualMorphism.signature.dual(), dom, cod, min, max, "");
        }

    }

}
