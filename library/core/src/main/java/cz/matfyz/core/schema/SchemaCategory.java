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

        final List<BaseSignature> dependencies = new ArrayList<>();
        objex.from().stream().forEach(m -> dependencies.add(m.signature()));
        objex.to().stream().forEach(m -> dependencies.add(m.signature()));
        if (!dependencies.isEmpty())
            throw SchemaException.removedObjexDependsOnMorphisms(objex.key(), dependencies);

        objexes.remove(key);
    }

    public SchemaObjex replaceObjex(SerializedObjex serialized) {
        final var key = serialized.key();
        final var prev = objexes.get(key);
        if (prev == null)
            throw SchemaException.replacingNonExistingObjex(key);

        final var next = new SchemaObjex(key, serialized.ids(), prev.isEntity());
        objexes.put(key, next);

        // The `set` methods modify the collections which we can't do while iterating over them. So we have to collect them first.
        final var prevFrom = prev.from().stream().toList();
        prevFrom.stream().forEach(m -> m.setDom(next));
        final var prevTo = prev.to().stream().toList();
        prevTo.stream().forEach(m -> m.setCod(next));

        return next;
    }

    public SchemaMorphism getMorphism(BaseSignature signature) {
        if (signature.isDual())
            throw MorphismNotFoundException.signatureIsDual(signature);

        final var morphism = morphisms.get(signature);
        if (morphism == null)
            throw MorphismNotFoundException.baseNotFound(signature);

        return morphism;
    }

    public SchemaMorphism addMorphism(SerializedMorphism serialized) {
        final var dom = getObjex(serialized.domKey());
        final var cod = getObjex(serialized.codKey());

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

    public void removeMorphism(BaseSignature signature) {
        final var morphism = morphisms.get(signature);
        if (morphism == null)
            throw SchemaException.removingNonExistingMorphism(signature);

        morphism.removeFromObjex();
        morphisms.remove(signature);
    }

    public void replaceMorphism(SerializedMorphism morphism) {
        // Unlike objexes, morphisms can don't depend on anything, so they can be simply removed and added again.
        removeMorphism(morphism);
        addMorphism(morphism);
    }

    /**
     * This class represents a directed edge in the schema category. Essentially, it's either a base morphism or a dual of such.
     */
    public record SchemaEdge(
        SchemaMorphism morphism,
        /** True if the edge corresponds to the morphism. False if it corresponds to its dual. */
        boolean direction
    ) {
        public BaseSignature signature() {
            return (direction ? morphism.signature() : morphism.signature().dual());
        }

        public BaseSignature absoluteSignature() {
            return morphism.signature();
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

    public KeyGenerator createKeyGenerator() {
        return KeyGenerator.create(objexes.keySet());
    }

    public SignatureGenerator createSignatureGenerator() {
        return SignatureGenerator.create(morphisms.keySet());
    }

}
