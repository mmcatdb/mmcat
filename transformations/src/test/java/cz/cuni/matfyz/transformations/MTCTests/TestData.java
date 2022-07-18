package cz.cuni.matfyz.transformations.MTCTests;

import cz.cuni.matfyz.core.category.*;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.schema.SchemaMorphism.*;
import cz.cuni.matfyz.core.mapping.*;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class TestData {

    private final String dataFilePrefix = "MTC_additional/";

    public final Key userKey = new Key(100);
    public final Key u_idKey = new Key(101);
    public final Key nameKey = new Key(102);
    public final Key orderKey = new Key(103);
    public final Key o_idKey = new Key(104);
    
    public final Signature userToU_id = new Signature(1);
    public final Signature userToName = new Signature(2);
    public final Signature userToOrder = new Signature(3);
    public final Signature orderToO_id = new Signature(4);
    
    public final Signature orderToU_id = userToOrder.dual().concatenate(userToU_id);

    public SchemaCategory createInitialSchemaCategory() {
        var schema = new SchemaCategory();

        var user = addSchemaObject(schema, userKey, "user", new Id(userToU_id));
        var u_id = addSchemaObject(schema, u_idKey, "u_id", Id.Empty());
        var name = addSchemaObject(schema, nameKey, "name", Id.Empty());
        var order = addSchemaObject(schema, orderKey, "order", new Id(orderToO_id));
        var o_id = addSchemaObject(schema, o_idKey, "o_id", Id.Empty());

        addMorphismWithDual(schema, userToU_id, user, u_id, Min.ONE, Max.ONE, Min.ONE, Max.ONE);
        addMorphismWithDual(schema, userToName, user, name, Min.ONE, Max.ONE, Min.ONE, Max.STAR);
        addMorphismWithDual(schema, userToOrder, user, order, Min.ZERO, Max.STAR, Min.ONE, Max.ONE);
        addMorphismWithDual(schema, orderToO_id, order, o_id, Min.ONE, Max.ONE, Min.ONE, Max.ONE);

        return schema;
    }

    public static SchemaObject addSchemaObject(SchemaCategory schema, Key key, String name, Id id) {
        var object = new SchemaObject(key, name, id, Set.of(id));
        schema.addObject(object);
        return object;
    }

    public static SchemaMorphism addMorphismWithDual(SchemaCategory schema, Signature signature, SchemaObject dom, SchemaObject cod, Min min, Max max, Min dualMin, Max dualMax) {
        var builder = new SchemaMorphism.Builder();
        var morphism = builder.fromArguments(signature, dom, cod, min, max);
        var dual = builder.fromDual(morphism, dualMin, dualMax);

        schema.addMorphism(morphism);
        schema.addMorphism(dual);

        return morphism;
    }

    public Mapping createUserTableMapping(SchemaCategory schema) throws URISyntaxException {
        var rootObject = schema.getObject(userKey);
        var path = createUserTableAccessPath();
        var url = ClassLoader.getSystemResource(dataFilePrefix + "userData.json");
        var fileName = Paths.get(url.toURI()).toAbsolutePath().toString();
        var mapping = new Mapping.Builder().fromArguments(schema, rootObject, null, path, fileName, null);

        return mapping;
    }

    public Mapping createOrderTableMapping(SchemaCategory schema) throws URISyntaxException {
        var rootObject = schema.getObject(orderKey);
        var path = createOrderTableAccessPath();
        var url = ClassLoader.getSystemResource(dataFilePrefix + "orderData.json");
        var fileName = Paths.get(url.toURI()).toAbsolutePath().toString();
        var mapping = new Mapping.Builder().fromArguments(schema, rootObject, null, path, fileName, null);

        return mapping;
    }

    private ComplexProperty createUserTableAccessPath() {
        return new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("u_id", userToU_id),
            new SimpleProperty("name", userToName)
        );
    }

    private ComplexProperty createOrderTableAccessPath() {
        return new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("o_id", orderToO_id),
            new SimpleProperty("u_id", orderToU_id)
        );
    }

}
