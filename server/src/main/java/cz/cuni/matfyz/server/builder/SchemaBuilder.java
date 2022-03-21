package cz.cuni.matfyz.server.builder;

import java.util.Map;
import java.util.TreeMap;

import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.server.entity.MappingWrapper;
import cz.cuni.matfyz.server.entity.SchemaCategoryWrapper;
import cz.cuni.matfyz.server.entity.SchemaMorphismWrapper;
import cz.cuni.matfyz.server.entity.SchemaObjectWrapper;

/**
 * 
 * @author jachym.bartik
 */
public class SchemaBuilder {

    private final Map<Integer, SchemaObject> createdObjects = new TreeMap<>();
    private final Map<Integer, SchemaMorphism> createdMorphisms = new TreeMap<>();

    private SchemaCategoryWrapper categoryWrapper;
    private MappingWrapper mappingWrapper;

    public SchemaBuilder setCategoryWrapper(SchemaCategoryWrapper categoryWrapper) {
        this.categoryWrapper = categoryWrapper;
        return this;
    }

    public SchemaBuilder setMappingWrapper(MappingWrapper mappingWrapper) {
        this.mappingWrapper = mappingWrapper;
        return this;
    }

    public Mapping build() {
        var category = buildCategory();
        var rootObject = createdObjects.get(mappingWrapper.rootObjectId);
        var rootMorphism = mappingWrapper.rootMorphismId == null ? null : createdMorphisms.get(mappingWrapper.rootMorphismId);
        
        return new Mapping.Builder().fromJSON(category, rootObject, rootMorphism, mappingWrapper.mappingJsonValue);
    }

    private SchemaObject buildObject(SchemaObjectWrapper wrapper) {
        var object = new SchemaObject.Builder().fromJSON(wrapper.jsonValue);
        createdObjects.put(wrapper.id, object);
        return object;
    }

    private SchemaMorphism buildMorphism(SchemaMorphismWrapper wrapper) {
        var morphism = new SchemaMorphism.Builder().fromJSON(
            createdObjects.get(wrapper.domId),
            createdObjects.get(wrapper.codId),
            wrapper.jsonValue
        );
        createdMorphisms.put(wrapper.id, morphism);
        return morphism;
    }

    private SchemaCategory buildCategory() {
        var category = new SchemaCategory();

        for (var objectWrapper : categoryWrapper.objects) {
            var object = buildObject(objectWrapper);
            category.addObject(object);
        }

        for (var morphismWrapper : categoryWrapper.morphisms) {
            var morphism = buildMorphism(morphismWrapper);
            category.addMorphism(morphism);
        }

        return category;
    }

}
