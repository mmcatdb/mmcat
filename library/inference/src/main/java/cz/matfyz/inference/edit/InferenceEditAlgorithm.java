package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class InferenceEditAlgorithm {

    protected SchemaCategory oldSchemaCategory;
    protected SchemaCategory newSchemaCategory;

    protected Set<Signature> signaturesToDelete = new HashSet<>();
    protected Set<Key> keysToDelete = new HashSet<>();

    protected void setSchemaCategories(SchemaCategory schemaCategory) {
        this.oldSchemaCategory = schemaCategory;
        this.newSchemaCategory = InferenceEditorUtils.createSchemaCategoryCopy(schemaCategory);
    }

    public abstract SchemaCategory applySchemaCategoryEdit(SchemaCategory schemaCategory);
    public abstract List<Mapping> applyMappingEdit(List<Mapping> mappings);

}

