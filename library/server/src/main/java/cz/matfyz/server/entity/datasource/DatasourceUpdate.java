package cz.matfyz.server.entity.datasource;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record DatasourceUpdate(
    String label,
    ObjectNode settings
) {

    public boolean hasPassword() {
        return this.settings.has(DatasourceEntity.PASSWORD_FIELD_NAME);
    }

    public void trySetPasswordFrom(DatasourceEntity datasource) {
        if (datasource.settings.has(DatasourceEntity.PASSWORD_FIELD_NAME))
            this.settings.set(DatasourceEntity.PASSWORD_FIELD_NAME, datasource.settings.get(DatasourceEntity.PASSWORD_FIELD_NAME));
    }

}
