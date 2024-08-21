package cz.matfyz.server.entity.job.data;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.mapping.Mapping.SerializedMapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer;
import cz.matfyz.core.metadata.MetadataSerializer.SerializedMetadata;
import cz.matfyz.core.rsd.Candidates;
import cz.matfyz.core.rsd.CandidatesSerializer;
import cz.matfyz.core.rsd.CandidatesSerializer.SerializedCandidates;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer;
import cz.matfyz.core.schema.SchemaSerializer.SerializedSchema;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.inference.edit.InferenceEditSerializer;
import cz.matfyz.inference.edit.InferenceEditSerializer.SerializedInferenceEdit;
import cz.matfyz.inference.schemaconversion.utils.LayoutType;
import cz.matfyz.server.entity.job.JobData;

import java.util.List;

public record InferenceJobData(
    List<SerializedInferenceEdit> edits,
    SerializedSchema inferenceSchema,
    SerializedSchema finalSchema,
    SerializedMetadata inferenceMetadata,
    SerializedMetadata finalMetadata,
    LayoutType layoutType,
    SerializedCandidates candidates,
    List<SerializedMapping> mappings
) implements JobData {

    public static InferenceJobData fromSchemaCategory(
        List<InferenceEdit> edits,
        SchemaCategory inferenceSchema,
        SchemaCategory finalSchema,
        MetadataCategory inferenceMetadata,
        MetadataCategory finalMetadata,
        LayoutType layoutType,
        Candidates candidates,
        List<Mapping> mappings
    ) {
        return new InferenceJobData(
            edits.stream().map(InferenceEditSerializer::serialize).toList(),
            SchemaSerializer.serialize(inferenceSchema),
            SchemaSerializer.serialize(finalSchema),
            MetadataSerializer.serialize(inferenceMetadata),
            MetadataSerializer.serialize(finalMetadata),
            layoutType,
            CandidatesSerializer.serialize(candidates),
            mappings.stream().map(SerializedMapping::fromMapping).toList()
        );
    }

}
