package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjectIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaMorphism.Tag;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class SchemaSerializer {

    public record SerializedSchema(
        List<SerializedObject> objects,
        List<SerializedMorphism> morphisms
    ) implements Serializable {}

    public static SerializedSchema serialize(SchemaCategory schema) {
        final List<SerializedObject> objects = schema.allObjects().stream()
            .map(SerializedObject::serialize)
            .toList();

        final List<SerializedMorphism> morphisms = schema.allMorphisms().stream()
            .map(SerializedMorphism::serialize)
            .toList();

        return new SerializedSchema(
            objects,
            morphisms
        );
    }

    public static SchemaCategory deserialize(SerializedSchema serializedSchema) {
        final var schema = new SchemaCategory();

        for (final var serializedObject : serializedSchema.objects)
            schema.addObject(serializedObject.deserialize());

        for (final var serializedMorphism : serializedSchema.morphisms)
            schema.addMorphism(serializedMorphism.deserialize(schema::getObject));

        return schema;
    }

    public record SerializedObject(
        Key key,
        ObjectIds ids,
        SignatureId superId
    ) {

        public static SerializedObject serialize(SchemaObject object) {
            return new SerializedObject(
                object.key(),
                object.ids(),
                object.superId()
            );
        }

        public SchemaObject deserialize() {
            return new SchemaObject(
                key,
                ids,
                superId
            );
        }

    }

    public record SerializedMorphism(
        Signature signature,
        Key domKey,
        Key codKey,
        Min min,
        Set<Tag> tags
    ) {

        public static SerializedMorphism serialize(SchemaMorphism morphism) {
            return new SerializedMorphism(
                morphism.signature(),
                morphism.dom().key(),
                morphism.cod().key(),
                morphism.min(),
                morphism.tags()
            );
        }

        public interface SchemaObjectProvider {
            SchemaObject getObject(Key key);
        }

        public SchemaMorphism deserialize(SchemaObjectProvider provider) {
            return new SchemaMorphism(
                signature,
                provider.getObject(domKey),
                provider.getObject(codKey),
                min,
                tags
            );
        }

    }

}
