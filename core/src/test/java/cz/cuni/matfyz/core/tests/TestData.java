package cz.cuni.matfyz.core.tests;

import static cz.cuni.matfyz.core.tests.TestDataUtils.addMorphism;
import static cz.cuni.matfyz.core.tests.TestDataUtils.addSchemaObject;
import static cz.cuni.matfyz.core.tests.TestDataUtils.buildInstanceScenario;

import cz.cuni.matfyz.core.TestInstanceCategoryBuilder;
import cz.cuni.matfyz.core.category.Morphism.Min;
import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.DomainRow;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.SimpleProperty;
import cz.cuni.matfyz.core.mapping.StaticName;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.ObjectIds;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;

/**
 * @author jachymb.bartik
 */
public class TestData {

    public final Key customerKey = new Key(100);
    public final Key idKey = new Key(101);
    public final Key orderedKey = new Key(102);
    public final Key orderKey = new Key(103);
    public final Key numberKey = new Key(104);
    public final Key arrayKey = new Key(105);
    public final Key contactKey = new Key(106);
    public final Key valueKey = new Key(107);
    public final Key typeKey = new Key(108);
    public final Key nameKey = new Key(109);
    public final Key nestedDocKey = new Key(110);
    public final Key propertyAKey = new Key(111);
    public final Key propertyBKey = new Key(112);
    public final Key propertyCKey = new Key(113);
    public final Key itemsKey = new Key(114);
    public final Key quantityKey = new Key(115);
    public final Key productKey = new Key(116);
    public final Key pidKey = new Key(117);
    public final Key pnameKey = new Key(118);
    public final Key priceKey = new Key(119);
    public final Key addressKey = new Key(120);
    public final Key labelKey = new Key(121);
    public final Key contentKey = new Key(122);
    public final Key textKey = new Key(123);
    public final Key localeKey = new Key(124);
    
    public final Signature customerToId = Signature.createBase(1);
    public final Signature orderedToCustomer = Signature.createBase(2);
    public final Signature orderedToOrder = Signature.createBase(3);
    public final Signature orderToNestedDoc = Signature.createBase(4);
    public final Signature nestedDocToPropertyA = Signature.createBase(5);
    public final Signature nestedDocToPropertyB = Signature.createBase(6);
    public final Signature nestedDocToPropertyC = Signature.createBase(7);
    public final Signature itemsToOrder = Signature.createBase(8);
    public final Signature itemsToProduct = Signature.createBase(9);
    public final Signature productToPid = Signature.createBase(10);
    public final Signature productToPrice = Signature.createBase(11);
    public final Signature productToPname = Signature.createBase(12);
    public final Signature itemsToQuantity = Signature.createBase(13);
    public final Signature contactToOrder = Signature.createBase(14);
    public final Signature contactToType = Signature.createBase(15);
    public final Signature typeToName = Signature.createBase(16);
    public final Signature contactToValue = Signature.createBase(17);
    public final Signature arrayToOrder = Signature.createBase(18);
    public final Signature orderToNumber = Signature.createBase(19);
    public final Signature addressToOrder = Signature.createBase(20);
    public final Signature addressToLabel = Signature.createBase(21);
    public final Signature addressToContent = Signature.createBase(22);
    public final Signature contentToText = Signature.createBase(23);
    public final Signature contentToLocale = Signature.createBase(24);
    
    public final Signature itemsToNumber = itemsToOrder.concatenate(orderToNumber);
    public final Signature itemsToPid = itemsToProduct.concatenate(productToPid);
    public final Signature itemsToPname = itemsToProduct.concatenate(productToPname);
    public final Signature itemsToPrice = itemsToProduct.concatenate(productToPrice);
    
    public final Signature contactToNumber = contactToOrder.concatenate(orderToNumber);
    public final Signature contactToName = contactToType.concatenate(typeToName);
    
    public final Signature orderedToNumber = orderedToOrder.concatenate(orderToNumber);
    public final Signature orderedToId = orderedToCustomer.concatenate(customerToId);
    public final Signature orderToId = orderedToOrder.dual().concatenate(orderedToId);
    public final Signature orderToCustomer = orderedToOrder.dual().concatenate(orderedToCustomer).concatenate(customerToId);
    
    public final Signature addressToNumber = addressToOrder.concatenate(orderToNumber);

