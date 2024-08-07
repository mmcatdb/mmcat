package cz.matfyz.server.entity.job.data;

import cz.matfyz.core.mapping.Mapping.SerializedMapping;
import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.server.entity.job.JobData;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper.SerializedSchemaCategory;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public record InferenceJobData(
    InferenceData inference,
    @Nullable List<InferenceEdit> manual,
    @Nullable SerializedSchemaCategory finalSchema
) implements JobData {

    public static record InferenceData(
        SerializedSchemaCategory schemaCategory,
        List<SerializedMapping> mappings
    ) {}

    public InferenceJobData(InferenceData inference) {
        this(inference, new ArrayList<>(), inference.schemaCategory);
    }

}
