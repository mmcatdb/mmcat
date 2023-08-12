package cz.matfyz.tests.schema;

import cz.matfyz.core.category.Morphism.Min;
import cz.matfyz.core.category.Morphism.Tag;
import cz.matfyz.core.category.Signature;
import cz.matfyz.core.schema.Key;
import cz.matfyz.core.schema.ObjectIds;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SchemaBuilder {
    
    private final List<ObjectDefinition> objects = new ArrayList<>();
    private final List<MorphismDefinition> morphisms = new ArrayList<>();

    private static record ObjectDefinition(Key key, String name, ObjectIds ids) {}

    public void object(Key key, String name, ObjectIds ids) {
        this.objects.add(new ObjectDefinition(key, name, ids));
    }

    private static record MorphismDefinition(Signature signature, Key dom, Key cod, Min min, Tag tag) {}


    public void morphism(Signature signature, Key dom, Key cod, Min min, Tag tag) {
        this.morphisms.add(new MorphismDefinition(signature, dom, cod, min, tag));
    }

    public void morphism(Signature signature, Key dom, Key cod, Min min) {
        this.morphism(signature, dom, cod, min, null);
    }

    public SchemaCategory build(String label) {
        final var schema = new SchemaCategory(label);

        this.objects.forEach(o -> {
            final var object = new SchemaObject(o.key, o.name, o.ids.generateDefaultSuperId(), o.ids, null, null);
            schema.addObject(object);
        });

        this.morphisms.forEach(m -> {
            final var builder = new SchemaMorphism.Builder();
            if (m.tag != null)
                builder.tags(Set.of(m.tag));

            final var dom = schema.getObject(m.dom);
            final var cod = schema.getObject(m.cod);
            final var morphism = builder.fromArguments(m.signature, dom, cod, m.min);
            schema.addMorphism(morphism);
        });

        return schema;
    }

}
