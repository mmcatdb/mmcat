package cz.cuni.matfyz.server;

import static cz.cuni.matfyz.core.tests.TestDataUtils.addMorphism;
import static cz.cuni.matfyz.core.tests.TestDataUtils.addSchemaObject;

import cz.cuni.matfyz.core.category.Morphism.Min;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.ObjectIds;
import cz.cuni.matfyz.core.schema.SchemaCategory;

import java.net.URISyntaxException;

/**
 * @author jachymb.bartik
 */
public class TestData {

    public final Key userKey = new Key(101);
    public final Key userIdKey = new Key(102);
    public final Key addressKey = new Key(103);
    public final Key streetKey = new Key(104);
    public final Key cityKey = new Key(105);

    public final Key orderKey = new Key(111);
    public final Key idOrderKey = new Key(112);

    public final Key fullAddressKey = new Key(131);
    
    public final Signature userToUserId = Signature.createBase(1);
    public final Signature userToAddress = Signature.createBase(2);
    public final Signature addressToStreet = Signature.createBase(3);
    public final Signature addressToCity = Signature.createBase(4);
    
    public final Signature orderToOrderId = Signature.createBase(11);
    
    public final Signature orderToUser = Signature.createBase(21);

    public final Signature addressToFullAddress = Signature.createBase(31);
    public final Signature orderToFullAddress = Signature.createBase(32);

    private SchemaCategory createInitialSchemaCategory() {
        final var schema = new SchemaCategory("");

        final var user = addSchemaObject(schema, userKey, "user", new ObjectIds(userToUserId));
        final var userId = addSchemaObject(schema, userIdKey, "id_user", ObjectIds.createValue());
        final var address = addSchemaObject(schema, addressKey, "address", new ObjectIds(addressToStreet, addressToCity));
        final var street = addSchemaObject(schema, streetKey, "street", ObjectIds.createValue());
        final var city = addSchemaObject(schema, cityKey, "city", ObjectIds.createValue());

        addMorphism(schema, userToUserId, user, userId, Min.ONE);
        addMorphism(schema, userToAddress, user, address, Min.ONE);
        addMorphism(schema, addressToStreet, address, street, Min.ONE);
        addMorphism(schema, addressToCity, address, city, Min.ONE);

        final var order = addSchemaObject(schema, orderKey, "order", new ObjectIds(orderToOrderId));
        final var orderId = addSchemaObject(schema, idOrderKey, "id_order", ObjectIds.createValue());

        addMorphism(schema, orderToOrderId, order, orderId, Min.ONE);

        addMorphism(schema, orderToUser, order, user, Min.ONE);

        return schema;
    }

    public Mapping createInitialMapping() throws URISyntaxException {
        var schema = createInitialSchemaCategory();
        var rootObject = schema.getObject(userKey);
        var path = createUserAccessPath();
        var mapping = new Mapping(schema, rootObject, path, "customer", null);

        return mapping;
    }

    public Mapping createFinalMapping(SchemaCategory schema) {
        var rootObject = schema.getObject(orderKey);
        var path = createOrderAccessPath();
        var mapping = new Mapping(schema, rootObject, path, "App_Order", null);

        return mapping;
    }

    private ComplexProperty createUserAccessPath() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("_id", userToUserId),
            ComplexProperty.create("contact_address", userToAddress,
                new SimpleProperty("street", addressToStreet),
                new SimpleProperty("city", addressToCity)
            ),
            ComplexProperty.create("order", orderToUser.dual(),
                new SimpleProperty("id", orderToOrderId)
            )
        );
    }

    private ComplexProperty createOrderAccessPath() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("id", orderToOrderId),
            new SimpleProperty("customer_id", orderToUser.concatenate(userToUserId)),
            new SimpleProperty("fullAddress", orderToFullAddress)
        );
    }

}
