package cz.matfyz.server.entity.datasource;

import cz.matfyz.core.datasource.Datasource.DatasourceType;

import com.fasterxml.jackson.databind.node.ObjectNode;

public record DatasourceInit(
    String label,
    DatasourceType type,
    ObjectNode settings
) {}
