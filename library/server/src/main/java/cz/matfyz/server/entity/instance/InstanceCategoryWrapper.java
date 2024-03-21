package cz.matfyz.server.entity.instance;

import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.server.entity.Id;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public record InstanceCategoryWrapper(
    Id categoryId,
    Id sessionId,
    String jsonData
) {

    private static final ObjectWriter jsonDataWriter = new ObjectMapper().writerFor(InstanceCategory.class);

    public static InstanceCategoryWrapper fromInstanceCategory(InstanceCategory instance, Id categoryId, Id sessionId) throws JsonProcessingException {
        final String jsonData = jsonDataWriter.writeValueAsString(instance);

        return new InstanceCategoryWrapper(
            categoryId,
            sessionId,
            jsonData
        );
    }

    private static final ObjectReader jsonDataReader = new ObjectMapper().readerFor(InstanceCategory.class);

    public InstanceCategory toInstanceCategory(String jsonData, SchemaCategory schemaCategory) throws JsonProcessingException {
        return jsonDataReader.withAttribute("schemaCategory", schemaCategory).readValue(jsonData);
    }

}
