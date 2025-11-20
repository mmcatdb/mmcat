package cz.matfyz.server.evolution;

import cz.matfyz.evolution.Version;
import cz.matfyz.server.utils.Utils;
import cz.matfyz.server.utils.entity.Id;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

public class MappingEvolution extends Evolution {

    public final Id mappingId;
    // TODO this
    public final Object todo;

    private MappingEvolution(Id id, Id categoryId, Version version, Id mappingId, Object todo) {
        super(id, categoryId, version, EvolutionType.mapping);
        this.mappingId = mappingId;
        this.todo = todo;
    }

    public static MappingEvolution createNew(Id categoryId, Version version, Id mappingId, Object todo) {
        return new MappingEvolution(
            Id.createNew(),
            categoryId,
            version,
            mappingId,
            todo
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return Utils.toJson(new JsonValue(todo));
    }

    private record JsonValue(
        Object todo
    ) {}

    private static final ObjectReader reader = new ObjectMapper().readerFor(JsonValue.class);

    public static MappingEvolution fromJsonValue(Id id, Id categoryId, Version version, Id mappingId, String jsonValue) throws JsonProcessingException {
        final JsonValue parsedValue = reader.readValue(jsonValue);

        return new MappingEvolution(
            id,
            categoryId,
            version,
            mappingId,
            parsedValue.todo
        );
    }

}
