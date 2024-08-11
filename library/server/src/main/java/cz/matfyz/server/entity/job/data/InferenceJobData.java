package cz.matfyz.server.entity.job.data;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.Mapping.SerializedMapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadata;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer;
import cz.matfyz.core.schema.SchemaSerializer.SerializedSchema;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.server.entity.job.JobData;

import java.util.List;

public record InferenceJobData(
    List<InferenceEdit> edits,
    SerializedSchema schema,
    SerializedMetadata metadata,
    List<SerializedMapping> mappings
) implements JobData {

    public static InferenceJobData fromSchemaCategory(
        List<InferenceEdit> edits,
        SchemaCategory schema,
        MetadataCategory metadata,
        List<Mapping> mappings
    ) {
        return new InferenceJobData(
            edits,
            SchemaSerializer.serialize(schema),
            MetadataSerializer.serialize(metadata),
            mappings.stream().map(SerializedMapping::fromMapping).toList()
        );
    }

}
