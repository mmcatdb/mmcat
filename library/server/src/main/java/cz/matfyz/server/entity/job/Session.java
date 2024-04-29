package cz.matfyz.server.entity.job;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * This class is something like a prototype of users' sessions. It is used mainly for preserving the results of transformation jobs.
 */
public class Session extends Entity {

    public final Id categoryId;
    public final Date createdAt;

    private Session(Id id, Id categoryId, Date createdAt) {
        super(id);
        this.categoryId = categoryId;
        this.createdAt = createdAt;
    }

    public static Session createNew(Id categoryId) {
        return new Session(
            Id.createNewUUID(),
            categoryId,
            new Date()
        );
    }

    private record JsonValue(
        Date createdAt
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static Session fromJsonValue(Id id, Id categoryId, String jsonValueString) throws JsonProcessingException {
        final JsonValue jsonValue = jsonValueReader.readValue(jsonValueString);

        return new Session(
            id,
            categoryId,
            jsonValue.createdAt
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(
            createdAt
        ));
    }

}
