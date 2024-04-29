package cz.matfyz.core.schema;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.MapUniqueContext;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.UniqueContext;
import cz.matfyz.core.schema.SchemaMorphism.Min;

import java.util.Collection;
import java.util.Set;

public class SchemaCategory {

    public final String label;

    public SchemaCategory(String label) {
        this.label = label;
    }

    private final UniqueContext<SchemaObject, Key> objectContext = new MapUniqueContext<>();
    private final UniqueContext<SchemaMorphism, Signature> morphismContext = new MapUniqueContext<>();

    public SchemaObject addObject(SchemaObject object) {
        return objectContext.createUniqueObject(object);
    }

    public SchemaMorphism addMorphism(SchemaMorphism morphism) {
        return morphismContext.createUniqueObject(morphism);
    }

    public void removeMorphism(SchemaMorphism morphism) {
        morphismContext.deleteUniqueObject(morphism);
    }

    public SchemaObject getObject(Key key) {
        return objectContext.getUniqueObject(key);
    }

    public SchemaMorphism getMorphism(Signature signature) {
        if (signature.isEmpty())
            throw MorphismNotFoundException.signatureIsEmpty();

        if (signature instanceof BaseSignature baseSignature) {
            if (baseSignature.isDual())
                throw MorphismNotFoundException.signatureIsDual(baseSignature);

            final SchemaMorphism baseMorphism = morphismContext.getUniqueObject(baseSignature);
            if (baseMorphism == null)
                throw MorphismNotFoundException.baseNotFound(baseSignature);

            return baseMorphism;
        }

        final SchemaMorphism morphism = morphismContext.getUniqueObject(signature);
        if (morphism != null)
            return morphism;

        final SchemaMorphism newMorphism = createCompositeMorphism(signature);
        return morphismContext.createUniqueObject(newMorphism);
    }

    /**
     * This class represents a directed edge in the schema category. Essentially, it's either a base morphism or a dual of such.
     */
    public record SchemaEdge(
        SchemaMorphism morphism,
        /** True if the edge corresponds to the morphism. False if it corresponds to its dual. */
        boolean direction
    ) {
        public Signature signature() {
            return direction ? morphism.signature() : morphism.signature().dual();
        }

        public SchemaObject from() {
            return direction ? morphism.dom() : morphism.cod();
        }

        public SchemaObject to() {
            return direction ? morphism.cod() : morphism.dom();
        }

        public boolean isArray() {
            return !direction;
        }
    }

    public SchemaEdge getEdge(BaseSignature base) {
        return new SchemaEdge(
            getMorphism(base.toNonDual()),
            !base.isDual()
        );
    }

    public Collection<SchemaObject> allObjects() {
        return objectContext.getAllUniqueObjects();
    }

    public Collection<SchemaMorphism> allMorphisms() {
        return morphismContext.getAllUniqueObjects();
    }

    public boolean hasObject(Key key) {
        return objectContext.getUniqueObject(key) != null;
    }

    public boolean hasMorphism(Signature signature) {
        return morphismContext.getUniqueObject(signature) != null;
    }

    public boolean hasEdge(BaseSignature base) {
        return hasMorphism(base.toNonDual());
    }

    /** Returns whether the object (corresponding to the given key) appears in any inner node of the (composite) morphism (corresponding to the given signature). */
    public boolean morphismContainsObject(Signature signature, Key key) {
        return signature
            .cutLast().toBases().stream()
            .anyMatch(base -> getEdge(base).to().key().equals(key));
    }

    private int lastCompositeId = 0;

    private SchemaMorphism createCompositeMorphism(Signature signature) {
        final String morphismLabel = "composite" + lastCompositeId++;

        final Signature[] bases = signature.toBases().toArray(Signature[]::new);

        final Signature lastSignature = bases[0];
        SchemaMorphism lastMorphism = this.getMorphism(lastSignature);
        final SchemaObject dom = lastMorphism.dom();
        SchemaObject cod = lastMorphism.cod();
        Min min = lastMorphism.min();

        for (final var base : bases) {
            lastMorphism = this.getMorphism(base);
            cod = lastMorphism.cod();
            min = Min.combine(min, lastMorphism.min());
        }

        return new SchemaMorphism(signature, morphismLabel, min, Set.of(), dom, cod);
    }

    public abstract static class Editor {

        protected static UniqueContext<SchemaObject, Key> getObjectContext(SchemaCategory category) {
            return category.objectContext;
        }

        protected static UniqueContext<SchemaMorphism, Signature> getMorphismContext(SchemaCategory category) {
            return category.morphismContext;
        }

    }

}
