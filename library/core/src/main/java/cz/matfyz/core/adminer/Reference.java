package cz.matfyz.core.adminer;

/**
 * The Reference class represents a reference in a database.
 * It encapsulates information about the referencing and referenced kind and property and the ID of datasource.
 */
public class Reference {
    private String referencedDatasourceId;
    private String referencedKindName;
    private String referencedProperty;
    private String referencingDatasourceId;
    private String referencingKindName;
    private String referencingProperty;

    public Reference(String referencedDatasourceId, String referencedKindName, String referencedProperty, String referencingDatasourceId, String referencingKindName, String referencingProperty) {
        this.referencedDatasourceId = referencedDatasourceId;
        this.referencedKindName = referencedKindName;
        this.referencedProperty = referencedProperty;
        this.referencingDatasourceId = referencingDatasourceId;
        this.referencingKindName = referencingKindName;
        this.referencingProperty = referencingProperty;
    }

    public String getReferencedDatasourceId() {
        return referencedDatasourceId;
    }

    public String getReferencedKindName() {
        return referencedKindName;
    }

    public String getReferencedProperty() {
        return referencedProperty;
    }

    public String getReferencingDatasourceId() {
        return referencingDatasourceId;
    }

    public String getReferencingKindName() {
        return referencingKindName;
    }

    public String getReferencingProperty() {
        return referencingProperty;
    }

    @Override
    public String toString() {
        return String.format("""
            {
                "referencedDatasourceId": "%s",
                "referencedKindName": "%s",
                "referencedProperty": "%s",
                "referencingDatasourceId": "%s",
                "referencingKindName": "%s",
                "referencingProperty": "%s"
            }
            """, referencedDatasourceId, referencedKindName, referencedProperty, referencingDatasourceId, referencingKindName, referencingProperty);
    }
}

