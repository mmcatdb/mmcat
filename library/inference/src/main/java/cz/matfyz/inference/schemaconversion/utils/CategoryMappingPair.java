package cz.matfyz.inference.schemaconversion.utils;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.List;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;

/**
 * A record to hold the final result of schema conversion, consisting of a schema category,
 * associated metadata, and a list of mappings.
 */
public record CategoryMappingPair(
    SchemaCategory schema,
    MetadataCategory metadata,
    List<Mapping> mappings
) {

    /**
     * Merges multiple {@code CategoryMappingPair} instances into a single instance.
     * The merged instance contains a combined schema, metadata, and mappings from all provided pairs.
     */
    public static CategoryMappingPair merge(List<CategoryMappingPair> pairs) {
        final SchemaCategory mergedSchema = new SchemaCategory();
        final MetadataCategory mergedMetadata = MetadataCategory.createEmpty(mergedSchema);

        for (final CategoryMappingPair pair : pairs) {
            for (final SchemaObject object : pair.schema.allObjects()) {
                mergedSchema.addObject(object);
                mergedMetadata.setObject(object, pair.metadata.getObject(object));
            }

            for (final SchemaMorphism morphism : pair.schema.allMorphisms()) {
                mergedSchema.addMorphism(morphism);
                mergedMetadata.setMorphism(morphism, pair.metadata.getMorphism(morphism));
            }
        }

        final List<Mapping> mappings = pairs.stream().flatMap(pair -> pair.mappings.stream()).toList();

        return new CategoryMappingPair(mergedSchema, mergedMetadata, mappings);
    }

}
