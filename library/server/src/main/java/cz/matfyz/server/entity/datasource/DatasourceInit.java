package cz.matfyz.server.entity.datasource;

import com.fasterxml.jackson.databind.node.ObjectNode;

import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;

public record DatasourceInit(
    String label,
    DatasourceType type,
    ObjectNode settings
) {}
