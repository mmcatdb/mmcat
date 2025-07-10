package cz.matfyz.server.entity;

import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceSerializer;
import cz.matfyz.core.instance.InstanceSerializer.SerializedInstance;
import cz.matfyz.core.schema.SchemaCategory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public record InstanceCategoryEntity(
    Id sessionId,
    Id categoryId,
    SerializedInstance data
) {

    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(SerializedInstance.class);

    public String toJsonValue() throws JsonProcessingException {
        return jsonValueWriter.writeValueAsString(data);
    }

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(SerializedInstance.class);

    public static InstanceCategoryEntity fromJsonValue(Id sessionId, Id categoryId, String jsonValue) throws JsonProcessingException {
        final SerializedInstance data = jsonValueReader.readValue(jsonValue);
        return new InstanceCategoryEntity(sessionId, categoryId, data);
    }

    public static InstanceCategoryEntity fromInstanceCategory(Id sessionId, Id categoryId, InstanceCategory instance) {
        return new InstanceCategoryEntity(
            sessionId,
            categoryId,
            InstanceSerializer.serialize(instance)
        );
    }

    public InstanceCategory toInstanceCategory(SchemaCategory schema) {
        return InstanceSerializer.deserialize(data, schema);
    }

}
