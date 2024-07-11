package cz.matfyz.inference.edit;

import cz.matfyz.core.schema.SchemaCategory;

import java.util.List;

public class SchemaCategoryInferenceEditor {

    private SchemaCategory schemaCategory;
    public final List<AbstractInferenceEdit> edits;

    public SchemaCategoryInferenceEditor(SchemaCategory schemaCategory, List<AbstractInferenceEdit> edits) {
        this.schemaCategory = schemaCategory;
        this.edits = edits;
    }

    public SchemaCategory getSchemaCategory() {
        return this.schemaCategory;
    }

    public void applyEdits() {
        for (AbstractInferenceEdit edit : edits) {
            schemaCategory = edit.applyEdit(schemaCategory);
        }
    }

}
