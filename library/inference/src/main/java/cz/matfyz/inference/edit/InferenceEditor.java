package cz.matfyz.inference.edit;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

public class InferenceEditor {

    private SchemaCategory schema;
    private MetadataCategory metadata;
    private @Nullable List<Mapping> mappings;
    private Mapping finalMapping;
    public final List<InferenceEdit> edits;

    public InferenceEditor(SchemaCategory schema, MetadataCategory metadata, List<InferenceEdit> edits) {
        this.schema = schema;
        this.metadata = metadata;
        this.edits = edits;
    }

    public InferenceEditor(SchemaCategory schema, MetadataCategory metadata, List<Mapping> mappings, List<InferenceEdit> edits) {
        this.schema = schema;
        this.metadata = metadata;
        this.mappings = mappings;
        this.edits = edits;
    }

    public SchemaCategory getSchemaCategory() {
        return this.schema;
    }

    public Mapping getFinalMapping() {
        return this.finalMapping;
    }

    public boolean hasMappings() {
        return this.mappings != null;
    }

    private final List<InferenceEditAlgorithm> algorithms = new ArrayList<>();

    public void applyEdits() {
        applyCategoryEdits();
        if (hasMappings()) {
            applyMappingEdits();
            setFinalMapping();
        }
    }

    private void applyCategoryEdits() {
        for (final var edit : edits) {
            final var algorithm = edit.createAlgorithm();
            algorithms.add(algorithm);

            final var result = algorithm.applyCategoryEdit(schema, metadata);
            schema = result.schema();
            metadata = result.metadata();
        }
    }

    private void applyMappingEdits() {
        for (final var algorithm : algorithms)
            mappings = algorithm.applyMappingEdit(mappings);
    }

    private void setFinalMapping() throws IllegalArgumentException {
        if (mappings.size() != 1)
            throw new IllegalArgumentException("The mappings after edits should be merged into one.");

        finalMapping = mappings.get(0);
    }

}
