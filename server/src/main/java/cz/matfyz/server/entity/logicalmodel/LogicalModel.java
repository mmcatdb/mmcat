package cz.matfyz.server.entity.logicalmodel;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;
import cz.matfyz.server.repository.utils.Utils;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

/**
 * @author jachym.bartik
 */
public class LogicalModel extends Entity {

    public final Id categoryId;
    public final Id databaseId;
    public final String label;

    public LogicalModel(Id id, Id categoryId, Id databaseId, String label) {
        super(id);
        this.categoryId = categoryId;
        this.databaseId = databaseId;
        this.label = label;
    }

    public LogicalModelInfo toInfo() {
        return new LogicalModelInfo(id, label);
    }

    private static final List<String> idPropertyNames = List.of("id", "categoryId", "databaseId");

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJsonWithoutProperties(this, idPropertyNames);
    }

    private record JsonValue(
        String label
    ) {}

    private static final ObjectReader reader = new ObjectMapper().readerFor(JsonValue.class);

    public static LogicalModel fromJsonValue(Id id, Id categoryId, Id databaseId, String jsonValue) throws JsonProcessingException {
        final JsonValue parsedValue = reader.readValue(jsonValue);

        return new LogicalModel(id, categoryId, databaseId, parsedValue.label);
    }

}
