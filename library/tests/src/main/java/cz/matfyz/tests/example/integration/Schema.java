package cz.matfyz.tests.example.integration;

import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.tests.example.common.SchemaBuilder;

public class Schema {

    public static final String schemaLabel = "integrationSchema";

    // TODO

    // Keys


    // Signatures

    
    public Schema() {

    }

    public SchemaCategory build() {
        return builder.build(schemaLabel);
    }

    /**
     * Create new full schema category.
     */
    public static SchemaCategory newSchemaCategory() {
        return new Schema()
            .build();
    }

    private final SchemaBuilder builder = new SchemaBuilder();

    
}
