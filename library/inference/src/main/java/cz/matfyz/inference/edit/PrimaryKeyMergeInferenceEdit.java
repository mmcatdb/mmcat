package cz.matfyz.inference.edit;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.List;

import org.apache.hadoop.shaded.org.apache.commons.lang3.NotImplementedException;

public class PrimaryKeyMergeInferenceEdit extends AbstractInferenceEdit {

    public final String primaryKey;

    public PrimaryKeyMergeInferenceEdit(String primaryKey) {
        this.primaryKey = primaryKey;
    }

    @Override
    public SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applySchemaCategoryEdit'");
    }

    @Override
    public List<Mapping> applyMappingEdit(List<Mapping> mappings, SchemaCategory schemaCategory) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'applyMappingEdit'");
    }
}
