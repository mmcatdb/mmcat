package cz.matfyz.server.entity;

import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.querying.QueryEvolutionResult.QueryEvolutionError;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class Query extends VersionedEntity {

    public final Id categoryId;
    public String label;
    public String content;
    public List<QueryEvolutionError> errors;

    private Query(Id id, Version version, Version lastValid, Id categoryId, String label, String content, List<QueryEvolutionError> errors) {
        super(id, version, lastValid);
        this.categoryId = categoryId;
        this.label = label;
        this.content = content;
        this.errors = errors;
    }

    public static Query createNew(Version version, Id categoryId, String label, String content) {
        return new Query(
            Id.createNew(),
            version,
            version,
            categoryId,
            label,
            content,
            List.of()
        );
    }

    private record JsonValue(
        String label,
        String content,
        List<QueryEvolutionError> errors
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static Query fromJsonValue(Id id, Version version, Version lastValid, Id categoryId, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        return new Query(
            id,
            version,
            lastValid,
            categoryId,
            json.label,
            json.content,
            json.errors
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(
            label,
            content,
            errors
        ));
    }

}
