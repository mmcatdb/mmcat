package cz.matfyz.core.schema;

import cz.matfyz.core.category.Category;
import cz.matfyz.core.category.Morphism.Min;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.identification.MapUniqueContext;
import cz.matfyz.core.identification.UniqueContext;

import java.util.Collection;

/**
 * @author pavel.koupil, jachymb.bartik
 */
public class SchemaCategory implements Category {
    
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
        if (signature.isBaseDual())
            throw MorphismNotFoundException.signatureIsDual(signature);

        SchemaMorphism morphism = morphismContext.getUniqueObject(signature);
        if (morphism == null) {
            if (signature.isEmpty() || signature.isBase())
                throw MorphismNotFoundException.baseNotFound(signature);

            SchemaMorphism newMorphism = createCompositeMorphism(signature);
            morphism = morphismContext.createUniqueObject(newMorphism);
        }

        return morphism;
    }

    public record SchemaEdge(
        SchemaMorphism morphism,
        boolean direction
    ) {
        public Signature signature() {
            return direction ? morphism.signature() : morphism.signature().dual();
        }

        public SchemaObject dom() {
            return direction ? morphism.dom() : morphism.cod();
        }

        public SchemaObject cod() {
            return direction ? morphism.cod() : morphism.dom();
        }

        public boolean isArray() {
            return !direction;
        }
    }
    
    public SchemaEdge getEdge(Signature signature) {
        return new SchemaEdge(
            getMorphism(signature.isBaseDual() ? signature.dual() : signature),
            !signature.isBaseDual()
        );
    }

    public Collection<SchemaObject> allObjects() {
        return objectContext.getAllUniqueObjects();
    }

    public Collection<SchemaMorphism> allMorphisms() {
        return morphismContext.getAllUniqueObjects();
    }

    private SchemaMorphism createCompositeMorphism(Signature signature) {
        Signature[] bases = signature.toBases().toArray(Signature[]::new);

        Signature lastSignature = bases[0];
        SchemaMorphism lastMorphism = this.getMorphism(lastSignature);
        SchemaObject dom = lastMorphism.dom();
        SchemaObject cod = lastMorphism.cod();
        Min min = lastMorphism.min();

        for (final var base : bases) {
            lastMorphism = this.getMorphism(base);
            cod = lastMorphism.cod();
            min = SchemaMorphism.combineMin(min, lastMorphism.min());
        }

        return new SchemaMorphism.Builder().fromArguments(signature, dom, cod, min);
    }

    public static class Editor {

        protected UniqueContext<SchemaObject, Key> getObjectContext(SchemaCategory category) {
            return category.objectContext;
        }

        protected UniqueContext<SchemaMorphism, Signature> getMorphismContext(SchemaCategory category) {
            return category.morphismContext;
        }

    }

}
