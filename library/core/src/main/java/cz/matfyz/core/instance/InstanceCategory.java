package cz.matfyz.core.instance;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaObjex;

import java.util.Collection;
import java.util.Map;

public class InstanceCategory {

    private final SchemaCategory schema;
    private final Map<Key, InstanceObjex> objexes;
    private final Map<BaseSignature, InstanceMorphism> morphisms;

    InstanceCategory(SchemaCategory schema, Map<Key, InstanceObjex> objexes, Map<BaseSignature, InstanceMorphism> morphisms) {
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

    /**
     * Only base morphisms that already exists are returned.
     * All other signatures throw an exception.
     */
    public InstanceMorphism getMorphism(BaseSignature signature) {
        final var morphism = morphisms.get(signature);
        if (morphism != null)
            return morphism;

        // Now we just need to decide why the signature is wrong.
        if (signature.isEmpty())
            throw MorphismNotFoundException.signatureIsEmpty();
        if (!(signature instanceof final BaseSignature baseSignature))
            throw MorphismNotFoundException.signatureIsComposite(signature);
        if (baseSignature.isDual())
            throw MorphismNotFoundException.signatureIsDual(baseSignature);
        throw MorphismNotFoundException.baseNotFound(baseSignature);
    }

    public Collection<InstanceObjex> allObjexes() {
        return this.objexes.values();
    }

    public Collection<InstanceMorphism> allMorphisms() {
        return this.morphisms.values();
    }

    public void createReferences() {
        for (final var objex : objexes.values())
            for (final var signature : objex.schema.superId())
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
            if (!currentTarget.superId().contains(signatureInTarget))
                continue;

            getObjex(currentTarget).addReference(signatureInTarget, pathFromTarget, signature);
        }
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();

        sb.append("Keys: ");
        for (Key key : objexes.keySet())
            sb.append(key).append(", ");
        sb.append("\n\n");

        sb.append("Objexes (showing only non-empty):\n");
        for (InstanceObjex objex : objexes.values())
            if (!objex.isEmpty())
                sb.append(objex).append("\n");
        sb.append("\n");

        sb.append("Signatures: ");
        for (Signature signature : morphisms.keySet())
            sb.append(signature).append(", ");
        sb.append("\n\n");

        sb.append("Morphisms (showing only non-empty):\n");
        for (InstanceMorphism morphism : morphisms.values())
            if (!morphism.isEmpty())
                sb.append(morphism).append("\n");
        sb.append("\n");

        return sb.toString();
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

        protected static Map<BaseSignature, InstanceMorphism> getMorphisms(InstanceCategory category) {
            return category.morphisms;
        }

    }

}
