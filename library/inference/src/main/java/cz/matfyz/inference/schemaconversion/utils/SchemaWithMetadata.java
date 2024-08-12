package cz.matfyz.inference.schemaconversion.utils;

import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;

public record SchemaWithMetadata(
    SchemaCategory schema,
    MetadataCategory metadata
) {}
