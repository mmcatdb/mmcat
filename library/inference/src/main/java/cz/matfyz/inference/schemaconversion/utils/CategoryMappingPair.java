package cz.matfyz.inference.schemaconversion.utils;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.ArrayList;
import java.util.List;

import cz.matfyz.core.mapping.Mapping;

/**
 * Record to hold the final result of SchemaConversion - SchemaCategory and Mapping
 */
public record CategoryMappingPair(
    SchemaCategory schemaCategory,
    Mapping mapping
) {
    public static SchemaCategory mergeCategory(List<CategoryMappingPair> pairs, String categoryLabel) {
        SchemaCategory mergedSchemaCategory = new SchemaCategory(categoryLabel);

        for (CategoryMappingPair pair : pairs) {
            for (SchemaObject object : pair.schemaCategory.allObjects()) {
                mergedSchemaCategory.addObject(object);
            }
            for (SchemaMorphism morphism : pair.schemaCategory.allMorphisms()) {
                mergedSchemaCategory.addMorphism(morphism);
            }
        }
        return mergedSchemaCategory;
    }

    public static List<Mapping> getMappings(List<CategoryMappingPair> pairs) {
        List mappings = new ArrayList<>();
        for (CategoryMappingPair pair : pairs) {
            mappings.add(pair.mapping());
        }
        return mappings;
    }

}
