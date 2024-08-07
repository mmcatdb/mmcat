package cz.matfyz.inference.edit;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.ArrayList;
import java.util.List;

public class InferenceEditor {

    private SchemaCategory schemaCategory;
    private List<Mapping> mappings;
    public final List<AbstractInferenceEdit> edits;

    public List<AbstractInferenceEdit> activeEdits;

    public InferenceEditor(SchemaCategory schemaCategory, List<AbstractInferenceEdit> edits) {
        this.schemaCategory = schemaCategory;
        this.edits = edits;
        filterActiveEdits();
    }

    public InferenceEditor(SchemaCategory schemaCategory, List<Mapping> mappings, List<AbstractInferenceEdit> edits) {
        this.schemaCategory = schemaCategory;
        this.mappings = mappings;
        this.edits = edits;
        filterActiveEdits();
    }

    private void filterActiveEdits() {
        List<AbstractInferenceEdit> filteredEdits = new ArrayList<>();
        for (AbstractInferenceEdit edit : edits) {
            if (edit.isActive) {
                filteredEdits.add(edit);
            }
        }
        this.activeEdits = filteredEdits;
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
        for (AbstractInferenceEdit edit : activeEdits) {
            System.out.println("edit id: " + edit.id);
            System.out.println("edit is active: " + edit.isActive);
            schemaCategory = edit.applySchemaCategoryEdit(schemaCategory);
        }
    }

    private void applyMappingEdits() {
        for (AbstractInferenceEdit edit : activeEdits) {
            mappings = edit.applyMappingEdit(mappings);
        }
    }
}
