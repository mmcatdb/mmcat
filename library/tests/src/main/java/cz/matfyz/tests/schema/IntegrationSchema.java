package cz.matfyz.tests.schema;

import cz.matfyz.core.schema.SchemaCategory;

public class IntegrationSchema {

    public static final String schemaLabel = "integrationSchema";

    // TODO

    // Keys


    // Signatures

    
    public IntegrationSchema() {

    }

    public SchemaCategory build() {
        return builder.build(schemaLabel);
    }

    /**
     * Create new full schema category.
     */
    public static SchemaCategory newSchemaCategory() {
        return new IntegrationSchema()
            .build();
    }

    private final SchemaBuilder builder = new SchemaBuilder();

    
}
