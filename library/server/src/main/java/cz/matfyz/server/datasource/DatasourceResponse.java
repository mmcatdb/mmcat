package cz.matfyz.server.datasource;

import cz.matfyz.core.datasource.Datasource.DatasourceType;
import cz.matfyz.server.utils.entity.Id;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record DatasourceResponse(
    Id id,
    DatasourceType type,
    String label,
    ObjectNode settings
) {

    public static DatasourceResponse fromEntity(DatasourceEntity datasource) {
        return new DatasourceResponse(
            datasource.id(),
            datasource.type,
            datasource.label,
            // Don't forget to sanitize the settings.
            datasource.getSanitizedSettings()
        );
    }

}

