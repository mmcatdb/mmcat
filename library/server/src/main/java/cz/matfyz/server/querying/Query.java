package cz.matfyz.server.querying;

import cz.matfyz.evolution.Version;
import cz.matfyz.evolution.querying.QueryEvolutionResult.QueryEvolutionError;
import cz.matfyz.server.utils.entity.Id;
import cz.matfyz.server.utils.entity.VersionedEntity;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

public class Query extends VersionedEntity {

    public final Id categoryId;
    public String label;
    public String content;
    public List<QueryEvolutionError> errors;
    /**
     * The weight of the query (in comparison with other queries) in the adaptation process.
     * If null, the weight is considered to be the {@link QueryStats#executionCount}.
     * It's a double to allow granularity but it can safely be anything from 0 to infinity.
     */
    public @Nullable Double weight;
    /**
     * If null, the stats are not defined - basically because the query wan't run yet.
     * That's very different from having stats with zero values - e.g., when mergning stats, 0 in min would never be replaced by another value.
     * Also, queries with no stats can't be used in some places - but that's a feature, not a bug. If they don't have stats, we can't be sure they work properly.
     */
    public @Nullable QueryStats stats;

    public final int index;

    private Query(Id id, Version version, Version lastValid, Id categoryId, String label, String content, List<QueryEvolutionError> errors, @Nullable Double weight, @Nullable QueryStats stats, int index) {
        super(id, version, lastValid);
        this.categoryId = categoryId;
        this.label = label;
        this.content = content;
        this.errors = errors;
        this.weight = weight;
        this.stats = stats;
        this.index = index;
    }

    public static Query createNew(Version version, Id categoryId, String label, String content) {
        return new Query(
            Id.createNew(),
            version,
            version,
            categoryId,
            label,
            content,
            List.of(),
            null,
            null,
            0 // Will be set properly when saving to the database, so it's not a problem.
        );
    }

    public double effectiveWeight() {
        if (weight != null)
            return weight;
        if (stats != null)
            return stats.executionCount();
        return 0;
    }

    private record JsonValue(
        String label,
        String content,
        List<QueryEvolutionError> errors,
        @Nullable Double weight,
        @Nullable QueryStats stats
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static Query fromJsonValue(Id id, Version version, Version lastValid, Id categoryId, int index, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        return new Query(
            id,
            version,
            lastValid,
            categoryId,
            json.label,
            json.content,
            json.errors,
            json.weight,
            json.stats,
            index
        );
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(
            label,
            content,
            errors,
            weight,
            stats
        ));
    }

}
