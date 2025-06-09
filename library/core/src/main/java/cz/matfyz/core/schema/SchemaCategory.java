package cz.matfyz.core.schema;

import cz.matfyz.core.exception.MorphismNotFoundException;
import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaMorphism.Min;

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

    public SchemaObjex addObjex(SchemaObjex objex) {
        return objexes.put(objex.key(), objex);
    }

    public SchemaMorphism addMorphism(SchemaMorphism morphism) {
        return morphisms.put(morphism.signature(), morphism);
    }

    public void removeMorphism(SchemaMorphism morphism) {
        morphisms.remove(morphism.signature());
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
            getMorphism(base.toNonDual()),
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
        return hasMorphism(base.toNonDual());
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

    public abstract static class Editor {

        protected static Map<Key, SchemaObjex> getObjexes(SchemaCategory category) {
            return category.objexes;
        }

        protected static Map<Signature, SchemaMorphism> getMorphisms(SchemaCategory category) {
            return category.morphisms;
        }

    }

}
