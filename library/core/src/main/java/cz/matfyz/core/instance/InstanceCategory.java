package cz.matfyz.core.instance;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;
import cz.matfyz.core.schema.SchemaMorphism.Min;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author pavel.koupil, jachym.bartik
 */
public class InstanceCategory {

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
            SchemaMorphism schemaMorphism = schema.getMorphism(x);
            InstanceObject dom = getObject(schemaMorphism.dom().key());
            InstanceObject cod = getObject(schemaMorphism.cod().key());

            return new InstanceMorphism(schemaMorphism, dom, cod, this);
        });
    }

    public InstanceMorphism getMorphism(SchemaMorphism schemaMorphism) {
        return this.getMorphism(schemaMorphism.signature());
    }

    public record InstanceEdge(
        InstanceMorphism morphism,
        boolean direction
    ) {
        public Signature signature() {
            return direction ? morphism.signature() : morphism.signature().dual();
        }

        public InstanceObject dom() {
            return direction ? morphism.dom() : morphism.cod();
        }

        public InstanceObject cod() {
            return direction ? morphism.cod() : morphism.dom();
        }

        public boolean isArray() {
            return !direction;
        }

        public void createMapping(DomainRow domainRow, DomainRow codomainRow) {
            if (direction)
                morphism.createMapping(domainRow, codomainRow);
            else
                morphism.createMapping(codomainRow, domainRow);
        }
    }

    public InstanceEdge getEdge(BaseSignature base) {
        return new InstanceEdge(
            getMorphism(base.isDual() ? base.dual() : base),
            !base.isDual()
        );
    }

    public record InstancePath(
        List<InstanceEdge> edges,
        Signature signature
    ) {
        public InstanceObject dom() {
            return edges.get(0).dom();
        }

        public InstanceObject cod() {
            return edges.get(edges.size() - 1).cod();
        }

        public boolean isArray() {
            for (final var edge : edges)
                if (edge.isArray())
                    return true;

            return false;
        }

        public Min min() {
            for (final var edge : edges)
                if (edge.isArray() || edge.morphism.min() == Min.ZERO)
                    return Min.ZERO;

            return Min.ONE;
        }
    }

    public InstancePath getPath(Signature signature) {
        final var list = new ArrayList<InstanceEdge>();
        signature.toBases().stream().map(this::getEdge).forEach(list::add);

        return new InstancePath(list, signature);
    }

    public void createReferences() {
        for (var object : objects.values())
            for (var signature : object.superId().signatures())
                createReferencesForSignature(signature);
    }

    private void createReferencesForSignature(Signature signature) {
        if (!signature.isComposite())
            return;

        var baseSignatures = signature.toBases();
        var signatureToTarget = Signature.createEmpty();

        for (int i = 0; i < baseSignatures.size() - 1; i++) {
            var currentBase = baseSignatures.get(i);
            var signatureInTarget = Signature.concatenate(baseSignatures.subList(i + 1, baseSignatures.size()));
            signatureToTarget = signatureToTarget.concatenate(currentBase);

            var pathFromTarget = getPath(signatureToTarget.dual());
            var currentTarget = pathFromTarget.cod();
            if (!currentTarget.superId().hasSignature(signatureInTarget))
                continue;

            currentTarget.addReferenceToRow(signatureInTarget, pathFromTarget, signature);
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
       
        /*builder.append("Objects :\n");
        for (InstanceObject object : objects.values())
            //if (!object.isEmpty())
            builder.append(object).append("\n");
        builder.append("\n");*/

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
