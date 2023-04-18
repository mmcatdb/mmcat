package cz.cuni.matfyz.core.tests;

import cz.cuni.matfyz.core.category.Morphism.Min;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.instance.InstanceCategoryBuilder;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.ObjectIds;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;

/**
 * @author jachymb.bartik
 */
public class TestDataUtils {

    public static SchemaObject addSchemaObject(SchemaCategory schema, Key key, String name, ObjectIds ids) {
        var object = new SchemaObject(key, name, ids.generateDefaultSuperId(), ids);
        schema.addObject(object);

        return object;
    }
    
    public static SchemaMorphism addMorphism(SchemaCategory schema, Signature signature, SchemaObject dom, SchemaObject cod, Min min) {
        final var builder = new SchemaMorphism.Builder();
        final var morphism = builder.fromArguments(signature, dom, cod, min);

        schema.addMorphism(morphism);

        return morphism;
    }
    
    public static InstanceCategory buildInstanceScenario(SchemaCategory schema) {
        return new InstanceCategoryBuilder().setSchemaCategory(schema).build();
    }

}
