package cz.matfyz.inference.schemaconversion.utils;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;

/**
 * A record representing a schema along with its associated metadata.
 *
 * @param schema The {@link SchemaCategory} representing the schema structure.
 * @param metadata The {@link MetadataCategory} representing the metadata associated with the schema.
 */
public record SchemaWithMetadata(
    SchemaCategory schema,
    MetadataCategory metadata
) {}
