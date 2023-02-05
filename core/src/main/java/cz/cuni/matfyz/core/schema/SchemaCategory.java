package cz.cuni.matfyz.core.schema;

import cz.cuni.matfyz.core.category.Category;
import cz.cuni.matfyz.core.category.Morphism.Max;
import cz.cuni.matfyz.core.category.Morphism.Min;
import cz.cuni.matfyz.core.identification.MapUniqueContext;
import cz.cuni.matfyz.core.identification.UniqueContext;
import cz.cuni.matfyz.core.category.Signature;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author pavel.koupil, jachymb.bartik
 */
public class SchemaCategory implements Serializable, Category {
    
    private final UniqueContext<SchemaObject, Key> objectContext = new MapUniqueContext<>();
    private final UniqueContext<SchemaMorphism, Signature> morphismContext = new MapUniqueContext<>();

    public SchemaObject addObject(SchemaObject object) {
        return objectContext.createUniqueObject(object);
    }

    public SchemaMorphism addMorphism(SchemaMorphism morphism) {
        var newMorphism = morphismContext.createUniqueObject(morphism);
        newMorphism.setCategory(this);
        return newMorphism;
    }

    public void deleteMorphism(SchemaMorphism morphism) {
        morphismContext.deleteUniqueObject(morphism);
    }

    public SchemaMorphism dual(Signature signatureOfOriginal) {
        return getMorphism(signatureOfOriginal.dual());
    }

    public SchemaObject getObject(Key key) {
        return objectContext.getUniqueObject(key);
    }
    
    public SchemaMorphism getMorphism(Signature signature) {
        SchemaMorphism morphism = morphismContext.getUniqueObject(signature);
        if (morphism == null) {
            SchemaMorphism  newMorphism = createCompositeMorphism(signature);
            morphism = morphismContext.createUniqueObject(newMorphism);
        }

        return morphism;
    }

    public Collection<SchemaObject> allObjects() {
        return objectContext.getAllUniqueObjects();
    }

    public Collection<SchemaMorphism> allMorphisms() {
        return morphismContext.getAllUniqueObjects();
    }

    private SchemaMorphism createCompositeMorphism(Signature signature) {
        Signature[] bases = signature.toBases().toArray(Signature[]::new);

        Signature lastSignature = bases[bases.length - 1];
        SchemaMorphism lastMorphism = this.getMorphism(lastSignature);
        SchemaObject dom = lastMorphism.dom();
        SchemaObject cod = lastMorphism.cod();
        Min min = lastMorphism.min();
        Max max = lastMorphism.max();

        for (int i = 2; i <= bases.length; i++) {
            lastSignature = bases[bases.length - i];
            lastMorphism = this.getMorphism(lastSignature);
            cod = lastMorphism.cod();
            min = SchemaMorphism.combineMin(min, lastMorphism.min());
            max = SchemaMorphism.combineMax(max, lastMorphism.max());
        }

        return new SchemaMorphism.Builder().fromArguments(signature, dom, cod, min, max);
    }

}
