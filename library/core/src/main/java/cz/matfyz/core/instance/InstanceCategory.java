package cz.matfyz.core.instance;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;

import java.util.Collection;
import java.util.Map;

public class InstanceCategory {

    private final SchemaCategory schema;
    private final Map<Key, InstanceObjex> objexes;
    private final Map<Signature, InstanceMorphism> morphisms;

    InstanceCategory(SchemaCategory schema, Map<Key, InstanceObjex> objexes, Map<Signature, InstanceMorphism> morphisms) {
        this.schema = schema;
        this.objexes = objexes;
        this.morphisms = morphisms;
    }

    public SchemaCategory schema() {
        return schema;
    }

    public InstanceObjex getObjex(Key key) {
        return objexes.get(key);
    }

    public InstanceObjex getObjex(SchemaObjex schemaObjex) {
        return this.getObjex(schemaObjex.key());
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

    public Collection<InstanceObjex> allObjexes() {
        return schema.allObjexes().stream().map(this::getObjex).toList();
    }

    public Collection<InstanceMorphism> allMorphisms() {
        return schema.allMorphisms().stream().map(this::getMorphism).toList();
    }

    public void createReferences() {
        for (final var objex : objexes.values())
            for (final var signature : objex.schema.superId().signatures())
                createReferencesForSignature(signature);
    }

    private void createReferencesForSignature(Signature signature) {
        if (!signature.isComposite())
            return;

        final var bases = signature.toBases();
        var signatureToTarget = Signature.createEmpty();

        for (int i = 0; i < bases.size() - 1; i++) {
            final var currentBase = bases.get(i);
            final var signatureInTarget = Signature.concatenate(bases.subList(i + 1, bases.size()));
            signatureToTarget = signatureToTarget.concatenate(currentBase);

            final var pathFromTarget = schema.getPath(signatureToTarget.dual());
            final var currentTarget = pathFromTarget.to();
            if (!currentTarget.superId().hasSignature(signatureInTarget))
                continue;

            getObjex(currentTarget).addReference(signatureInTarget, pathFromTarget, signature);
        }
    }

    @Override public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("Keys: ");
        for (Key key : objexes.keySet())
            builder.append(key).append(", ");
        builder.append("\n\n");

        builder.append("Objexes (showing only non-empty):\n");
        for (InstanceObjex objex : objexes.values())
            if (!objex.isEmpty())
                builder.append(objex).append("\n");
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
            && objexes.equals(category.objexes)
            && morphisms.equals(category.morphisms);
    }

    public abstract static class Editor {

        protected static Map<Key, InstanceObjex> getObjexes(InstanceCategory category) {
            return category.objexes;
        }

        protected static Map<Signature, InstanceMorphism> getMorphisms(InstanceCategory category) {
            return category.morphisms;
        }

    }

}
