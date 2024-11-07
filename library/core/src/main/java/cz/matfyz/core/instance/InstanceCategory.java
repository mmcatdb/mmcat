package cz.matfyz.core.instance;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.Collection;
import java.util.Map;

public class InstanceCategory {

    private final SchemaCategory schema;
    private final Map<Key, InstanceObject> objects;
    private final Map<Signature, InstanceMorphism> morphisms;

    InstanceCategory(SchemaCategory schema, Map<Key, InstanceObject> objects, Map<Signature, InstanceMorphism> morphisms) {
        this.schema = schema;
        this.objects = objects;
        this.morphisms = morphisms;
    }

    public InstanceObject getObject(Key key) {
        return objects.get(key);
    }

    public InstanceObject getObject(SchemaObject schemaObject) {
        return this.getObject(schemaObject.key());
    }

    public InstanceMorphism getMorphism(Signature signature) {
        if (signature.isEmpty())
            throw MorphismNotFoundException.signatureIsEmpty();

        if (signature instanceof BaseSignature baseSignature) {
            if (baseSignature.isDual())
                throw MorphismNotFoundException.signatureIsDual(baseSignature);

            return morphisms.computeIfAbsent(baseSignature, x -> {
                throw MorphismNotFoundException.baseNotFound(baseSignature);
            });
        }

        return morphisms.computeIfAbsent(signature, x -> {
            // The composite morphisms are created dynamically when needed.
            return new InstanceMorphism(schema.getMorphism(x));
        });
    }

    public InstanceMorphism getMorphism(SchemaMorphism schemaMorphism) {
        return this.getMorphism(schemaMorphism.signature());
    }

    public Collection<InstanceObject> allObjects() {
        return schema.allObjects().stream().map(this::getObject).toList();
    }

    public Collection<InstanceMorphism> allMorphisms() {
        return schema.allMorphisms().stream().map(this::getMorphism).toList();
    }

    public void createReferences() {
        for (final var object : objects.values())
            for (final var signature : object.schema.superId().signatures())
                createReferencesForSignature(signature);
    }

    private void createReferencesForSignature(Signature signature) {
        if (!signature.isComposite())
            return;

        final var baseSignatures = signature.toBases();
        var signatureToTarget = Signature.createEmpty();

        for (int i = 0; i < baseSignatures.size() - 1; i++) {
            final var currentBase = baseSignatures.get(i);
            final var signatureInTarget = Signature.concatenate(baseSignatures.subList(i + 1, baseSignatures.size()));
            signatureToTarget = signatureToTarget.concatenate(currentBase);

            final var pathFromTarget = schema.getPath(signatureToTarget.dual());
            final var currentTarget = pathFromTarget.to();
            if (!currentTarget.superId().hasSignature(signatureInTarget))
                continue;

            getObject(currentTarget).addReferenceToRow(signatureInTarget, pathFromTarget, signature);
        }
    }

    @Override public String toString() {
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

    @Override public boolean equals(Object object) {
        return object instanceof InstanceCategory category
            && objects.equals(category.objects)
            && morphisms.equals(category.morphisms);
    }

}
