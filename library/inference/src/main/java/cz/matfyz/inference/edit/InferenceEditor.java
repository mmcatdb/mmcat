package cz.matfyz.inference.edit;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.List;

public class InferenceEditor {

    private SchemaCategory schemaCategory;
    private List<Mapping> mappings;
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

    public List<Mapping> getMappings() {
        return this.mappings;
    }

    public boolean hasMappings() {
        return this.mappings != null;
    }

    public void applyEdits() {
        applySchemaCategoryEdits();
        if (hasMappings()) {
            applyMappingEdits();
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

}
