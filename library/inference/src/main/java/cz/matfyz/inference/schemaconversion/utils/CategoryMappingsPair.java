package cz.matfyz.inference.schemaconversion.utils;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObjex;

import java.util.List;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;

/**
 * A record to hold the final result of schema conversion, consisting of a schema category,
 * associated metadata, and a list of mappings.
 */
public record CategoryMappingsPair(
    SchemaCategory schema,
    MetadataCategory metadata,
    List<Mapping> mappings
) {

    /**
     * Merges multiple {@code CategoryMappingPair} instances into a single instance.
     * The merged instance contains a combined schema, metadata, and mappings from all provided pairs.
     */
    public static CategoryMappingsPair merge(List<CategoryMappingsPair> pairs) {
        final SchemaCategory mergedSchema = new SchemaCategory();
        final MetadataCategory mergedMetadata = MetadataCategory.createEmpty(mergedSchema);

        for (final CategoryMappingsPair pair : pairs) {
            for (final SchemaObjex objex : pair.schema.allObjexes()) {
                mergedSchema.addObjex(objex);
                mergedMetadata.setObjex(objex, pair.metadata.getObjex(objex));
            }

            for (final SchemaMorphism morphism : pair.schema.allMorphisms()) {
                mergedSchema.addMorphism(morphism);
                mergedMetadata.setMorphism(morphism, pair.metadata.getMorphism(morphism));
            }
        }

        final List<Mapping> mappings = pairs.stream().flatMap(pair -> pair.mappings.stream()).toList();

        return new CategoryMappingsPair(mergedSchema, mergedMetadata, mappings);
    }

}
