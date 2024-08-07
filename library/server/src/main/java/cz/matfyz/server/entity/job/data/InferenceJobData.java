package cz.matfyz.server.entity.job.data;

import cz.matfyz.inference.edit.InferenceEdit;
import cz.matfyz.server.entity.job.JobData;
import cz.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.matfyz.server.repository.MappingRepository.MappingJsonValue;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public record InferenceJobData(
    InferenceData inference,
    @Nullable List<InferenceEdit> manual,
    @Nullable SchemaCategoryWrapper finalSchema
) implements JobData {

    public static record InferenceData(
        SchemaCategoryWrapper schemaCategory,
        List<MappingJsonValue> mapping
    ) {}

    public InferenceJobData(InferenceData inference) {
        this(inference, new ArrayList<>(), inference.schemaCategory);
    }

}
