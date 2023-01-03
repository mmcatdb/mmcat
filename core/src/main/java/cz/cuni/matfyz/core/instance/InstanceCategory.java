package cz.cuni.matfyz.core.instance;

import cz.cuni.matfyz.core.category.Category;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.category.Signature.Type;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;

import java.io.Serializable;
import java.util.Map;

/**
 * @author pavel.koupil, jachym.bartik
 */
public class InstanceCategory implements Serializable, Category {

    // Evolution extension
    public final SchemaCategory schema;
    private final Map<Key, InstanceObject> objects;
    private final Map<Signature, InstanceMorphism> morphisms;

    InstanceCategory(SchemaCategory schema, Map<Key, InstanceObject> objects, Map<Signature, InstanceMorphism> morphisms) {
        this.schema = schema;
        this.objects = objects;
        this.morphisms = morphisms;
    }
    
    public Map<Key, InstanceObject> objects() {
        return objects;
    }
    
    public Map<Signature, InstanceMorphism> morphisms() {
        return morphisms;
    }

    // Evolution extension
    public void deleteMorphism(InstanceMorphism morphism) {
        morphisms.remove(morphism.signature());
    }
    
    public InstanceObject getObject(Key key) {
        return objects.get(key);
    }

    public InstanceObject getObject(SchemaObject schemaObject) {
        return this.getObject(schemaObject.key());
    }
    
    public InstanceMorphism getMorphism(Signature signature) {
        return morphisms.computeIfAbsent(signature, x -> {
            // This must be a composite morphism. These are created dynamically so we have to add it dynamically.
            SchemaMorphism schemaMorphism = schema.getMorphism(signature);
            InstanceObject dom = getObject(schemaMorphism.dom().key());
            InstanceObject cod = getObject(schemaMorphism.cod().key());

            return new InstanceMorphism(schemaMorphism, dom, cod, this);
        });
    }

    public InstanceMorphism getMorphism(SchemaMorphism schemaMorphism) {
        return this.getMorphism(schemaMorphism.signature());
    }
    
    public InstanceMorphism dual(Signature signatureOfOriginal) {
        return getMorphism(signatureOfOriginal.dual());
    }

    public void createReferences() {
        for (var object : objects.values())
            for (var signature : object.superId().signatures())
                createReferencesForSignature(signature);
    }

    private void createReferencesForSignature(Signature signature) {
        if (signature.getType() != Type.COMPOSITE)
            return;

        var baseSignatures = signature.toBasesReverse();
        var signatureToTarget = Signature.createEmpty();

        for (int i = 0; i < baseSignatures.size() - 1; i++) {
            var currentBase = baseSignatures.get(i);
            var signatureInTarget = Signature.concatenate(baseSignatures.subList(i + 1, baseSignatures.size()));
            signatureToTarget = signatureToTarget.concatenate(currentBase);
            
            var pathFromTarget = getMorphism(signatureToTarget.dual());
            var currentTarget = pathFromTarget.cod();
            if (!currentTarget.superId().hasSignature(signatureInTarget))
                continue;

            currentTarget.addReferenceToRow(signatureInTarget, pathFromTarget, signature);
        }
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Keys: ");
        for (Key key : objects.keySet())
            builder.append(key).append(", ");
        builder.append("\n\n");
        

        builder.append("Objects (showing only non-empty):\n");
        for (InstanceObject object : objects.values())
            if (!object.isEmpty())
                builder.append(object).append("\n");
        builder.append("\n");

        builder.append("Signatures: ");
        for (Signature signature : morphisms.keySet())
            builder.append(signature).append(", ");
        builder.append("\n\n");
        
        builder.append("Morphisms (showing only non-empty):\n");
        for (InstanceMorphism morphism : morphisms.values())
            if (!morphism.isEmpty())
                builder.append(morphism).append("\n");
        builder.append("\n");

        return builder.toString();
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof InstanceCategory category
            && objects.equals(category.objects)
            && morphisms.equals(category.morphisms);
    }
}
