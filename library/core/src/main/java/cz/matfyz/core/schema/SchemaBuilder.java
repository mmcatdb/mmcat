package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaMorphism.Tag;
import cz.matfyz.core.utils.SequenceGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SchemaBuilder {

    private String schemaLabel;

    public SchemaBuilder(String schemaLabel) {
        this.schemaLabel = schemaLabel;
    }

    // Schema object

    public static class BuilderObject implements Comparable<BuilderObject> {

        private final Key key;
        private final String label;
        // These needs to be updated later, because the morphisms might not be defined yet.
        ObjectIds ids;
        SignatureId superId;

        public BuilderObject(Key key, String label, ObjectIds ids) {
            this.key = key;
            this.label = label;
            this.ids = ids;
            this.superId = ids.generateDefaultSuperId();
        }

        public Key key() {
            return key;
        }

        public String label() {
            return label;
        }

        public ObjectIds ids() {
            return ids;
        }

        public SignatureId superId() {
            return superId;
        }

        @Override public int compareTo(BuilderObject other) {
            return key.compareTo(other.key);
        }

    }

    private SequenceGenerator keyGenerator = new SequenceGenerator(1);
    private Map<Key, BuilderObject> objectsByKey = new TreeMap<>();
    private Map<String, BuilderObject> objectsByLabel = new TreeMap<>();

    private boolean nextIdsIsGenerated = false;
    public SchemaBuilder generatedIds() {
        this.nextIdsIsGenerated = true;
        return this;
    }

    public BuilderObject object(String label) {
        return object(label, new Key(keyGenerator.next()));
    }

    public BuilderObject object(String label, int key) {
        return object(label, new Key(keyGenerator.next(key)));
    }

    private BuilderObject object(String label, Key key) {
        final var ids = nextIdsIsGenerated ? ObjectIds.createGenerated() : ObjectIds.createValue();
        final var object = new BuilderObject(key, label, ids);
        objectsByKey.put(object.key(), object);
        objectsByLabel.put(object.label(), object);

        nextIdsIsGenerated = false;

        return object;
    }

    // Schema morphism

    public record BuilderMorphism(
        Signature signature,
        String label,
        BuilderObject dom,
        BuilderObject cod,
        Min min,
        Set<Tag> tags
    ) implements Comparable<BuilderMorphism> {

        public Key domKey() {
            return dom.key();
        }

        public Key codKey() {
            return cod.key();
        }

        @Override public int compareTo(BuilderMorphism other) {
            return signature.compareTo(other.signature);
        }

        public Signature dual() {
            return signature.dual();
        }

    }

    private SequenceGenerator signatureGenerator = new SequenceGenerator(1);
    private Map<Signature, BuilderMorphism> morphismsBySignature = new TreeMap<>();
    private Map<String, BuilderMorphism> morphismsByLabel = new TreeMap<>();

    // By default, we don't want any label because that would be a mess in the graph view.
    private String nextLabel = "";
    public SchemaBuilder label(String nextLabel) {
        this.nextLabel = nextLabel;
        return this;
    }

    private Min nextMin = Min.ONE;
    public SchemaBuilder min(Min nextMin) {
        this.nextMin = nextMin;
        return this;
    }

    private Set<Tag> nextTags = Set.of();
    public SchemaBuilder tags(Tag... nextTags) {
        this.nextTags = Set.of(nextTags);
        return this;
    }

    public BuilderMorphism morphism(BuilderObject dom, BuilderObject cod) {
        return morphism(dom, cod, Signature.createBase(signatureGenerator.next()));
    }

    public BuilderMorphism morphism(BuilderObject dom, BuilderObject cod, int signature) {
        return morphism(dom, cod, Signature.createBase(signatureGenerator.next(signature)));
    }

    private BuilderMorphism morphism(BuilderObject dom, BuilderObject cod, BaseSignature signature) {
        final var morphism = new BuilderMorphism(signature, nextLabel, dom, cod, nextMin, nextTags);
        morphismsBySignature.put(morphism.signature(), morphism);
        morphismsByLabel.put(morphism.label(), morphism);

        nextLabel = "";
        nextMin = Min.ONE;
        nextTags = Set.of();

        return morphism;
    }

    public BuilderMorphism composite(BuilderMorphism... morphisms) {
        final Signature signature = Signature.concatenate(Stream.of(morphisms).map(m -> m.signature()).toList());
        final String label = Stream.of(morphisms).map(m -> m.label()).collect(Collectors.joining("#"));
        final var min = Stream.of(morphisms).anyMatch(m -> m.min.equals(Min.ZERO)) ? Min.ZERO : Min.ONE;
        final var composite = new BuilderMorphism(
            signature,
            label,
            morphisms[0].dom,
            morphisms[morphisms.length - 1].cod,
            min,
            Set.of()
        );

        morphismsBySignature.put(composite.signature(), composite);
        morphismsByLabel.put(composite.label(), composite);

        return composite;
    }

    // Convenience methods for creating composite signatures.

    public Signature dual(BuilderMorphism morphism) {
        return morphism.signature().dual();
    }

    // This is just disgusting. But Java doesn't offer a better way to do this.
    public Signature concatenate(Object... objects) {
        final List<Signature> signatures = new ArrayList<>();
        for (final Object object : objects) {
            if (object instanceof Signature signature)
                signatures.add(signature);
            else if (object instanceof BuilderMorphism morphism)
                signatures.add(morphism.signature());
            else
                throw new IllegalArgumentException("Only signatures and morphisms are allowed.");
        }

        return Signature.concatenate(signatures);
    }

    // Objects ids - they need to be defined when the morphisms are already here.

    public SchemaBuilder ids(BuilderObject object, BuilderMorphism... morphisms) {
        final var id = new SignatureId(Stream.of(morphisms).map(m -> m.signature()).toArray(Signature[]::new));
        object.ids = new ObjectIds(Set.of(id));
        object.superId = object.ids.generateDefaultSuperId();

        return this;
    }

    private Set<BuilderObject> objectsToSkip = new TreeSet<>();

    public SchemaBuilder skip(BuilderObject... objects) {
        objectsToSkip.addAll(List.of(objects));

        return this;
    }

    private Set<BuilderMorphism> morphismsToSkip = new TreeSet<>();

    public SchemaBuilder skip(BuilderMorphism... morphisms) {
        morphismsToSkip.addAll(List.of(morphisms));

        return this;
    }

    public SchemaCategory build() {
        final var schema = new SchemaCategory(schemaLabel);

        objectsByKey.values().forEach(o -> {
            if (objectsToSkip.contains(o))
                return;

            final var object = new SchemaObject(o.key, o.label, o.ids, o.superId);
            schema.addObject(object);
        });

        morphismsBySignature.values().forEach(m -> {
            if (morphismsToSkip.contains(m) || objectsToSkip.contains(m.dom) || objectsToSkip.contains(m.cod))
                return;

            if (!(m.signature instanceof BaseSignature))
                return;

            final var dom = schema.getObject(m.domKey());
            final var cod = schema.getObject(m.codKey());
            final var morphism = new SchemaMorphism(m.signature, m.label, m.min, m.tags, dom, cod);

            schema.addMorphism(morphism);
        });

        objectsToSkip.clear();
        morphismsToSkip.clear();

        return schema;
    }

}
