package cz.matfyz.server.entity.instance;

import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceSerializer;
import cz.matfyz.core.instance.InstanceSerializer.SerializedInstance;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.server.entity.Id;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public record InstanceCategoryWrapper(
    Id sessionId,
    Id categoryId,
    SerializedInstance data
) {

    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(SerializedInstance.class);

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(data);
    }

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(SerializedInstance.class);

    public static InstanceCategoryWrapper fromJsonValue(Id sessionId, Id categoryId, String jsonValue) throws JsonProcessingException {
        final SerializedInstance data = jsonValueReader.readValue(jsonValue);
        return new InstanceCategoryWrapper(sessionId, categoryId, data);
    }

    public static InstanceCategoryWrapper fromInstanceCategory(Id sessionId, Id categoryId, InstanceCategory instance) {
        return new InstanceCategoryWrapper(
            sessionId,
            categoryId,
            InstanceSerializer.serialize(instance)
        );
    }

    public InstanceCategory toInstanceCategory(SchemaCategory schema) {
        return InstanceSerializer.deserialize(data, schema);
    }

}
