package cz.cuni.matfyz.server.entity.job;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;

import java.io.IOException;
import java.io.Serializable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.lang.Nullable;

/**
 * @author jachym.bartik
 */
@JsonSerialize(using = Job.Serializer.class)
@JsonDeserialize(using = Job.Deserializer.class)
public class Job extends Entity {

    public final Id categoryId;
    @Nullable
    public final Id logicalModelId;
    @Nullable
    public final Id dataSourceId;
    public String label;
    public Type type;
    public State state;

    @Nullable
    public Serializable data;

    /*
    public Job(
        @JsonProperty("id") Integer id,
        @JsonProperty("logicalModelId") int logicalModelId,
        @JsonProperty("state") JobState state
    ) {
        super(id);
        this.logicalModelId = logicalModelId;
        this.state = state;
    }
    */

    private Job(Id id, Id categoryId, Id logicalModelId, Id dataSourceId) {
        super(id);
        this.categoryId = categoryId;
        this.logicalModelId = logicalModelId;
        this.dataSourceId = dataSourceId;
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

    public boolean isValid() {
        return type == Type.JsonLdToCategory
            ? dataSourceId != null
            : logicalModelId != null;
    }

    public static class Builder {

        private static final ObjectReader jobJsonReader = new ObjectMapper().readerFor(Job.class);

        public Job fromJsonValue(Id id, Id categoryId, Id logicalModelId, Id dataSourceId, String jsonValue) throws JsonProcessingException {
            return jobJsonReader
                .withAttribute("id", id)
                .withAttribute("categoryId", categoryId)
                .withAttribute("logicalModelId", logicalModelId)
                .withAttribute("dataSourceId", dataSourceId)
                .readValue(jsonValue);
        }

        public Job fromInit(JobInit init) {
            final var job = new Job(null, init.categoryId(), init.logicalModelId(), init.dataSourceId());
            job.label = init.label();
            job.type = init.type();
            job.state = State.Ready;

            return job;
        }

    }

    public static class Serializer extends StdSerializer<Job> {
    
        public Serializer() {
            this(null);
        }
      
        public Serializer(Class<Job> t) {
            super(t);
        }
    
        @Override
        public void serialize(Job job, JsonGenerator generator, SerializerProvider provider) throws IOException {
            generator.writeStartObject();
            generator.writeStringField("label", job.label);
            generator.writeStringField("type", job.type.name());
            generator.writeStringField("state", job.state.name());
            generator.writeObjectField("data", job.data);
            generator.writeEndObject();
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
    
        @Override
        public Job deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            final JsonNode node = parser.getCodec().readTree(parser);
    
            final var id = (Id) context.getAttribute("id");
            final var categoryId = (Id) context.getAttribute("categoryId");
            final var logicalModelId = (Id) context.getAttribute("logicalModelId");
            final var dataSourceId = (Id) context.getAttribute("dataSourceId");

            final var job = new Job(id, categoryId, logicalModelId, dataSourceId);

            job.label = node.get("label").asText();
            job.type = Type.valueOf(node.get("type").asText());
            job.state = State.valueOf(node.get("state").asText());

            if (node.hasNonNull("data"))
                job.data = dataReader.readValue(node.get("data"));

            return job;
        }

    }
    
}
