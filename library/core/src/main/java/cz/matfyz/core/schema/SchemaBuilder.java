package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.BaseSignature;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataMorphism;
import cz.matfyz.core.metadata.MetadataObjex;
import cz.matfyz.core.metadata.MetadataObjex.Position;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaMorphism.Tag;
import cz.matfyz.core.schema.SchemaSerializer.SerializedMorphism;
import cz.matfyz.core.schema.SchemaSerializer.SerializedObjex;
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

    // Schema objex

    public static class BuilderObjex implements Comparable<BuilderObjex> {

        private final Key key;
        private final String label;
        // These needs to be updated later, because the morphisms might not be defined yet.
        ObjexIds ids;

        public BuilderObjex(Key key, String label, ObjexIds ids) {
            this.key = key;
            this.label = label;
            this.ids = ids;
        }

        public Key key() {
            return key;
        }

        public ObjexIds ids() {
            return ids;
        }

        public String label() {
            return label;
        }

        private Position position = Position.createDefault();

        public Position position() {
            return position;
        }

        public BuilderObjex position(Position position) {
            this.position = position;
            return this;
        }

        @Override public int compareTo(BuilderObjex other) {
            return key.compareTo(other.key);
        }

    }

    private SequenceGenerator keyGenerator = new SequenceGenerator(1);
    private Map<Key, BuilderObjex> objexesByKey = new TreeMap<>();
    private Map<String, BuilderObjex> objexesByLabel = new TreeMap<>();

    private boolean nextIdsIsGenerated = false;
    public SchemaBuilder generatedIds() {
        this.nextIdsIsGenerated = true;
        return this;
    }

    public BuilderObjex objex(String label) {
        return objex(label, new Key(keyGenerator.next()));
    }

    public BuilderObjex objex(String label, int key) {
        return objex(label, new Key(keyGenerator.next(key)));
    }

    private BuilderObjex objex(String label, Key key) {
        final var ids = nextIdsIsGenerated ? ObjexIds.createGenerated() : ObjexIds.createValue();
        final var objex = new BuilderObjex(key, label, ids);
        objexesByKey.put(objex.key(), objex);
        objexesByLabel.put(objex.label(), objex);

        nextIdsIsGenerated = false;

        return objex;
    }

    // Schema morphism

    public record BuilderMorphism(
        Signature signature,
        String label,
        BuilderObjex dom,
        BuilderObjex cod,
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
    /** @see SchemaMorphism.Min */
    public SchemaBuilder min(Min nextMin) {
        this.nextMin = nextMin;
        return this;
    }

    private Set<Tag> nextTags = Set.of();
    /** @see SchemaMorphism.Tag */
    public SchemaBuilder tags(Tag... nextTags) {
        this.nextTags = Set.of(nextTags);
        return this;
    }

    public BuilderMorphism morphism(BuilderObjex dom, BuilderObjex cod) {
        return morphism(dom, cod, Signature.createBase(signatureGenerator.next()));
    }

    public BuilderMorphism morphism(BuilderObjex dom, BuilderObjex cod, int signature) {
        return morphism(dom, cod, Signature.createBase(signatureGenerator.next(signature)));
    }

    private BuilderMorphism morphism(BuilderObjex dom, BuilderObjex cod, BaseSignature signature) {
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

    // Objexes' ids - they need to be defined when the morphisms are already here.

    public SchemaBuilder ids(BuilderObjex objexes, BuilderMorphism... morphisms) {
        final var id = new SignatureId(Stream.of(morphisms).map(m -> m.signature()).toArray(Signature[]::new));
        objexes.ids = new ObjexIds(Set.of(id));

        return this;
    }

    private Set<BuilderObjex> objexesToSkip = new TreeSet<>();

    public SchemaBuilder skip(BuilderObjex... objexes) {
        objexesToSkip.addAll(List.of(objexes));

        return this;
    }

    private Set<BuilderMorphism> morphismsToSkip = new TreeSet<>();

    public SchemaBuilder skip(BuilderMorphism... morphisms) {
        morphismsToSkip.addAll(List.of(morphisms));

        return this;
    }

    public SchemaCategory build() {
        final var schema = new SchemaCategory();

        objexesByKey.values().forEach(o -> {
            if (objexesToSkip.contains(o))
                return;

            schema.addObjex(new SerializedObjex(o.key, o.ids));
        });

        morphismsBySignature.values().forEach(m -> {
            if (morphismsToSkip.contains(m) || objexesToSkip.contains(m.dom) || objexesToSkip.contains(m.cod))
                return;

            if (!(m.signature instanceof BaseSignature))
                return;

            schema.addMorphism(new SerializedMorphism(m.signature, m.domKey(), m.codKey(), m.min, m.tags));
        });

        objexesToSkip.clear();
        morphismsToSkip.clear();

        return schema;
    }

    public MetadataCategory buildMetadata(SchemaCategory schema) {
        final var metadata = MetadataCategory.createEmpty(schema);

        schema.allObjexes().forEach(objex -> {
            final var builderObjex = objexesByKey.get(objex.key());
            metadata.setObjex(objex, new MetadataObjex(builderObjex.label(), builderObjex.position()));
        });

        schema.allMorphisms().forEach(morphism -> {
            final var builderMorphism = morphismsBySignature.get(morphism.signature());
            metadata.setMorphism(morphism, new MetadataMorphism(builderMorphism.label()));
        });

        return metadata;
    }

}