    public SchemaCategory createDefaultSchemaCategory() {
        var schema = new SchemaCategory("");
        var order = buildOrder(schema);
        addArray(schema, order);
        addItems(schema, order);
        addContact(schema, order);
        addNestedDoc(schema, order);
        addOrdered(schema, order);
        addAddress(schema, order);

        return schema;
    }

    public SchemaCategory createDefaultV3SchemaCategory() {
        var schema = new SchemaCategory("");
        var order = addSchemaObject(
            schema,
            orderKey,
            "OrderV3",
            ObjectIds.createValue()
        );

        var number = addSchemaObject(
            schema,
            numberKey,
            "NumberV3",
            ObjectIds.createValue()
        );
        addMorphism(schema, orderToNumber, order, number, Min.ONE);

        return schema;
    }

    private SchemaObject buildOrder(SchemaCategory schema) {
        var order = addSchemaObject(
            schema,
            orderKey,
            "Order",
            new ObjectIds(orderToNumber)
        );
        
        var number = addSchemaObject(
            schema,
            numberKey,
            "Number",
            ObjectIds.createValue()
        );
        addMorphism(schema, orderToNumber, order, number, Min.ONE);
        
        return order;
    }

    private void addArray(SchemaCategory schema, SchemaObject order) {
        var array = addSchemaObject(
            schema,
            arrayKey,
            "Array",
            ObjectIds.createValue()
        );
        addMorphism(schema, arrayToOrder, array, order, Min.ONE);
    }
    
    private void addItems(SchemaCategory schema, SchemaObject order) {
        //var number = schema.getObject(numberKey);

        var items = addSchemaObject(
            schema,
            itemsKey,
            "Items",
            new ObjectIds(itemsToNumber, itemsToPid)
        );
        addMorphism(schema, itemsToOrder, items, order, Min.ONE);
        //addMorphism(schema, itemsToNumber, items, number, Min.ONE);
        
        var quantity = addSchemaObject(
            schema,
            quantityKey,
            "Quantity",
            ObjectIds.createValue()
        );
        addMorphism(schema, itemsToQuantity, items, quantity, Min.ONE);
        
        var product = addSchemaObject(
            schema,
            productKey,
            "Product",
            new ObjectIds(productToPid)
        );
        addMorphism(schema, itemsToProduct, items, product, Min.ONE);
        
        var pid = addSchemaObject(
            schema,
            pidKey,
            "Id",
            ObjectIds.createValue()
        );
        addMorphism(schema, productToPid, product, pid, Min.ONE);
        //addMorphism(schema, itemsToPid, items, pid, Min.ONE);

        var price = addSchemaObject(
            schema,
            priceKey,
            "Price",
            ObjectIds.createValue()
        );
        addMorphism(schema, productToPrice, product, price, Min.ONE);
        //addMorphism(schema, itemsToPrice, items, price, Min.ONE);

        var pname = addSchemaObject(
            schema,
            pnameKey,
            "Name",
            ObjectIds.createValue()
        );
        addMorphism(schema, productToPname, product, pname, Min.ONE);
        //addMorphism(schema, itemsToPname, items, pname, Min.ONE);
    }
    
    private void addContact(SchemaCategory schema, SchemaObject order) {
        var contact = addSchemaObject(
            schema,
            contactKey,
            "Contact",
            new ObjectIds(contactToNumber, contactToValue, contactToName)
        );
        addMorphism(schema, contactToOrder, contact, order, Min.ONE);

        var value = addSchemaObject(
            schema,
            valueKey,
            "Value",
            ObjectIds.createValue()
        );
        addMorphism(schema, contactToValue, contact, value, Min.ONE);

        var type = addSchemaObject(
            schema,
            typeKey,
            "Type",
            new ObjectIds(typeToName)
        );
        addMorphism(schema, contactToType, contact, type, Min.ONE);

        var name = addSchemaObject(
            schema,
            nameKey,
            "Name",
            ObjectIds.createValue()
        );
        addMorphism(schema, typeToName, type, name, Min.ONE);
        addMorphism(schema, contactToName, contact, name, Min.ONE);
    }
    
