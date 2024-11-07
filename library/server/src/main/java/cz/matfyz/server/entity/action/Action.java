package cz.matfyz.server.entity.action;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class Action extends Entity {

    public final Id categoryId;
    public final String label;
    public final ActionPayload payload;

    private Action(Id id, Id categoryId, String label, ActionPayload payload) {
        super(id);
        this.categoryId = categoryId;
        this.label = label;
        this.payload = payload;
    }

    public static Action createNew(Id categoryId, String label, ActionPayload payload) {
        return new Action(
            Id.createNew(),
            categoryId,
            label,
            payload
        );
    }

    private record JsonValue(
        String label,
        ActionPayload payload
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static Action fromJsonValue(Id id, Id categoryId, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        return new Action(
            id,
            categoryId,
            json.label,
            json.payload
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(
            label,
            payload
        ));
    }

}
