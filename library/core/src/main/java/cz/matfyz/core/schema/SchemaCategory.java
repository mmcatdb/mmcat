package cz.matfyz.core.schema;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.exception.SchemaException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.Key.KeyGenerator;
import cz.matfyz.core.identifiers.Signature.SignatureGenerator;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class SchemaCategory {

    private final Map<Key, SchemaObjex> objexes = new TreeMap<>();
    private final Map<Signature, SchemaMorphism> morphisms = new TreeMap<>();

    public SchemaObjex getObjex(Key key) {
        return objexes.get(key);
    }

    public SchemaObjex addObjex(SerializedObjex serialized) {
        final var objex = new SchemaObjex(serialized.key(), serialized.ids(), false);
        objexes.put(objex.key(), objex);
        return objex;
    }

    public SchemaObjex addObjexCopy(SchemaObjex objex) {
        return addObjex(SerializedObjex.serialize(objex));
    }

    public void removeObjex(SchemaObjex objex) {
        removeObjex(objex.key());
    }

    public void removeObjex(SerializedObjex objex) {
        removeObjex(objex.key());
    }

    public void removeObjex(Key key) {
        final var objex = objexes.get(key);
        if (objex == null)
            throw SchemaException.removingNonExistingObjex(key);

        final List<Signature> signaturesOfDependentMorphisms = morphisms.values().stream()
            .filter(morphism -> morphism.dom().equals(objex) || morphism.cod().equals(objex))
            .map(SchemaMorphism::signature)
            .toList();

        if (!signaturesOfDependentMorphisms.isEmpty())
            throw SchemaException.removedObjexDependsOnMorphisms(objex.key(), signaturesOfDependentMorphisms);

        objexes.remove(key);
    }

    public SchemaObjex replaceObjex(SerializedObjex serialized) {
        final var key = serialized.key();
        final var existing = objexes.get(key);
        if (existing == null)
            throw SchemaException.replacingNonExistingObjex(key);

        final var objex = new SchemaObjex(key, serialized.ids(), existing.isEntity());

        objexes.put(key, objex);
        morphisms.values().forEach(morphism -> morphism.updateObjex(objex));

        return objex;
    }

    public SchemaMorphism addMorphism(SerializedMorphism serialized) {
        final var dom = getObjex(serialized.domKey());
        final var cod = getObjex(serialized.codKey());

        // FIXME check isEntity

        final var morphism = new SchemaMorphism(serialized.signature(), dom, cod, serialized.min(), serialized.tags());
        morphisms.put(morphism.signature(), morphism);

        return morphism;
    }

    public SchemaMorphism addMorphismCopy(SchemaMorphism morphism) {
        return addMorphism(SerializedMorphism.serialize(morphism));
    }

    public void removeMorphism(SchemaMorphism morphism) {
        removeMorphism(morphism.signature());
    }

    public void removeMorphism(SerializedMorphism morphism) {
        removeMorphism(morphism.signature());
    }

    public void removeMorphism(Signature signature) {
        // FIXME check isEntity

        morphisms.remove(signature);
    }


    public void replaceMorphism(SerializedMorphism morphism) {
        // TODO maybe some integriy check is needed here? Or comment why not ...
        removeMorphism(morphism);
        addMorphism(morphism);
    }

    public SchemaMorphism getMorphism(Signature signature) {
        if (signature.isEmpty())
            throw MorphismNotFoundException.signatureIsEmpty();

        if (signature instanceof BaseSignature baseSignature) {
            if (baseSignature.isDual())
                throw MorphismNotFoundException.signatureIsDual(baseSignature);

            return morphisms.computeIfAbsent(baseSignature, x -> {
                throw MorphismNotFoundException.baseNotFound(baseSignature);
            });
        }

        return morphisms.computeIfAbsent(signature, this::createCompositeMorphism);
    }

    /**
     * This class represents a directed edge in the schema category. Essentially, it's either a base morphism or a dual of such.
     */
    public record SchemaEdge(
        /** A base morphism. */
        SchemaMorphism morphism,
        /** True if the edge corresponds to the morphism. False if it corresponds to its dual. */
        boolean direction
    ) {
        public BaseSignature signature() {
            return (BaseSignature) (direction ? morphism.signature() : morphism.signature().dual());
        }

        public BaseSignature absoluteSignature() {
            return (BaseSignature) morphism.signature();
        }

        public SchemaObjex from() {
            return direction ? morphism.dom() : morphism.cod();
        }

        public SchemaObjex to() {
            return direction ? morphism.cod() : morphism.dom();
        }

        public boolean isArray() {
            return !direction;
        }
    }

    public SchemaEdge getEdge(BaseSignature base) {
        return new SchemaEdge(
            getMorphism(base.toAbsolute()),
            !base.isDual()
        );
    }

    public record SchemaPath(
        List<SchemaEdge> edges,
        Signature signature
    ) {
        public SchemaObjex from() {
            return edges.get(0).from();
        }

        public SchemaObjex to() {
            return edges.get(edges.size() - 1).to();
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

    public SchemaPath getPath(Signature signature) {
        final var list = new ArrayList<SchemaEdge>();
        signature.toBases().stream().map(this::getEdge).forEach(list::add);

        return new SchemaPath(list, signature);
    }

    public Collection<SchemaObjex> allObjexes() {
        return objexes.values();
    }

    public Collection<SchemaMorphism> allMorphisms() {
        return morphisms.values();
    }

    public boolean hasObjex(Key key) {
        return objexes.containsKey(key);
    }

    public boolean hasMorphism(Signature signature) {
        return morphisms.containsKey(signature);
    }

    public boolean hasEdge(BaseSignature base) {
        return hasMorphism(base.toAbsolute());
    }

    /** Returns whether the objex (corresponding to the given key) appears in any inner node of the (composite) morphism (corresponding to the given signature). */
    public boolean morphismContainsObjex(Signature signature, Key key) {
        return signature
            .cutLast().toBases().stream()
            .anyMatch(base -> getEdge(base).to().key().equals(key));
    }

    private SchemaMorphism createCompositeMorphism(Signature signature) {
        final Signature[] bases = signature.toBases().toArray(Signature[]::new);

        final Signature lastSignature = bases[0];
        SchemaMorphism lastMorphism = this.getMorphism(lastSignature);
        final SchemaObjex dom = lastMorphism.dom();
        SchemaObjex cod = lastMorphism.cod();
        Min min = lastMorphism.min();

        for (final var base : bases) {
            lastMorphism = this.getMorphism(base);
            cod = lastMorphism.cod();
            min = Min.combine(min, lastMorphism.min());
        }

        return new SchemaMorphism(signature, dom, cod, min, Set.of());
    }

    public KeyGenerator createKeyGenerator() {
        return KeyGenerator.create(objexes.keySet());
    }

    public SignatureGenerator createSignatureGenerator() {
        return SignatureGenerator.create(morphisms.keySet());
    }

}
