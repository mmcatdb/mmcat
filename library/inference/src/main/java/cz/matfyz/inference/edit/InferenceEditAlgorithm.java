package cz.matfyz.inference.edit;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.identifiers.Signature;
import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.inference.schemaconversion.utils.SchemaWithMetadata;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class InferenceEditAlgorithm {

    public SchemaWithMetadata applyCategoryEdit(SchemaCategory oldSchema, MetadataCategory oldMetadata) {
        this.oldSchema = oldSchema;
        this.newSchema = InferenceEditorUtils.createSchemaCopy(oldSchema);
        this.oldMetadata = oldMetadata;
        this.newMetadata = InferenceEditorUtils.createMetadataCopy(oldMetadata, this.newSchema);

        innerCategoryEdit();

        return new SchemaWithMetadata(newSchema, newMetadata);
    }

    protected abstract void innerCategoryEdit();

    public abstract List<Mapping> applyMappingEdit(List<Mapping> mappings);

    protected SchemaCategory oldSchema;
    protected SchemaCategory newSchema;

    protected MetadataCategory oldMetadata;
    protected MetadataCategory newMetadata;

    protected Set<Signature> signaturesToDelete = new HashSet<>();
    protected Set<Key> keysToDelete = new HashSet<>();

}

