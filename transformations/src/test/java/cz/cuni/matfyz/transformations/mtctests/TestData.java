package cz.cuni.matfyz.transformations.mtctests;

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
import java.nio.file.Paths;

/**
 * @author jachymb.bartik
 */
public class TestData {

    private final String dataFilePrefix = "MTC_additional/";

    public final Key userKey = new Key(100);
    public final Key userIdKey = new Key(101);
    public final Key nameKey = new Key(102);
    public final Key orderKey = new Key(103);
    public final Key orderIdKey = new Key(104);
    
    public final Signature userToUserId = Signature.createBase(1);
    public final Signature userToName = Signature.createBase(2);
    public final Signature orderToUser = Signature.createBase(3);
    public final Signature orderToOrderId = Signature.createBase(4);
    
    public final Signature orderToUserId = orderToUser.concatenate(userToUserId);

    public SchemaCategory createInitialSchemaCategory() {
        final var schema = new SchemaCategory("");

        final var user = addSchemaObject(schema, userKey, "user", new ObjectIds(userToUserId));
        final var userId = addSchemaObject(schema, userIdKey, "u_id", ObjectIds.createValue());
        final var name = addSchemaObject(schema, nameKey, "name", ObjectIds.createValue());
        final var order = addSchemaObject(schema, orderKey, "order", new ObjectIds(orderToOrderId));
        final var orderId = addSchemaObject(schema, orderIdKey, "o_id", ObjectIds.createValue());

        addMorphism(schema, userToUserId, user, userId, Min.ONE);
        addMorphism(schema, userToName, user, name, Min.ONE);
        addMorphism(schema, orderToUser, order, user, Min.ONE);
        addMorphism(schema, orderToOrderId, order, orderId, Min.ONE);

        return schema;
    }

    public Mapping createUserTableMapping(SchemaCategory schema) throws URISyntaxException {
        var rootObject = schema.getObject(userKey);
        var path = createUserTableAccessPath();
        var url = ClassLoader.getSystemResource(dataFilePrefix + "userData.json");
        var fileName = Paths.get(url.toURI()).toAbsolutePath().toString();
        var mapping = new Mapping(schema, rootObject, path, fileName, null);

        return mapping;
    }

    public Mapping createOrderTableMapping(SchemaCategory schema) throws URISyntaxException {
        var rootObject = schema.getObject(orderKey);
        var path = createOrderTableAccessPath();
        var url = ClassLoader.getSystemResource(dataFilePrefix + "orderData.json");
        var fileName = Paths.get(url.toURI()).toAbsolutePath().toString();
        var mapping = new Mapping(schema, rootObject, path, fileName, null);

        return mapping;
    }

    private ComplexProperty createUserTableAccessPath() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("u_id", userToUserId),
            new SimpleProperty("name", userToName)
        );
    }

    private ComplexProperty createOrderTableAccessPath() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("o_id", orderToOrderId),
            new SimpleProperty("u_id", orderToUserId)
        );
    }

}
