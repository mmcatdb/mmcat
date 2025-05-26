package cz.matfyz.core.schema;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.ObjexIds;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.identifiers.SignatureId;
import cz.matfyz.core.schema.SchemaMorphism.Min;
import cz.matfyz.core.schema.SchemaMorphism.Tag;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class SchemaSerializer {

    public record SerializedSchema(
        List<SerializedObjex> objexes,
        List<SerializedMorphism> morphisms
    ) implements Serializable {}

    public static SerializedSchema serialize(SchemaCategory schema) {
        final List<SerializedObjex> objexes = schema.allObjexes().stream()
            .map(SerializedObjex::serialize)
            .toList();

        final List<SerializedMorphism> morphisms = schema.allMorphisms().stream()
            .map(SerializedMorphism::serialize)
            .toList();

        return new SerializedSchema(
            objexes,
            morphisms
        );
    }

    public static SchemaCategory deserialize(SerializedSchema serializedSchema) {
        final var schema = new SchemaCategory();

        for (final var serializedObjex : serializedSchema.objexes)
            schema.addObjex(serializedObjex.deserialize());

        for (final var serializedMorphism : serializedSchema.morphisms)
            schema.addMorphism(serializedMorphism.deserialize(schema::getObjex));

        return schema;
    }

    public record SerializedObjex(
        Key key,
        ObjexIds ids,
        SignatureId superId
    ) {

        public static SerializedObjex serialize(SchemaObjex objex) {
            return new SerializedObjex(
                objex.key(),
                objex.ids(),
                objex.superId()
            );
        }

        public SchemaObjex deserialize() {
            return new SchemaObjex(
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

        public interface SchemaObjexProvider {
            SchemaObjex getObjex(Key key);
        }

        public SchemaMorphism deserialize(SchemaObjexProvider provider) {
            return new SchemaMorphism(
                signature,
                provider.getObjex(domKey),
                provider.getObjex(codKey),
                min,
                tags
            );
        }

    }

}
