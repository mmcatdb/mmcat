package cz.matfyz.server.entity.evolution;

import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.querying.QueryEvolutionResult.QueryEvolutionError;
import cz.matfyz.server.entity.Id;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public class QueryEvolution extends Evolution {

    public final Id queryId;
    public final String newContent;
    public final String oldContent;
    // FIXME
    public List<QueryEvolutionError> errors;

    private QueryEvolution(Id id, Id categoryId, Version version, Id queryId, String newContent, String oldContent, List<QueryEvolutionError> errors) {
        super(id, categoryId, version, EvolutionType.query);
        this.queryId = queryId;
        this.newContent = newContent;
        this.oldContent = oldContent;
        this.errors = errors;
    }

    public static QueryEvolution createNew(Id categoryId, Version version, Id queryId, String newContent, String oldContent, List<QueryEvolutionError> errors) {
        return new QueryEvolution(
            Id.createNew(),
            categoryId,
            version,
            queryId,
            newContent,
            oldContent,
            errors
        );
    }

    private record JsonValue(
        String newContent,
        String oldContent,
        List<QueryEvolutionError> errors
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);


    public static QueryEvolution fromJsonValue(Id id, Id categoryId, Version version, Id queryId, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        return new QueryEvolution(
            id,
            categoryId,
            version,
            queryId,
            json.newContent,
            json.oldContent,
            json.errors
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(
            newContent,
            oldContent,
            errors
        ));
    }

}
