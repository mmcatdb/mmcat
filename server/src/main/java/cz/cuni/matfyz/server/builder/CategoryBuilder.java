package cz.cuni.matfyz.server.builder;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.server.entity.Id;
import cz.cuni.matfyz.server.entity.schema.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaMorphismWrapper;
import cz.cuni.matfyz.server.entity.schema.SchemaObjectWrapper;

import java.util.Map;
import java.util.TreeMap;

/**
 * @author jachym.bartik
 */
public class CategoryBuilder {

    private final Map<Id, SchemaObject> createdObjects = new TreeMap<>();
    private final Map<Id, SchemaMorphism> createdMorphisms = new TreeMap<>();

    public SchemaObject getObject(Id id) {
        return createdObjects.get(id);
    }

    public SchemaMorphism getMorphism(Id id) {
        return createdMorphisms.get(id);
    }

    private SchemaCategoryWrapper categoryWrapper;

    public CategoryBuilder setCategoryWrapper(SchemaCategoryWrapper categoryWrapper) {
        this.categoryWrapper = categoryWrapper;

        return this;
    }

    public SchemaCategory build() {
        final var category = new SchemaCategory();

        for (final var objectWrapper : categoryWrapper.objects) {
            final var object = buildObject(objectWrapper);
            category.addObject(object);
        }

        for (final var morphismWrapper : categoryWrapper.morphisms) {
            final var morphism = buildMorphism(morphismWrapper);
            category.addMorphism(morphism);
        }

        return category;
    }

    private SchemaObject buildObject(SchemaObjectWrapper wrapper) {
        final var object = new SchemaObject.Builder().fromJSON(wrapper.jsonValue);
        createdObjects.put(wrapper.id, object);

        return object;
    }

    private SchemaMorphism buildMorphism(SchemaMorphismWrapper wrapper) {
        final var morphism = new SchemaMorphism.Builder().fromJSON(
            createdObjects.get(wrapper.domId),
            createdObjects.get(wrapper.codId),
            wrapper.jsonValue
        );
        createdMorphisms.put(wrapper.id, morphism);

        return morphism;
    }

}
