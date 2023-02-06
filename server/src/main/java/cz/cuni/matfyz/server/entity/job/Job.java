package cz.cuni.matfyz.server.entity.job;

import cz.cuni.matfyz.server.entity.Entity;
import cz.cuni.matfyz.server.entity.Id;

import java.io.IOException;

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
    public Status status;

    /*
    public Job(
        @JsonProperty("id") Integer id,
        @JsonProperty("logicalModelId") int logicalModelId,
        @JsonProperty("status") Status status
    ) {
        super(id);
        this.logicalModelId = logicalModelId;
        this.status = status;
    }
    */

    private Job(Id id, Id categoryId, Id logicalModelId, Id dataSourceId) {
        super(id);
        this.categoryId = categoryId;
        this.logicalModelId = logicalModelId;
        this.dataSourceId = dataSourceId;
    }

    public enum Status {
        Default, // The job isn't created yet.
        Ready, // The job can be started now.
        Running, // The job is currently being processed.
        Finished, // The job is finished, either with a success or with an error.
        Canceled // The job was canceled while being in one of the previous states. It can never be started (again).
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

        private static final ObjectReader jobJSONReader = new ObjectMapper().readerFor(Job.class);

        public Job fromJSON(Id id, Id categoryId, Id logicalModelId, Id dataSourceId, String jsonValue) throws JsonProcessingException {
            return jobJSONReader
                .withAttribute("id", id)
                .withAttribute("categoryId", categoryId)
                .withAttribute("logicalModelId", logicalModelId)
                .withAttribute("dataSourceId", dataSourceId)
                .readValue(jsonValue);
        }

        public Job fromArguments(Id id, Id categoryId, Id logicalModelId, Id dataSourceId, String label, Type type, Status status) {
            var job = new Job(id, categoryId, logicalModelId, dataSourceId);
            job.label = label;
            job.type = type;
            job.status = status;

            return job;
        }

        public Job fromInit(JobInit init) {
            final var job = new Job(null, init.categoryId(), init.logicalModelId(), init.dataSourceId());
            job.label = init.label();
            job.type = init.type();
            job.status = Status.Ready;

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
            generator.writeStringField("status", job.status.name());
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
            job.status = Status.valueOf(node.get("status").asText());

            return job;
        }

    }
    
}