    private void addNestedDoc(SchemaCategory schema, SchemaObject order) {
        var nestedDoc = addSchemaObject(
            schema,
            nestedDocKey,
            "NestedDoc",
            ObjectIds.createGenerated()
        );
        addMorphism(schema, orderToNestedDoc, order, nestedDoc, Min.ONE);
        
        var propertyA = addSchemaObject(
            schema,
            propertyAKey,
            "PropertyA",
            ObjectIds.createValue()
        );
        addMorphism(schema, nestedDocToPropertyA, nestedDoc, propertyA, Min.ONE);

        var propertyB = addSchemaObject(
            schema,
            propertyBKey,
            "PropertyB",
            ObjectIds.createValue()
        );
        addMorphism(schema, nestedDocToPropertyB, nestedDoc, propertyB, Min.ONE);

        var propertyC = addSchemaObject(
            schema,
            propertyCKey,
            "PropertyC",
            ObjectIds.createValue()
        );
        addMorphism(schema, nestedDocToPropertyC, nestedDoc, propertyC, Min.ONE);
    }
    
    private void addOrdered(SchemaCategory schema, SchemaObject order) {
        var ordered = addSchemaObject(
            schema,
            orderedKey,
            "Ordered",
            new ObjectIds(orderedToNumber, orderedToId)
        );
        addMorphism(schema, orderedToOrder, ordered, order, Min.ONE);

        var customer = addSchemaObject(
            schema,
            customerKey,
            "Customer",
            new ObjectIds(customerToId)
        );
        addMorphism(schema, orderedToCustomer, ordered, customer, Min.ONE);

        var id = addSchemaObject(
            schema,
            idKey,
            "Id",
            ObjectIds.createValue()
        );
        addMorphism(schema, customerToId, customer, id, Min.ONE);
        //addMorphism(schema, orderToId, order, id, Min.ONE);
    }

    private void addAddress(SchemaCategory schema, SchemaObject order) {
        var address = addSchemaObject(
            schema,
            addressKey,
            "address",
            new ObjectIds(addressToNumber, addressToLabel)
        );
        addMorphism(schema, addressToOrder, address, order, Min.ONE);

        var label = addSchemaObject(
            schema,
            labelKey,
            "label",
            ObjectIds.createValue()
        );
        addMorphism(schema, addressToLabel, address, label, Min.ONE);

        var content = addSchemaObject(
            schema,
            contentKey,
            "content",
            ObjectIds.createGenerated()
        );
        addMorphism(schema, addressToContent, address, content, Min.ONE);
        
        var text = addSchemaObject(
            schema,
            textKey,
            "text",
            ObjectIds.createValue()
        );
        addMorphism(schema, contentToText, content, text, Min.ONE);
        
        var locale = addSchemaObject(
            schema,
            localeKey,
            "locale",
            ObjectIds.createValue()
        );
        addMorphism(schema, contentToLocale, content, locale, Min.ONE);
    }
    
    public InstanceCategory expectedInstance_order(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        expectedOrder(builder, "2043");
        expectedOrder(builder, "1653");
        
        return instance;
    }

    private DomainRow expectedOrder(TestInstanceCategoryBuilder builder, String orderNumber) {
        var order = builder.value(orderToNumber, orderNumber).object(orderKey);
        var number = builder.value(Signature.createEmpty(), orderNumber).object(numberKey);
        builder.morphism(orderToNumber, order, number);
        
        return order;
    }

    public InstanceCategory expectedInstance_nestedDoc(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        expectedOrder_nestedDoc(builder, "2043", "0", "hodnotaA", "hodnotaB", "hodnotaC");
        expectedOrder_nestedDoc(builder, "1653", "1", "hodnotaA2", "hodnotaB2", "hodnotaC2");
        
        return instance;
    }

    private void expectedOrder_nestedDoc(TestInstanceCategoryBuilder builder, String orderNumber, String uniqueId, String valueA, String valueB, String valueC) {
        var order = expectedOrder(builder, orderNumber);
        var nestedDoc = builder.value(Signature.createEmpty(), uniqueId).object(nestedDocKey);
        var propertyA = builder.value(Signature.createEmpty(), valueA).object(propertyAKey);
        var propertyB = builder.value(Signature.createEmpty(), valueB).object(propertyBKey);
        var propertyC = builder.value(Signature.createEmpty(), valueC).object(propertyCKey);
        
        builder.morphism(orderToNestedDoc, order, nestedDoc);
        builder.morphism(nestedDocToPropertyA, nestedDoc, propertyA);
        builder.morphism(nestedDocToPropertyB, nestedDoc, propertyB);
        builder.morphism(nestedDocToPropertyC, nestedDoc, propertyC);
    }

