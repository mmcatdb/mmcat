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

/**
 * The {@code InferenceEditAlgorithm} class is an abstract base class that represents
 * an algorithm for editing a schema category and associated metadata. It provides methods
 * for applying edits to schema categories and mappings, as well as managing the old and
 * new schema and metadata states.
 *
 * <p>Subclasses of this class should implement the {@code innerCategoryEdit} and
 * {@code applyMappingEdit} methods to define specific editing behaviors.
 */
public abstract class InferenceEditAlgorithm {

    /**
     * Applies an edit to the schema category and metadata, creating new schema and metadata
     * instances that reflect the applied changes.
     *
     * @param oldSchema The original schema category to be edited.
     * @param oldMetadata The original metadata associated with the schema.
     * @return A {@code SchemaWithMetadata} object containing the edited schema and metadata.
     */
    public SchemaWithMetadata applyCategoryEdit(SchemaCategory oldSchema, MetadataCategory oldMetadata) {
        this.oldSchema = oldSchema;
        this.newSchema = InferenceEditorUtils.createSchemaCopy(oldSchema);
        this.oldMetadata = oldMetadata;
        this.newMetadata = InferenceEditorUtils.createMetadataCopy(oldMetadata, this.newSchema);

        innerCategoryEdit();

        return new SchemaWithMetadata(newSchema, newMetadata);
    }

    /**
     * Defines the inner logic of the category edit operation. Subclasses should implement this method
     * to specify how the schema and metadata should be modified.
     */
    protected abstract void innerCategoryEdit();

    /**
     * Applies an edit to a list of mappings. Subclasses should implement this method to specify
     * how the mappings should be modified based on the edit.
     *
     * @param mappings The list of mappings to be edited.
     * @return A list of edited mappings.
     */
    public abstract List<Mapping> applyMappingEdit(List<Mapping> mappings);

    /** The original schema category before the edit is applied. */
    protected SchemaCategory oldSchema;

    /** The new schema category after the edit is applied. */
    protected SchemaCategory newSchema;

    /** The original metadata associated with the schema before the edit is applied. */
    protected MetadataCategory oldMetadata;

    /** The new metadata associated with the schema after the edit is applied. */
    protected MetadataCategory newMetadata;

    /** A set of signatures that are marked for deletion as part of the edit. */
    protected Set<Signature> signaturesToDelete = new HashSet<>();

    /** A set of keys that are marked for deletion as part of the edit. */
    protected Set<Key> keysToDelete = new HashSet<>();

}
