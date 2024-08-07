package cz.matfyz.inference.edit;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.ArrayList;
import java.util.List;

public class InferenceEditor {

    private SchemaCategory schemaCategory;
    private List<Mapping> mappings;
    private Mapping finalMapping;
    public final List<InferenceEdit> edits;

    public InferenceEditor(SchemaCategory schemaCategory, List<InferenceEdit> edits) {
        this.schemaCategory = schemaCategory;
        this.edits = edits;
    }

    public InferenceEditor(SchemaCategory schemaCategory, List<Mapping> mappings, List<InferenceEdit> edits) {
        this.schemaCategory = schemaCategory;
        this.mappings = mappings;
        this.edits = edits;
    }

    public SchemaCategory getSchemaCategory() {
        return this.schemaCategory;
    }

    public Mapping getFinalMapping() {
        return this.finalMapping;
    }

    public boolean hasMappings() {
        return this.mappings != null;
    }

    private final List<InferenceEditAlgorithm> algorithms = new ArrayList<>();

    public void applyEdits() {
        applySchemaCategoryEdits();
        if (hasMappings()) {
            applyMappingEdits();
            setFinalMapping();
        }
    }

    private void applySchemaCategoryEdits() {
        for (final var edit : edits) {
            final var algorithm = edit.createAlgorithm();
            algorithms.add(algorithm);
            schemaCategory = algorithm.applySchemaCategoryEdit(schemaCategory);
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
