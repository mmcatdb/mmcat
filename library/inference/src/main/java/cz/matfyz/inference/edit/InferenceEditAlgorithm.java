package cz.matfyz.inference.edit;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.metadata.MetadataSerializer;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.core.schema.SchemaSerializer;
import cz.matfyz.inference.schemaconversion.utils.SchemaWithMetadata;

import java.util.List;

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
     */
    public SchemaWithMetadata applyCategoryEdit(SchemaCategory oldSchema, MetadataCategory oldMetadata) {
        this.oldSchema = oldSchema;
        newSchema = SchemaSerializer.deserialize(SchemaSerializer.serialize(oldSchema));
        this.oldMetadata = oldMetadata;
        newMetadata = MetadataSerializer.deserialize(MetadataSerializer.serialize(oldMetadata), newSchema);

        innerCategoryEdit();

        return new SchemaWithMetadata(newSchema, newMetadata);
    }

    protected abstract void innerCategoryEdit();

    /**
     * Applies an edit to a list of mappings. Subclasses should implement this method to specify
     * how the mappings should be modified based on the edit.
     */
    public abstract List<Mapping> applyMappingEdit(List<Mapping> mappings);

    protected SchemaCategory oldSchema;
    protected SchemaCategory newSchema;
    protected MetadataCategory oldMetadata;
    protected MetadataCategory newMetadata;

}
