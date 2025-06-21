package cz.matfyz.core.metadata;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.metadata.MetadataObjex.Position;
import cz.matfyz.core.schema.SchemaCategory;

import java.io.Serializable;
import java.util.List;
import java.util.TreeMap;

public class MetadataSerializer {

    public record SerializedMetadata(
        List<SerializedMetadataObjex> objexes,
        List<SerializedMetadataMorphism> morphisms
    ) implements Serializable {}

    public record SerializedMetadataObjex(
        Key key,
        String label,
        Position position
    ) {

        public MetadataObjex deserialize() {
            return new MetadataObjex(label, position);
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
        final List<SerializedMetadataObjex> objexes = metadata.schema.allObjexes().stream()
            .map(objex -> {
                final var mo = metadata.getObjex(objex);
                return new SerializedMetadataObjex(
                    objex.key(),
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
            objexes,
            morphisms
        );
    }

    public static MetadataCategory deserialize(SerializedMetadata serializedMetadata, SchemaCategory schemaCategory) {
        final var objexes = new TreeMap<Key, MetadataObjex>();
        serializedMetadata.objexes.stream().forEach(so -> objexes.put(so.key, so.deserialize()));

        final var morphisms = new TreeMap<Signature, MetadataMorphism>();
        serializedMetadata.morphisms.stream().forEach(sm -> morphisms.put(sm.signature, sm.deserialize()));

        return new MetadataCategory(schemaCategory, objexes, morphisms);
    }

}
