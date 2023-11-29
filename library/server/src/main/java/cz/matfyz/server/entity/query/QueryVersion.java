package cz.matfyz.server.entity.query;

import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.querying.QueryUpdateResult.QueryUpdateError;
import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

/**
 * @author jachym.bartik
 */
public class QueryVersion extends Entity {

    public final Id queryId;
    /** For which version of category is this query version valid. */
    public Version version;
    public String content;
    public List<QueryUpdateError> errors;

    private QueryVersion(Id id, Id queryId, Version version, String content, List<QueryUpdateError> errors) {
        super(id);
        this.queryId = queryId;
        this.version = version;
        this.content = content;
        this.errors = errors;
    }

    public static QueryVersion createNew(Id queryId, Version version, String content, List<QueryUpdateError> errors) {
        return new QueryVersion(
            Id.createNewUUID(),
            queryId,
            version,
            content,
            errors
        );
    }

    private static record JsonValue(
        Version version,
        String content,
        List<QueryUpdateError> errors
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);


    public static QueryVersion fromJsonValue(Id id, Id queryId, String jsonValueString) throws JsonProcessingException {
        final JsonValue jsonValue = jsonValueReader.readValue(jsonValueString);

        return new QueryVersion(
            id,
            queryId,
            jsonValue.version,
            jsonValue.content,
            jsonValue.errors
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(
            version,
            content,
            errors
        ));
    }

}
