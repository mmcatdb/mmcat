package cz.matfyz.server.entity.logicalmodel;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.utils.Utils;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

public record LogicalModelInit(
    Id datasourceId,
    Id categoryId,
    String label
) {
    private static final List<String> idPropertyNames = List.of("id", "categoryId", "datasourceId");

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJsonWithoutProperties(this, idPropertyNames);
    }
}
