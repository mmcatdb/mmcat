package cz.matfyz.core.metadata;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.metadata.MetadataObject.Position;
import cz.matfyz.core.schema.SchemaCategory;

import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;

public class MetadataSerializer {

    public record SerializedMetadata(
        List<SerializedMetadataObject> objects,
        List<SerializedMetadataMorphism> morphisms
    ) implements Serializable {}

    public record SerializedMetadataObject(
        Key key,
        String label,
        Position position
    ) {

        public MetadataObject deserialize() {
            return new MetadataObject(label, position);
        }

    }

    public record SerializedMetadataMorphism(
        Signature signature,
        String label
    ) {

        public MetadataMorphism deserialize() {
            return new MetadataMorphism(label);
        }

    }

    public static SerializedMetadata serialize(MetadataCategory metadata) {
        final List<SerializedMetadataObject> objects = metadata.schema.allObjects().stream()
            .map(object -> {
                final var mo = metadata.getObject(object);
                return new SerializedMetadataObject(
                    object.key(),
                    mo.label,
                    mo.position
                );
            })
            .toList();

        final List<SerializedMetadataMorphism> morphisms = metadata.schema.allMorphisms().stream()
            .map(morphism -> {
                final var mm = metadata.getMorphism(morphism);
                return new SerializedMetadataMorphism(
                    morphism.signature(),
                    mm.label
                );
            })
            .toList();

        return new SerializedMetadata(
            objects,
            morphisms
        );
    }

    public static MetadataCategory deserialize(SerializedMetadata serializedMetadata, SchemaCategory schemaCategory) {
        final var objects = new TreeMap<Key, MetadataObject>();
        serializedMetadata.objects.stream().forEach(so -> objects.put(so.key, so.deserialize()));

        final var morphisms = new TreeMap<Signature, MetadataMorphism>();
        serializedMetadata.morphisms.stream().forEach(sm -> morphisms.put(sm.signature, sm.deserialize()));

        return new MetadataCategory(schemaCategory, objects, morphisms);
    }

}
