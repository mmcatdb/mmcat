package cz.matfyz.server.entity.datasource;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record DatasourceUpdate(
    String label,
    ObjectNode settings
) {

    public boolean hasPassword() {
        return this.settings.has(DatasourceWrapper.PASSWORD_FIELD_NAME);
    }

    public void trySetPasswordFrom(DatasourceWrapper datasource) {
        if (datasource.settings.has(DatasourceWrapper.PASSWORD_FIELD_NAME))
            this.settings.set(DatasourceWrapper.PASSWORD_FIELD_NAME, datasource.settings.get(DatasourceWrapper.PASSWORD_FIELD_NAME));
    }

}
