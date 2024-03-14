package cz.matfyz.tests.example.common;

import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaMorphism.Tag;
import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

public class SchemaBuilder {

    private final List<ObjectDefinition> objects = new ArrayList<>();
    private final List<MorphismDefinition> morphisms = new ArrayList<>();

    private record ObjectDefinition(Key key, String name, ObjectIds ids) {}

    public void object(Key key, String name, ObjectIds ids) {
        this.objects.add(new ObjectDefinition(key, name, ids));
    }

    private record MorphismDefinition(Signature signature, Key dom, Key cod, Min min, @Nullable Tag tag) {}

    public void morphism(Signature signature, Key dom, Key cod, Min min, Tag tag) {
        this.morphisms.add(new MorphismDefinition(signature, dom, cod, min, tag));
    }

    public void morphism(Signature signature, Key dom, Key cod, Min min) {
        this.morphism(signature, dom, cod, min, null);
    }

    public SchemaCategory build(String label) {
        final var schema = new SchemaCategory(label);

        this.objects.forEach(o -> {
            final var object = new SchemaObject(o.key, o.name, o.ids, o.ids.generateDefaultSuperId());
            schema.addObject(object);
        });

        this.morphisms.forEach(m -> {
            final Set<Tag> tags = m.tag == null ? Set.of() : Set.of(m.tag);
            final var dom = schema.getObject(m.dom);
            final var cod = schema.getObject(m.cod);
            // TODO label
            final var morphism = new SchemaMorphism(m.signature, "", m.min, tags, dom, cod);
            schema.addMorphism(morphism);
        });

        return schema;
    }

}
