package cz.cuni.matfyz.evolution;

import cz.cuni.matfyz.core.schema.SchemaCategory;

public abstract class SchemaModificationOperationBase implements SchemaModificationOperation {

    private final String beforeVersion;
    private final String afterVersion;
    private final SchemaCategory schemaCategory;

    protected SchemaModificationOperationBase(String beforeVersion, String afterVersion, SchemaCategory schemaCategory) {
        this.beforeVersion = beforeVersion;
        this.afterVersion = afterVersion;
        this.schemaCategory = schemaCategory;
    }

    public String getBeforeVersion() {
        return this.beforeVersion;
    }

    public String getAfterVersion() {
        return this.afterVersion;
    }

}
