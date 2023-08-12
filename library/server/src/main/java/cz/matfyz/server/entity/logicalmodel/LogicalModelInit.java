package cz.matfyz.server.entity.logicalmodel;

import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.utils.Utils;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author jachym.bartik
 */
public record LogicalModelInit(
    Id databaseId,
    Id categoryId,
    String label
) {
    private static final List<String> idPropertyNames = List.of("id", "categoryId", "databaseId");

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJsonWithoutProperties(this, idPropertyNames);
    }
}
