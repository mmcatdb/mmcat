package cz.matfyz.server.job;

import cz.matfyz.server.utils.entity.Entity;
import cz.matfyz.server.utils.entity.Id;
import cz.matfyz.server.job.JobRepository.JobInfo;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A single unit of work that can be executed.
 * Once processed, it can't be restarted. However, a new job can be created instead.
 */
public class Job extends Entity {

    public enum State {
        /** The job won't start automatically. */
        Disabled,
        /** The job will be started automatically as soon as possible (when the jobs it depends on finish). */
        Ready,
        /** The job is currently being processed. */
        Running,
        /** The job is waiting for a manual input. */
        Waiting,
        /** The job is finished, either with a success or with an error. */
        Finished,
        /** The job failed. */
        Failed,
    }

    public final Id runId;
    /** Sequential order in the run. */
    public final int index;
    public final String label;
    public final Date createdAt;
    /** The job contains all information needed to execute it. */
    public final JobPayload payload;
    public State state;
    public @Nullable JobData data = null;
    public @Nullable Serializable error = null;

    private Job(Id id, Id runId, int index, String label, Date createdAt, JobPayload payload, State state) {
        super(id);
        this.runId = runId;
        this.index = index;
        this.label = label;
        this.createdAt = createdAt;
        this.payload = payload;
        this.state = state;
    }

    public static Job createNew(Id runId, int index, String label, JobPayload payload, boolean isStartedAutomatically) {
        return new Job(
            Id.createNew(),
            runId,
            index,
            label,
            new Date(),
            payload,
            isStartedAutomatically ? State.Ready : State.Disabled
        );
    }

    private record JsonValue(
        int index,
        String label,
        Date createdAt,
        JobPayload payload,
        State state,
        @Nullable JobData data,
        @Nullable Serializable error
    ) {}

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonValue.class);
    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonValue.class);

    public static Job fromJsonValue(Id id, Id runId, String jsonValue) throws JsonProcessingException {
        final JsonValue json = jsonValueReader.readValue(jsonValue);
        final var job = new Job(
            id,
            runId,
            json.index,
            json.label,
            json.createdAt,
            json.payload,
            json.state
        );
        job.data = json.data;
        job.error = json.error;

        return job;
    }

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(new JsonValue(
            index,
            label,
            createdAt,
            payload,
            state,
            data,
            error
        ));
    }

    /**
     * Sorts the jobs by their indexes (smaller indexes first) and then by their creation times (earlier first).
     * So the last job with each index is the one "active" job for the index.
     */
    public int compareInRun(Job other) {
        final var indexComparison = index - other.index;
        return indexComparison != 0
            ? indexComparison
            : createdAt.compareTo(other.createdAt);
    }

    public static int compareInRun(JobInfo a, JobInfo b) {
        final var indexComparison = a.index() - b.index();
        return indexComparison != 0
            ? indexComparison
            : a.createdAt().compareTo(b.createdAt());
    }

}
