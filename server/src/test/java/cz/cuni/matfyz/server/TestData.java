package cz.cuni.matfyz.server;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.schema.Id;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaMorphism.Max;
import cz.cuni.matfyz.core.schema.SchemaMorphism.Min;
import cz.cuni.matfyz.core.schema.SchemaObject;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * @author jachymb.bartik
 */
public class TestData {

    public final Key userKey = new Key(101);
    public final Key id_userKey = new Key(102);
    public final Key addressKey = new Key(103);
    public final Key streetKey = new Key(104);
    public final Key cityKey = new Key(105);

    public final Key orderKey = new Key(111);
    public final Key id_orderKey = new Key(112);

    public final Key fullAddressKey = new Key(131);
    
    public final Signature userToId_user = new Signature(1);
    public final Signature userToAddress = new Signature(2);
    public final Signature addressToStreet = new Signature(3);
    public final Signature addressToCity = new Signature(4);
    
    public final Signature orderToId_order = new Signature(11);
    
    public final Signature userToOrder = new Signature(21);

    public final Signature addressToFullAddress = new Signature(31);
    public final Signature orderToFullAddress = new Signature(32);

    private SchemaCategory createInitialSchemaCategory() {
        var schema = new SchemaCategory();

        var user = addSchemaObject(schema, userKey, "user", new Id(userToId_user));
        var id_user = addSchemaObject(schema, id_userKey, "id_user", Id.createEmpty());
        var address = addSchemaObject(schema, addressKey, "address", new Id(addressToStreet, addressToCity));
        var street = addSchemaObject(schema, streetKey, "street", Id.createEmpty());
        var city = addSchemaObject(schema, cityKey, "city", Id.createEmpty());

        addMorphismWithDual(schema, userToId_user, user, id_user, Min.ONE, Max.ONE, Min.ONE, Max.ONE);
        addMorphismWithDual(schema, userToAddress, user, address, Min.ONE, Max.ONE, Min.ONE, Max.ONE);
        addMorphismWithDual(schema, addressToStreet, address, street, Min.ONE, Max.ONE, Min.ONE, Max.STAR);
        addMorphismWithDual(schema, addressToCity, address, city, Min.ONE, Max.ONE, Min.ZERO, Max.STAR);

        var order = addSchemaObject(schema, orderKey, "order", new Id(orderToId_order));
        var id_order = addSchemaObject(schema, id_orderKey, "id_order", Id.createEmpty());

        addMorphismWithDual(schema, orderToId_order, order, id_order, Min.ONE, Max.ONE, Min.ONE, Max.ONE);

        addMorphismWithDual(schema, userToOrder, user, order, Min.ZERO, Max.STAR, Min.ONE, Max.ONE);

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

    public Mapping createInitialMapping() throws URISyntaxException {
        var schema = createInitialSchemaCategory();
        var rootObject = schema.getObject(userKey);
        var path = createUserAccessPath();
        var mapping = new Mapping.Builder().fromArguments(schema, rootObject, null, path, "customer", null);

        return mapping;
    }

    public Mapping createFinalMapping(SchemaCategory schema) {
        var rootObject = schema.getObject(orderKey);
        var path = createOrderAccessPath();
        var mapping = new Mapping.Builder().fromArguments(schema, rootObject, null, path, "App_Order", null);

        return mapping;
    }

    private ComplexProperty createUserAccessPath() {
        return new ComplexProperty(StaticName.createAnonymous(), Signature.createNull(),
            new SimpleProperty("id", userToId_user),
            new ComplexProperty("contact_address", userToAddress,
                new SimpleProperty("street", addressToStreet),
                new SimpleProperty("city", addressToCity)
            ),
            new ComplexProperty("order", userToOrder,
                new SimpleProperty("id", orderToId_order)
            )
        );
    }

    private ComplexProperty createOrderAccessPath() {
        return new ComplexProperty(StaticName.createAnonymous(), Signature.createNull(),
            new SimpleProperty("id", orderToId_order),
            new SimpleProperty("customer_id", userToOrder.dual().concatenate(userToId_user)),
            new SimpleProperty("fullAddress", orderToFullAddress)
        );
    }

}
