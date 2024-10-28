package cz.matfyz.inference.schemaconversion.utils;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;

/**
 * A record representing a schema along with its associated metadata.
 */
public record SchemaWithMetadata(
    SchemaCategory schema,
    MetadataCategory metadata
) {}
