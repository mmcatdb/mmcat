package cz.matfyz.server.entity;

import cz.matfyz.server.repository.utils.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class LogicalModel extends Entity {

    /** Immutable. */
    public final Id categoryId;
    /** Immutable. */
    public final Id datasourceId;
    public final String label;

    private LogicalModel(Id id, Id categoryId, Id datasourceId, String label) {
        super(id);
        this.categoryId = categoryId;
        this.datasourceId = datasourceId;
        this.label = label;
    }

    public static LogicalModel createNew(Id categoryId, Id datasourceId, String label) {
        return new LogicalModel(
            Id.createNew(),
            categoryId,
            datasourceId,
            label
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJson(new JsonValue(label));
    }

    private record JsonValue(
        String label
    ) {}

    private static final ObjectReader reader = new ObjectMapper().readerFor(JsonValue.class);

    public static LogicalModel fromJsonValue(Id id, Id categoryId, Id datasourceId, String jsonValue) throws JsonProcessingException {
        final JsonValue parsedValue = reader.readValue(jsonValue);

        return new LogicalModel(id, categoryId, datasourceId, parsedValue.label);
    }

}