    public InstanceCategory expectedInstance_array(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        expectedOrder_array(builder, "2043", new String[]{ "123", "456", "789" });
        expectedOrder_array(builder, "1653", new String[]{ "123", "String456", "String789" });
        
        return instance;
    }

    private void expectedOrder_array(TestInstanceCategoryBuilder builder, String orderNumber, String[] array) {
        var order = expectedOrder(builder, orderNumber);
        for (String value : array) {
            var arrayItem = builder.value(Signature.createEmpty(), value).object(arrayKey);
            builder.morphism(arrayToOrder, arrayItem, order);
        }
    }

    public InstanceCategory expectedInstance_items(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);

        var order = expectedOrder(builder, "2043");
        expectedItem(builder, order, "2043", "123", "Toy", "125", "1");
        expectedItem(builder, order, "2043", "765", "Book", "199", "2");
        expectedItem(builder, order, "2043", "457", "Knife", "299", "7");
        expectedItem(builder, order, "2043", "734", "Doll", "350", "3");
        
        return instance;
    }

    private void expectedItem(TestInstanceCategoryBuilder builder, DomainRow order, String orderNumber, String pidValue, String pnameValue, String priceValue, String quantityValue) {
        var items = builder.value(itemsToNumber, orderNumber).value(itemsToPid, pidValue).object(itemsKey);
        builder.morphism(itemsToOrder, items, order);
        var quantity = builder.value(Signature.createEmpty(), quantityValue).object(quantityKey);
        builder.morphism(itemsToQuantity, items, quantity);

        var product = builder.value(productToPid, pidValue).object(productKey);
        builder.morphism(itemsToProduct, items, product);

        var pid = builder.value(Signature.createEmpty(), pidValue).object(pidKey);
        builder.morphism(productToPid, product, pid);
        // Empty morphisms are needed because of string comparison of actual and expected instance categories.
        builder.morphism(itemsToPid);
        var pname = builder.value(Signature.createEmpty(), pnameValue).object(pnameKey);
        builder.morphism(productToPname, product, pname);
        builder.morphism(itemsToPname);
        var price = builder.value(Signature.createEmpty(), priceValue).object(priceKey);
        builder.morphism(productToPrice, product, price);
        builder.morphism(itemsToPrice);
    }

    public InstanceCategory expectedInstance_contact(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        var order1 = expectedOrder(builder, "2043");
        addExpectedContact(builder, order1, "2043", "anna@seznam.cz", "email");
        addExpectedContact(builder, order1, "2043", "+420777123456", "cellphone");
        
        var order2 = expectedOrder(builder, "1653");
        addExpectedContact(builder, order2, "1653", "skype123", "skype");
        addExpectedContact(builder, order2, "1653", "+420123456789", "cellphone");
        
        return instance;
    }

    private void addExpectedContact(TestInstanceCategoryBuilder builder, DomainRow order, String numberValue, String valueValue, String nameValue) {
        var contact = builder.value(contactToNumber, numberValue).value(contactToValue, valueValue).value(contactToName, nameValue).object(contactKey);
        var value = builder.value(Signature.createEmpty(), valueValue).object(valueKey);
        var name = builder.value(Signature.createEmpty(), nameValue).object(nameKey);
        var type = builder.value(typeToName, nameValue).object(typeKey);
        
        builder.morphism(contactToValue, contact, value);
        builder.morphism(contactToType, contact, type);
        builder.morphism(typeToName, type, name);
        builder.morphism(contactToOrder, contact, order);
    }

    public InstanceCategory expectedInstance_ordered(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        expectedOrder_ordered(builder, "2043", "1");
        expectedOrder_ordered(builder, "1653", "1");
        
        return instance;
    }

    public DomainRow expectedOrder_ordered(TestInstanceCategoryBuilder builder, String orderNumber, String customerId) {
        var order = expectedOrder(builder, orderNumber);
        var id = builder.value(Signature.createEmpty(), customerId).object(idKey);
        var customer = builder.value(customerToId, customerId).object(customerKey);
        var ordered = builder.value(orderedToId, customerId).value(orderedToNumber, orderNumber).object(orderedKey);

        builder.morphism(orderedToOrder, ordered, order);
        builder.morphism(orderedToCustomer, ordered, customer);
        builder.morphism(customerToId, customer, id);

        return order;
    }

    public InstanceCategory expectedInstance_nestedDocMissingSimple(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        var order1 = expectedOrder(builder, "2043");
        var nestedDoc1 = builder.value(Signature.createEmpty(), "0").object(nestedDocKey);
        var propertyA1 = builder.value(Signature.createEmpty(), "hodnotaA").object(propertyAKey);
        var propertyC1 = builder.value(Signature.createEmpty(), "hodnotaC").object(propertyCKey);
        
        builder.morphism(orderToNestedDoc, order1, nestedDoc1);
        builder.morphism(nestedDocToPropertyA, nestedDoc1, propertyA1);
        builder.morphism(nestedDocToPropertyC, nestedDoc1, propertyC1);
        
        var order2 = expectedOrder(builder, "1653");
        var nestedDoc2 = builder.value(Signature.createEmpty(), "1").object(nestedDocKey);
        var propertyA2 = builder.value(Signature.createEmpty(), "hodnotaA2").object(propertyAKey);
        var propertyC2 = builder.value(Signature.createEmpty(), "hodnotaC2").object(propertyCKey);
        
        builder.morphism(orderToNestedDoc, order2, nestedDoc2);
        builder.morphism(nestedDocToPropertyA, nestedDoc2, propertyA2);
        builder.morphism(nestedDocToPropertyC, nestedDoc2, propertyC2);
        
        return instance;
    }

    public InstanceCategory expectedInstance_nestedDocMissingComplex(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        expectedOrder(builder, "2043");
        expectedOrder(builder, "1653");
        
        return instance;
    }

    public InstanceCategory expectedInstance_itemsEmpty(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        expectedOrder(builder, "2043");
        
        return instance;
    }

    public InstanceCategory expectedInstance_address(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        var order1 = expectedOrder(builder, "2043");
        addExpectedAddress(builder, order1, "0", "2043", "city", "Praha", "cs");
        addExpectedAddress(builder, order1, "1", "2043", "country", "Czech republic", "en");

        var order2 = expectedOrder(builder, "1653");
        addExpectedAddress(builder, order2, "2", "1653", "location", "Praha", "cs");
        addExpectedAddress(builder, order2, "3", "1653", "country", "Česká republika", "cs");
        
        return instance;
    }

    private void addExpectedAddress(TestInstanceCategoryBuilder builder, DomainRow order, String uniqueId, String number, String label, String text, String locale) {
        var address = builder.value(addressToNumber, number).value(addressToLabel, label).object(addressKey);
        var labelRow = builder.value(Signature.createEmpty(), label).object(labelKey);
        var contentRow = builder.value(Signature.createEmpty(), uniqueId).object(contentKey);
        var textRow = builder.value(Signature.createEmpty(), text).object(textKey);
        var localeRow = builder.value(Signature.createEmpty(), locale).object(localeKey);
        
        builder.morphism(addressToLabel, address, labelRow);
        builder.morphism(addressToContent, address, contentRow);
        builder.morphism(contentToText, contentRow, textRow);
        builder.morphism(contentToLocale, contentRow, localeRow);
        builder.morphism(addressToOrder, address, order);
    }

    public InstanceCategory expectedInstance_itemsMissing(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        var order = expectedOrder(builder, "2043");
        expectedItemMissing(builder, order, "2043", "123", "Toy", "125", null);
        expectedItemMissing(builder, order, "2043", "456", null, "500", "4");
        expectedItemMissing(builder, order, "2043", "765", "Book", null, "2");
        expectedItemMissing(builder, order, "2043", "457", null, "299", "7");
        expectedItemMissing(builder, order, "2043", "734", null, "350", "3");
        
        return instance;
    }

    private void expectedItemMissing(TestInstanceCategoryBuilder builder, DomainRow order, String orderNumber, String pidValue, String pnameValue, String priceValue, String quantityValue) {
        var items = builder.value(itemsToNumber, orderNumber).value(itemsToPid, pidValue).object(itemsKey);
        builder.morphism(itemsToOrder, items, order);

        if (quantityValue != null) {
            var quantity = builder.value(Signature.createEmpty(), quantityValue).object(quantityKey);
            builder.morphism(itemsToQuantity, items, quantity);
        }

        var product = builder.value(productToPid, pidValue).object(productKey);
        builder.morphism(itemsToProduct, items, product);

        if (pidValue != null) {
            var pid = builder.value(Signature.createEmpty(), pidValue).object(pidKey);
            builder.morphism(productToPid, product, pid);
            builder.morphism(itemsToPid);
        }

        if (pnameValue != null) {
            var pname = builder.value(Signature.createEmpty(), pnameValue).object(pnameKey);
            builder.morphism(productToPname, product, pname);
            builder.morphism(itemsToPname);
        }

        if (priceValue != null) {
            var price = builder.value(Signature.createEmpty(), priceValue).object(priceKey);
            builder.morphism(productToPrice, product, price);
            builder.morphism(itemsToPrice);
        }
    }

    public InstanceCategory expectedInstance_selfIdentifier(SchemaCategory schema) {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new TestInstanceCategoryBuilder(instance);
        
        var order1 = builder.value(Signature.createEmpty(), "#2043").object(orderKey);
        var number1 = builder.value(Signature.createEmpty(), "2043").object(numberKey);
        builder.morphism(orderToNumber, order1, number1);

        var order2 = builder.value(Signature.createEmpty(), "#1653").object(orderKey);
        var number2 = builder.value(Signature.createEmpty(), "1653").object(numberKey);
        builder.morphism(orderToNumber, order2, number2);
        
        return instance;
    }

    public ComplexProperty path_orderRoot() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("number", orderToNumber)
        );
    }

    public ComplexProperty path_nestedDoc() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("number", orderToNumber),
            ComplexProperty.create("nested", orderToNestedDoc,
                new SimpleProperty("propertyA", nestedDocToPropertyA),
                new SimpleProperty("propertyB", nestedDocToPropertyB),
                new SimpleProperty("propertyC", nestedDocToPropertyC)
            )
        );
    }

    public ComplexProperty path_array() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("number", orderToNumber),
            new SimpleProperty("array", arrayToOrder.dual())
        );
    }

    public ComplexProperty path_items() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("number", orderToNumber),
            ComplexProperty.create("items", itemsToOrder.dual(),
                new SimpleProperty("productId", itemsToPid),
                new SimpleProperty("name", itemsToPname),
                new SimpleProperty("price", itemsToPrice),
                new SimpleProperty("quantity", itemsToQuantity)
            )
        );
    }

    public ComplexProperty path_contact() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("number", orderToNumber),
            ComplexProperty.create("contact", contactToOrder.dual(),
                new SimpleProperty(contactToName, contactToValue)
            )
        );
    }

    public ComplexProperty path_ordered() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            ComplexProperty.createAuxiliary(new StaticName("_id"),
                new SimpleProperty("customer", orderToCustomer),
                new SimpleProperty("number", orderToNumber)
            )
        );
    }

    public ComplexProperty path_address() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("number", orderToNumber),
                ComplexProperty.create("address", addressToOrder.dual(),
                ComplexProperty.create(addressToLabel, addressToContent,
                    new SimpleProperty("text", contentToText),
                    new SimpleProperty("locale", contentToLocale)
                )
            )
        );
    }

    public ComplexProperty path_contactRoot() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("value", contactToValue),
            new SimpleProperty("name", contactToName),
            new SimpleProperty("order_number", contactToNumber)
        );
    }

    public ComplexProperty path_orderedRoot() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("customer_id", orderedToId),
            new SimpleProperty("order_number", orderedToNumber)
        );
    }

    public ComplexProperty path_customerRoot() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("id", customerToId)
        );
    }

    public ComplexProperty path_orderV3Root() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("id", Signature.createEmpty()),
            new SimpleProperty("number", orderToNumber)
        );
    }

    public ComplexProperty path_neo4j_order() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("customer_id", orderToId),
            new SimpleProperty("number", orderToNumber)
        );
    }

    public ComplexProperty path_neo4j_items() {
        return ComplexProperty.createAuxiliary(StaticName.createAnonymous(),
            new SimpleProperty("quantity", itemsToQuantity),
            ComplexProperty.create("_from.order", itemsToOrder,
                new SimpleProperty("customer_id", orderToId)
            ),
            ComplexProperty.create("_to.product", itemsToProduct,
                new SimpleProperty("id", productToPid),
                new SimpleProperty("name", productToPname)
            )
        );
    }

}
