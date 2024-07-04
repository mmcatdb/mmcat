package cz.matfyz.inference.edit;

import cz.matfyz.abstractwrappers.AbstractInferenceEdit;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.ArrayList;
import java.util.List;

public class SchemaCategoryInferenceEditor {

    private SchemaCategory schemaCategory;
    private List<AbstractInferenceEdit> edits = new ArrayList<>();

    public SchemaCategoryInferenceEditor(SchemaCategory schemaCategory, List<AbstractInferenceEdit> edits) {
        this.schemaCategory = schemaCategory;
        this.edits = edits;
    }

    public void applyEdits() {
        for (AbstractInferenceEdit edit : edits) {
            edit.applyEdit(schemaCategory);

        }
    }
}
