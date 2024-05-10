package cz.matfyz.server.entity.instance;

import cz.matfyz.core.identifiers.Key;
import cz.matfyz.core.instance.DomainRow;
import cz.matfyz.core.instance.InstanceCategory;
import cz.matfyz.core.instance.InstanceCategoryBuilder;
import cz.matfyz.core.instance.InstanceMorphism;
import cz.matfyz.core.instance.InstanceObject;
import cz.matfyz.core.schema.SchemaCategory;
import cz.matfyz.server.entity.Id;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;

public record InstanceCategoryWrapper(
    Id sessionId,
    Id categoryId,
    List<InstanceObjectWrapper> objects,
    List<InstanceMorphismWrapper> morphisms
) {

    record JsonData(
        List<InstanceObjectWrapper> objects,
        List<InstanceMorphismWrapper> morphisms
    ) {}

    private static final ObjectWriter jsonValueWriter = new ObjectMapper().writerFor(JsonData.class);

    public String toJsonValue() throws JsonProcessingException {
        final JsonData data = new JsonData(objects, morphisms);
        return jsonValueWriter.writeValueAsString(data);
    }

    private static final ObjectReader jsonValueReader = new ObjectMapper().readerFor(JsonData.class);

    public static InstanceCategoryWrapper fromJsonValue(Id sessionId, Id categoryId, String jsonValue) throws JsonProcessingException {
        final JsonData data = jsonValueReader.readValue(jsonValue);
        return new InstanceCategoryWrapper(sessionId, categoryId, data.objects, data.morphisms);
    }

    public static InstanceCategoryWrapper fromInstanceCategory(Id sessionId, Id categoryId, InstanceCategory instance) {
        final var context = new WrapperContext(instance);

        final List<InstanceObjectWrapper> objects = new ArrayList<>();
        for (final InstanceObject object : instance.objects().values())
            objects.add(InstanceObjectWrapper.fromInstanceObject(object, context));

        final List<InstanceMorphismWrapper> morphisms = new ArrayList<>();
        for (final InstanceMorphism morphism : instance.morphisms().values())
            morphisms.add(InstanceMorphismWrapper.fromInstanceMorphism(morphism, context));

        return new InstanceCategoryWrapper(
            sessionId,
            categoryId,
            objects,
            morphisms
        );
    }

    public InstanceCategory toInstanceCategory(SchemaCategory schemaCategory) {
        final var category = new InstanceCategoryBuilder().setSchemaCategory(schemaCategory).build();
        final var context = new WrapperContext(category);

        for (final InstanceObjectWrapper objectWrapper : objects)
            objectWrapper.toInstanceObject(context);

        for (final InstanceMorphismWrapper morphismWrapper : morphisms)
            morphismWrapper.toInstanceMorphism(context);
        //System.out.println("toInstanceCat: " + category.objects());
        //System.out.println("toInstanceCat: " + category.morphisms());
        return category;
    }

    static class WrapperContext {
        public final InstanceCategory category;
        public final Map<Key, Map<Integer, DomainRow>> idToRow = new TreeMap<>();
        public final Map<Key, Map<DomainRow, Integer>> rowToId = new TreeMap<>();

        WrapperContext(InstanceCategory category) {
            this.category = category;
        }
    }

}
