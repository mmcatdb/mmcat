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
        final SchemaCategory schema = new SchemaCategory();
        final MetadataCategory metadata = MetadataCategory.createEmpty(schema);

        for (final CategoryMappingsPair pair : pairs) {
            for (final SchemaObjex objex : pair.schema.allObjexes()) {
                final var copy = schema.addObjexCopy(objex);
                metadata.setObjex(copy, pair.metadata.getObjex(copy));
            }

            for (final SchemaMorphism morphism : pair.schema.allMorphisms()) {
                final var copy = schema.addMorphismCopy(morphism);
                metadata.setMorphism(copy, pair.metadata.getMorphism(copy));
            }
        }

        // Collect all mappings & update their schema category.
        final List<Mapping> mappings = pairs.stream().flatMap(pair -> pair.mappings.stream().map(m -> m.withSchemaAndPath(schema, m.accessPath()))).toList();

        return new CategoryMappingsPair(schema, metadata, mappings);
    }

}
