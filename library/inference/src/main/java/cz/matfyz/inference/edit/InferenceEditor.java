package cz.matfyz.inference.edit;

import cz.matfyz.core.mapping.Mapping;
import cz.matfyz.core.metadata.MetadataCategory;
import cz.matfyz.core.schema.SchemaCategory;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The {@code InferenceEditor} class manages the application of inference edits
 * to a schema and its associated metadata. It supports applying a series of edits
 * to both the schema category and mappings, based on the provided edit algorithms.
 */
public class InferenceEditor {

    private SchemaCategory schema;
    private MetadataCategory metadata;
    private @Nullable List<Mapping> mappings;
    public final List<InferenceEdit> edits;
    private List<InferenceEdit> activeEdits;

    /**
     * Constructs an {@code InferenceEditor} with the specified schema, metadata, and edits.
     * This constructor is used when there are no initial mappings provided.
     *
     * @param schema The schema category to edit.
     * @param metadata The metadata associated with the schema.
     * @param edits The list of edits to apply.
     */
    public InferenceEditor(SchemaCategory schema, MetadataCategory metadata, List<InferenceEdit> edits) {
        this.schema = schema;
        this.metadata = metadata;
        this.edits = edits;
        filterActiveEdits();
    }

    /**
     * Constructs an {@code InferenceEditor} with the specified schema, metadata, mappings, and edits.
     * This constructor is used when there are initial mappings provided.
     *
     * @param schema The schema category to edit.
     * @param metadata The metadata associated with the schema.
     * @param mappings The initial list of mappings to edit.
     * @param edits The list of edits to apply.
     */
    public InferenceEditor(SchemaCategory schema, MetadataCategory metadata, List<Mapping> mappings, List<InferenceEdit> edits) {
        this.schema = schema;
        this.metadata = metadata;
        this.mappings = mappings;
        this.edits = edits;
        filterActiveEdits();
    }

    /**
     * Filters the list of edits to identify those that are active.
     * Active edits are those marked with {@code isActive} set to {@code true}.
     */
    private void filterActiveEdits() {
        List<InferenceEdit> filteredEdits = new ArrayList<>();
        for (InferenceEdit edit : edits)
            if (edit.isActive())
                filteredEdits.add(edit);

        this.activeEdits = filteredEdits;
    }

    public SchemaCategory getSchemaCategory() {
        return this.schema;
    }

    public List<Mapping> getMappings() {
        return this.mappings;
    }

    public MetadataCategory getMetadata() {
        return this.metadata;
    }

    public boolean hasMappings() {
        return this.mappings != null;
    }

    /** A list of algorithms corresponding to the active edits. */
    private final List<InferenceEditAlgorithm> algorithms = new ArrayList<>();

    /**
     * Applies all active edits to the schema and mappings. This includes both category
     * edits and mapping edits, depending on whether mappings are available.
     */
    public void applyEdits() {
        applyCategoryEdits();
        if (hasMappings())
            applyMappingEdits();
    }

    /**
     * Applies all active category edits to the schema and metadata.
     */
    private void applyCategoryEdits() {
        for (final var edit : activeEdits) {
            final var algorithm = edit.createAlgorithm();
            algorithms.add(algorithm);

            final var result = algorithm.applyCategoryEdit(schema, metadata);
            schema = result.schema();
            metadata = result.metadata();
        }
    }

    /**
     * Applies all active mapping edits to the list of mappings.
     * This method is only called if mappings are available.
     */
    private void applyMappingEdits() {
        for (final var algorithm : algorithms)
            mappings = algorithm.applyMappingEdit(mappings);
    }

}
