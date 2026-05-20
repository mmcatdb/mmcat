package cz.matfyz.server.job;

import cz.matfyz.server.utils.entity.Entity;
import cz.matfyz.server.utils.entity.Id;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * Predefined user actions that can repeatedly spawn runs.
 */
public class Action extends Entity {

    public final Id categoryId;
    public final String label;
    // TODO Enable dependencies between jobs (now it's just a list of jobs, so they have to be processed in a sequential order).
    public final List<JobPayload> payloads;

    private Action(Id id, Id categoryId, String label, List<JobPayload> payload) {
        super(id);
        this.categoryId = categoryId;
        this.label = label;
        this.payloads = payload;
    }

    public static Action createNew(Id categoryId, String label, List<JobPayload> payload) {
        return new Action(
            Id.createNew(),
            categoryId,
            label,
            payload
        );
    }

    private record JsonValue(
        String label,
        List<JobPayload> payloads
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static Action fromJsonValue(Id id, Id categoryId, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        return new Action(
            id,
            categoryId,
            json.label,
            json.payloads
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(
            label,
            payloads
        ));
    }

}
