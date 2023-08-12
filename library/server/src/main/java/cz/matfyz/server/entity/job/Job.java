package cz.matfyz.server.entity.job;

import cz.matfyz.server.entity.Entity;
import cz.matfyz.server.entity.Id;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.lang.Nullable;

/**
 * @author jachym.bartik
 */
@JsonDeserialize(using = Job.Deserializer.class)
public class Job extends Entity {

    @JsonIgnore
    public final Id categoryId;
    public final String label;
    // public Type type;
    public State state;

    public final JobPayload payload;

    @Nullable
    public Serializable data;

    // private Job(Id id, Id categoryId, Id logicalModelId, Id dataSourceId) {
    //     super(id);
    //     this.categoryId = categoryId;
    //     this.logicalModelId = logicalModelId;
    //     this.dataSourceId = dataSourceId;
    // }

    protected Job(Id id, Id categoryId, String label, State state, JobPayload payload, Serializable data) {
        super(id);
        this.categoryId = categoryId;
        this.label = label;
        this.state = state;
        this.payload = payload;
        this.data = data;
    }

    public enum State {
        Default, // The job isn't created yet.
        Ready, // The job can be started now.
        Running, // The job is currently being processed.
        Finished, // The job is finished, either with a success or with an error.
        Canceled, // The job was canceled while being in one of the previous states. It can never be started (again).
        Failed // The job failed.
    }

    public enum Type {
        ModelToCategory,
        CategoryToModel,
        JsonLdToCategory
    }

    public static class Builder {

        private static final ObjectReader jobJsonReader = new ObjectMapper().readerFor(Job.class);

        public Job fromJsonValue(Id id, Id categoryId, String jsonValue) throws JsonProcessingException {
            return jobJsonReader
                .withAttribute("id", id)
                .withAttribute("categoryId", categoryId)
                .readValue(jsonValue);
        }

        public Job fromInit(JobInit init) {
            return new Job(
                null,
                init.categoryId(),
                init.label(),
                State.Ready,
                init.payload(),
                null
            );
        }

    }

    public static class Deserializer extends StdDeserializer<Job> {

        public Deserializer() {
            this(null);
        }
    
        public Deserializer(Class<?> vc) {
            super(vc);
        }

        private static final ObjectReader dataReader = new ObjectMapper().readerFor(Serializable.class);
        private static final ObjectReader payloadReader = new ObjectMapper().readerFor(JobPayload.class);
    
        @Override
        public Job deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);
    
            final Id id = (Id) context.getAttribute("id");
            final Id categoryId = (Id) context.getAttribute("categoryId");

            final String label = node.get("label").asText();
            final State state = State.valueOf(node.get("state").asText());

            final JobPayload payload = payloadReader.readValue(node.get("payload"));
            final Serializable data = node.hasNonNull("data") ? dataReader.readValue(node.get("data")) : null;

            return new Job(
                id,
                categoryId,
                label,
                state,
                payload,
                data
            );
        }

    }
    
}
