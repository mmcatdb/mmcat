package cz.matfyz.inference.schemaconversion.utils;

import cz.matfyz.core.schema.SchemaCategory;

import cz.matfyz.core.mapping.Mapping;

/**
 * Record to hold the final result of SchemaConversion - SchemaCategory and Mapping
 */
public record CategoryMappingPair(SchemaCategory schemaCat, Mapping mapping) {

    public CategoryMappingPair(SchemaCategory schemaCat, Mapping mapping) {
        this.schemaCat = schemaCat;
        this.mapping = mapping;
    }
}
