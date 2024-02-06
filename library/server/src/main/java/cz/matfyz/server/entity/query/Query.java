package cz.matfyz.server.entity.query;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * @author jachym.bartik
 */
public class Query extends Entity {

    public final Id categoryId;
    public String label;

    private Query(Id id, Id categoryId, String label) {
        super(id);
        this.categoryId = categoryId;
        this.label = label;
    }

    public static Query createNew(Id categoryId, String label) {
        return new Query(
            Id.createNewUUID(),
            categoryId,
            label
        );
    }
    
    private record JsonValue(
        String label
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static Query fromJsonValue(Id id, Id categoryId, String jsonValueString) throws JsonProcessingException {
        final JsonValue jsonValue = jsonValueReader.readValue(jsonValueString);

        return new Query(
            id,
            categoryId,
            jsonValue.label
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(
            label
        ));
    }

}
