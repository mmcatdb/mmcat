package cz.matfyz.inference.schemaconversion.utils;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaMorphism;
import cz.matfyz.core.schema.SchemaObject;

import java.util.List;

import cz.matfyz.core.mapping.Mapping;

/**
 * Record to hold the final result of SchemaConversion - SchemaCategory and Mapping
 */
public record CategoryMappingPair(
    SchemaCategory schemaCategory,
    Mapping mapping
) {
    public static CategoryMappingPair merge(List<CategoryMappingPair> pairs, String categoryLabel) {
        SchemaCategory mergedSchemaCategory = new SchemaCategory(categoryLabel);
        for (CategoryMappingPair pair : pairs) {
            for (SchemaObject object : pair.schemaCategory.allObjects()) {
                mergedSchemaCategory.addObject(object);
            }
            for (SchemaMorphism morphism : pair.schemaCategory.allMorphisms()) {
                mergedSchemaCategory.addMorphism(morphism);
            }
        }

        return new CategoryMappingPair(mergedSchemaCategory, null);
    }

}
