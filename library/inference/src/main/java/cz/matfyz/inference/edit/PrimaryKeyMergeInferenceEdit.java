package cz.matfyz.inference.edit;

import cz.matfyz.abstractwrappers.AbstractInferenceEdit;
import cz.matfyz.core.schema.SchemaCategory;

import org.apache.hadoop.shaded.org.apache.commons.lang3.NotImplementedException;

public class PrimaryKeyMergeInferenceEdit extends AbstractInferenceEdit {

    public final String primaryKey;

    public PrimaryKeyMergeInferenceEdit(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Override
    public void applyEdit(SchemaCategory schemaCategory) {
        throw new NotImplementedException("PrimaryKeyMergeInferenceEdit.applyEdit() is not implemented");
    }
}
