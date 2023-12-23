package cz.matfyz.core.schema;

import cz.matfyz.core.category.Morphism.Min;
import cz.matfyz.core.category.Morphism.Tag;
import cz.matfyz.core.utils.SequenceGenerator;
import cz.matfyz.core.category.BaseSignature;
import cz.matfyz.core.category.Signature;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleBuilder {

    private String schemaLabel;

    public SimpleBuilder(String schemaLabel) {
        this.schemaLabel = schemaLabel;
    }

    public static class Object {
        private final Key key;
        private final String label;

        public Object(Key key, String label) {
            this.key = key;
            this.label = label;
        }

        public Key key() {
            return key;
        }

        public String label() {
            return label;
        }

        ObjectIds ids = ObjectIds.createValue();
        SignatureId superId = ids.generateDefaultSuperId();

        public ObjectIds ids() {
            return ids;
        }

        public SignatureId superId() {
            return superId;
        }
    }

    private SequenceGenerator keyGenerator = new SequenceGenerator(1);
    private Map<Key, Object> objectsByKey = new TreeMap<>();
    private Map<String, Object> objectsByLabel = new TreeMap<>();

    public Object object(String label) {
        return object(label, new Key(keyGenerator.next()));
    }

    public Object object(String label, int key) {
        return object(label, new Key(keyGenerator.next(key)));
    }

    private Object object(String label, Key key) {
        final var object = new Object(key, label);
        objectsByKey.put(object.key(), object);
        objectsByLabel.put(object.label(), object);

        return object;
    }

    public static record Morphism(
        Signature signature,
        String label,
        Object dom,
        Object cod,
        Min min,
        Set<Tag> tags
    ) {
        public Key domKey() {
            return dom.key();
        }
        
        public Key codKey() {
            return cod.key();
        }
    }
        
    private SequenceGenerator signatureGenerator;
    private Map<Signature, Morphism> morphismsBySignature = new TreeMap<>();
    private Map<String, Morphism> morphismsByLabel = new TreeMap<>();
    
    private Min nextMin = Min.ONE;
    public SimpleBuilder min(Min nextMin) {
        this.nextMin = nextMin;
        return this;
    }

    private Set<Tag> nextTags = Set.of();
    public SimpleBuilder tags(Tag... nextTags) {
        this.nextTags = Set.of(nextTags);
        return this;
    }

    public Morphism morphism(String label, Object dom, Object cod) {
        return morphism(label, dom, cod, Signature.createBase(signatureGenerator.next()));
    }

    public Morphism morphism(String label, Object dom, Object cod, int signature) {
        return morphism(label, dom, cod, Signature.createBase(signatureGenerator.next(signature)));
    }

    private Morphism morphism(String label, Object dom, Object cod, Signature signature) {
        final var morphism = new Morphism(signature, label, dom, cod, nextMin, nextTags);
        morphismsBySignature.put(morphism.signature(), morphism);
        morphismsByLabel.put(morphism.label(), morphism);

        this.nextMin = Min.ONE;
        this.nextTags = Set.of();
        
        return morphism;
    }

    public Morphism composite(Morphism... morphisms) {
        final Signature signature = Signature.concatenate(Stream.of(morphisms).map(m -> m.signature()).toList());
        final String label = Stream.of(morphisms).map(m -> m.label()).collect(Collectors.joining("#"));
        final var min = Stream.of(morphisms).anyMatch(m -> m.min.equals(Min.ZERO)) ? Min.ZERO : Min.ONE;
        final var composite = new Morphism(
            signature,
            label,
            morphisms[0].dom,
            morphisms[morphisms.length - 1].cod,
            min,
            Set.of()
        );

        this.morphismsBySignature.put(composite.signature(), composite);
        this.morphismsByLabel.put(composite.label(), composite);

        return composite;
    }

    public SimpleBuilder ids(Object object, Morphism... morphisms) {
        final var id = new SignatureId(Stream.of(morphisms).map(m -> m.signature()).toArray(Signature[]::new));
        object.ids = new ObjectIds(Set.of(id));
        object.superId = object.ids.generateDefaultSuperId();

        return this;
    }

    public SchemaCategory build() {
        final var schema = new SchemaCategory(schemaLabel);

        objectsByKey.values().forEach(o -> {
            final var object = new SchemaObject(o.key, o.label, o.ids, o.superId, null, null);
            schema.addObject(object);
        });

        morphismsBySignature.values().forEach(m -> {
            if (!(m.signature instanceof BaseSignature))
                return;

            final var dom = schema.getObject(m.domKey());
            final var cod = schema.getObject(m.codKey());
            final var morphism = new SchemaMorphism.Builder()
                .label(m.label)
                .tags(m.tags)
                .fromArguments(m.signature, dom, cod, m.min);

            schema.addMorphism(morphism);
        });

        return schema;
    }

}
