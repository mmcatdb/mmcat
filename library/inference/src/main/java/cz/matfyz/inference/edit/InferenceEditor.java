package cz.matfyz.inference.edit;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.List;

public class InferenceEditor {

    private SchemaCategory schemaCategory;
    private List<Mapping> mappings;
    private Mapping finalMapping;
    public final List<AbstractInferenceEdit> edits;

    public InferenceEditor(SchemaCategory schemaCategory, List<AbstractInferenceEdit> edits) {
        this.schemaCategory = schemaCategory;
        this.edits = edits;
    }

    public InferenceEditor(SchemaCategory schemaCategory, List<Mapping> mappings, List<AbstractInferenceEdit> edits) {
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

    public void applyEdits() {
        applySchemaCategoryEdits();
        if (hasMappings()) {
            applyMappingEdits();
            setFinalMapping();
        }
    }

    private void applySchemaCategoryEdits() {
        for (AbstractInferenceEdit edit : edits) {
            schemaCategory = edit.applySchemaCategoryEdit(schemaCategory);
        }
    }

    private void applyMappingEdits() {
        for (AbstractInferenceEdit edit : edits) {
            mappings = edit.applyMappingEdit(mappings);
        }
    }

    private void setFinalMapping() throws IllegalArgumentException {
        if (mappings.size() == 1) {
            finalMapping = mappings.get(0);
        } else {
            throw new IllegalArgumentException("The mappings after edits should be merged into one.");
        }
    }

}
