package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.*;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;

import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class TestData
{
    private final Key customerKey = new Key(100);
    private final Key idKey = new Key(101);
    private final Key orderedKey = new Key(102);
    private final Key orderKey = new Key(103);
    private final Key numberKey = new Key(104);
    private final Key arrayKey = new Key(105);
    private final Key contactKey = new Key(106);
    private final Key valueKey = new Key(107);
    private final Key typeKey = new Key(108);
    private final Key nameKey = new Key(109);
    private final Key nestedDocKey = new Key(110);
    private final Key propertyAKey = new Key(111);
    private final Key propertyBKey = new Key(112);
    private final Key propertyCKey = new Key(113);
    private final Key itemsKey = new Key(114);
    private final Key quantityKey = new Key(115);
    private final Key productKey = new Key(116);
    private final Key pidKey = new Key(117);
    private final Key pnameKey = new Key(118);
    private final Key priceKey = new Key(119);
    private final Key addressKey = new Key(120);
    private final Key labelKey = new Key(121);
    private final Key contentKey = new Key(122);
    private final Key textKey = new Key(123);
    private final Key localeKey = new Key(124);
    
    private final Signature customerToId = new Signature(1);
    private final Signature customerToOrdered = new Signature(2);
    private final Signature orderedToOrder = new Signature(3);
    private final Signature orderToNestedDoc = new Signature(4);
    private final Signature nestedDocToPropertyA = new Signature(5);
    private final Signature nestedDocToPropertyB = new Signature(6);
    private final Signature nestedDocToPropertyC = new Signature(7);
    private final Signature orderToItems = new Signature(8);
    private final Signature itemsToProduct = new Signature(9);
    private final Signature productToPid = new Signature(10);
    private final Signature productToPrice = new Signature(11);
    private final Signature productToPname = new Signature(12);
    private final Signature itemsToQuantity = new Signature(13);
    private final Signature orderToContact = new Signature(14);
    private final Signature contactToType = new Signature(15);
    private final Signature typeToName = new Signature(16);
    private final Signature contactToValue = new Signature(17);
    private final Signature orderToArray = new Signature(18);
    private final Signature orderToNumber = new Signature(19);
    private final Signature orderToAddress = new Signature(20);
    private final Signature addressToLabel = new Signature(21);
    private final Signature addressToContent = new Signature(22);
    private final Signature contentToText = new Signature(23);
    private final Signature contentToLocale = new Signature(24);
    
    private final Signature itemsToNumber = orderToItems.dual().concatenate(orderToNumber);
    private final Signature itemsToPid = itemsToProduct.concatenate(productToPid);
    private final Signature itemsToPname = itemsToProduct.concatenate(productToPname);
    private final Signature itemsToPrice = itemsToProduct.concatenate(productToPrice);
    
    private final Signature contactToNumber = orderToContact.dual().concatenate(orderToNumber);
    private final Signature contactToName = contactToType.concatenate(typeToName);
    
    private final Signature orderedToNumber = orderedToOrder.concatenate(orderToNumber);
    private final Signature orderedToId = customerToOrdered.dual().concatenate(customerToId);
    private final Signature orderToId = orderedToOrder.dual().concatenate(orderedToId);
    private final Signature orderToCustomer = orderedToOrder.dual().concatenate(customerToOrdered.dual()).concatenate(customerToId);
    
    private final Signature addressToNumber = orderToAddress.dual().concatenate(orderToNumber);

    public SchemaObject buildOrder(SchemaCategory schema)
    {
        var order = createSchemaObject(
            orderKey,
            "Order",
            new Id(orderToNumber)
        );
        schema.addObject(order);
        
        var number = createSchemaObject(
            numberKey,
            "Number",
            Id.Empty()
        );
        schema.addObject(number);
        addMorphismWithDual(schema, orderToNumber, order, number);
        
        return order;
    }

    public void addArray(SchemaCategory schema, SchemaObject order)
    {
        var array = createSchemaObject(
            arrayKey,
            "Array",
            Id.Empty()
        );
        schema.addObject(array);
        addMorphismWithDual(schema, orderToArray, order, array);
    }
    
    public void addItems(SchemaCategory schema, SchemaObject order)
    {
        var number = schema.keyToObject(numberKey);

        var items = createSchemaObject(
            itemsKey,
            "Items",
            new Id(itemsToNumber, itemsToPid)
        );
        schema.addObject(items);
        addMorphismWithDual(schema, orderToItems, order, items);
        addMorphismWithDual(schema, itemsToNumber, items, number);
        
        var quantity = createSchemaObject(
            quantityKey,
            "Quantity",
            Id.Empty()
        );
        schema.addObject(quantity);
        addMorphismWithDual(schema, itemsToQuantity, items, quantity);
        
        var product = createSchemaObject(
            productKey,
            "Product",
            new Id(productToPid)
        );
        schema.addObject(product);
        addMorphismWithDual(schema, itemsToProduct, items, product);
        
        var pid = createSchemaObject(
            pidKey,
            "Id",
            Id.Empty()
        );
        schema.addObject(pid);
        addMorphismWithDual(schema, productToPid, product, pid);
        addMorphismWithDual(schema, itemsToPid, items, pid);

        var price = createSchemaObject(
            priceKey,
            "Price",
            Id.Empty()
        );
        schema.addObject(price);
        addMorphismWithDual(schema, productToPrice, product, price);
        addMorphismWithDual(schema, itemsToPrice, items, price);

        var pname = createSchemaObject(
            pnameKey,
            "Name",
            Id.Empty()
        );
        schema.addObject(pname);
        addMorphismWithDual(schema, productToPname, product, pname);
        addMorphismWithDual(schema, itemsToPname, items, pname);
    }
    
    public void addContact(SchemaCategory schema, SchemaObject order)
    {
        var contact = createSchemaObject(
            contactKey,
            "Contact",
            new Id(contactToNumber, contactToValue, contactToName)
        );
        schema.addObject(contact);
        addMorphismWithDual(schema, orderToContact, order, contact);

        var value = createSchemaObject(
            valueKey,
            "Value",
            Id.Empty()
        );
        schema.addObject(value);
        addMorphismWithDual(schema, contactToValue, contact, value);

        var type = createSchemaObject(
            typeKey,
            "Type",
            new Id(typeToName)
        );
        schema.addObject(type);
        addMorphismWithDual(schema, contactToType, contact, type);

        var name = createSchemaObject(
            nameKey,
            "Name",
            Id.Empty()
        );
        schema.addObject(name);
        addMorphismWithDual(schema, typeToName, type, name);
        addMorphismWithDual(schema, contactToName, contact, name);
    }
    
    public void addNestedDoc(SchemaCategory schema, SchemaObject order)
    {   
        var nestedDoc = createSchemaObject(
            nestedDocKey,
            "NestedDoc",
            Id.Empty()
        );
        schema.addObject(nestedDoc);
        addMorphismWithDual(schema, orderToNestedDoc, order, nestedDoc);
        
        var propertyA = createSchemaObject(
            propertyAKey,
            "PropertyA",
            Id.Empty()
        );
        schema.addObject(propertyA);
        addMorphismWithDual(schema, nestedDocToPropertyA, nestedDoc, propertyA);

        var propertyB = createSchemaObject(
            propertyBKey,
            "PropertyB",
            Id.Empty()
        );
        schema.addObject(propertyB);
        addMorphismWithDual(schema, nestedDocToPropertyB, nestedDoc, propertyB);

        var propertyC = createSchemaObject(
            propertyCKey,
            "PropertyC",
            Id.Empty()
        );
        schema.addObject(propertyC);
        addMorphismWithDual(schema, nestedDocToPropertyC, nestedDoc, propertyC);
    }
    
    public void addOrdered(SchemaCategory schema, SchemaObject order)
    {
        var ordered = createSchemaObject(
            orderedKey,
            "Ordered",
            new Id(orderedToNumber, orderedToId)
        );
        schema.addObject(ordered);
        addMorphismWithDual(schema, orderedToOrder.dual(), order, ordered);

        var customer = createSchemaObject(
            customerKey,
            "Customer",
            new Id(customerToId)
        );
        schema.addObject(customer);
        addMorphismWithDual(schema, customerToOrdered.dual(), ordered, customer);

        var id = createSchemaObject(
            idKey,
            "Id",
            Id.Empty()
        );
        schema.addObject(id);
        addMorphismWithDual(schema, customerToId, customer, id);
        addMorphismWithDual(schema, orderToId, order, id);
    }

    public void addAddress(SchemaCategory schema, SchemaObject order)
    {
        var address = createSchemaObject(
            addressKey,
            "address",
            new Id(addressToNumber, addressToLabel)
        );
        schema.addObject(address);
        addMorphismWithDual(schema, orderToAddress, order, address);

        var label = createSchemaObject(
            labelKey,
            "label",
            Id.Empty()
        );
        schema.addObject(label);
        addMorphismWithDual(schema, addressToLabel, address, label);        

        var content = createSchemaObject(
            contentKey,
            "content",
            Id.Empty()
        );
        schema.addObject(content);
        addMorphismWithDual(schema, addressToContent, address, content);
        
        var text = createSchemaObject(
            textKey,
            "text",
            Id.Empty()
        );
        schema.addObject(text);
        addMorphismWithDual(schema, contentToText, content, text);
        
        var locale = new SchemaObject(
            localeKey,
            "locale",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(locale);
        addMorphismWithDual(schema, contentToLocale, content, locale);
    }
    
    private SchemaObject createSchemaObject(Key key, String name, Id id)
    {
        return new SchemaObject(key, name, id, Set.of(id));
    }
    
    private void addMorphismWithDual(SchemaCategory schema, Signature signature, SchemaObject dom, SchemaObject cod)
    {
        var morphism = new SchemaMorphism(signature, dom, cod, SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR);
        schema.addMorphism(morphism);
        schema.addMorphism(morphism.createDual(SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR));
    }
    
    private InstanceCategory buildInstanceScenario(SchemaCategory schema)
    {
        return new InstanceCategoryBuilder().setSchemaCategory(schema).build();
    }

    public InstanceCategory buildExpectedInstanceCategory_basic(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        buildExpectedOrder_basic(builder, "2043");
        buildExpectedOrder_basic(builder, "1653");
        
        return instance;
    }

    private ActiveDomainRow buildExpectedOrder_basic(SimpleInstanceCategoryBuilder builder, String orderNumber)
    {
        var order = builder.value(orderToNumber, orderNumber).object(orderKey);
        var number = builder.value(Signature.Empty(), orderNumber).object(numberKey);
        builder.morphism(orderToNumber, order, number);
        return order;
    }

    public InstanceCategory buildExpectedInstanceCategory_structure(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        buildExpectedOrder_structure(builder, "2043", "0", "hodnotaA", "hodnotaB", "hodnotaC");
        buildExpectedOrder_structure(builder, "1653", "1", "hodnotaA2", "hodnotaB2", "hodnotaC2");
        
        return instance;
    }

    private void buildExpectedOrder_structure(SimpleInstanceCategoryBuilder builder, String orderNumber, String uniqueId, String valueA, String valueB, String valueC)
    {
        var order = buildExpectedOrder_basic(builder, orderNumber);
        var nestedDoc = builder.value(Signature.Empty(), uniqueId).object(nestedDocKey);
        var propertyA = builder.value(Signature.Empty(), valueA).object(propertyAKey);
        var propertyB = builder.value(Signature.Empty(), valueB).object(propertyBKey);
        var propertyC = builder.value(Signature.Empty(), valueC).object(propertyCKey);
        
        builder.morphism(orderToNestedDoc, order, nestedDoc);
        builder.morphism(nestedDocToPropertyA, nestedDoc, propertyA);
        builder.morphism(nestedDocToPropertyB, nestedDoc, propertyB);
        builder.morphism(nestedDocToPropertyC, nestedDoc, propertyC);
    }

    public InstanceCategory buildExpectedInstanceCategory_simpleArray(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        buildExpectedOrder_simpleArray(builder, "2043", new String[]{ "123", "456", "789" });
        buildExpectedOrder_simpleArray(builder, "1653", new String[]{ "123", "String456", "String789" });
        
        return instance;
    }

    private void buildExpectedOrder_simpleArray(SimpleInstanceCategoryBuilder builder, String orderNumber, String[] array)
    {
        var order = buildExpectedOrder_basic(builder, orderNumber);
        for (String value : array)
        {
            var arrayItem = builder.value(Signature.Empty(), value).object(arrayKey);
            builder.morphism(orderToArray, order, arrayItem);
        }
    }

    public InstanceCategory buildExpectedInstanceCategory_complexArray(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);

        var order = buildExpectedOrder_basic(builder, "2043");
        buildExpectedItem_complexArray(builder, order, "2043", "123", "Toy", "125", "1");
        buildExpectedItem_complexArray(builder, order, "2043", "765", "Book", "199", "2");
        buildExpectedItem_complexArray(builder, order, "2043", "457", "Knife", "299", "7");
        buildExpectedItem_complexArray(builder, order, "2043", "734", "Doll", "350", "3");
        
        return instance;
    }

    private void buildExpectedItem_complexArray(SimpleInstanceCategoryBuilder builder, ActiveDomainRow order, String orderNumber, String pidValue, String pnameValue, String priceValue, String quantityValue)
    {
        var items = builder.value(itemsToNumber, orderNumber).value(itemsToPid, pidValue).object(itemsKey);
        var pid = builder.value(Signature.Empty(), pidValue).object(pidKey);
        var pname = builder.value(Signature.Empty(), pnameValue).object(pnameKey);
        var price = builder.value(Signature.Empty(), priceValue).object(priceKey);
        var quantity = builder.value(Signature.Empty(), quantityValue).object(quantityKey);
        
        builder.morphism(itemsToQuantity, items, quantity);
        builder.morphism(itemsToPid, items, pid);
        builder.morphism(itemsToPname, items, pname);
        builder.morphism(itemsToPrice, items, price);
        builder.morphism(orderToItems, order, items);
    }

    public InstanceCategory buildExpectedInstanceCategory_map(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        var order1 = buildExpectedOrder_basic(builder, "2043");
        addExpectedContactInstance_map(builder, order1, "2043", "anna@seznam.cz", "email");
        addExpectedContactInstance_map(builder, order1, "2043", "+420777123456", "cellphone");
        
        var order2 = buildExpectedOrder_basic(builder, "1653");
        addExpectedContactInstance_map(builder, order2, "1653", "skype123", "skype");
        addExpectedContactInstance_map(builder, order2, "1653", "+420123456789", "cellphone");
        
        return instance;
    }

    private void addExpectedContactInstance_map(SimpleInstanceCategoryBuilder builder, ActiveDomainRow order, String numberValue, String valueValue, String nameValue)
    {
        var contact = builder.value(contactToNumber, numberValue).value(contactToValue, valueValue).value(contactToName, nameValue).object(contactKey);
        var value = builder.value(Signature.Empty(), valueValue).object(valueKey);
        var name = builder.value(Signature.Empty(), nameValue).object(nameKey);
        
        builder.morphism(contactToValue, contact, value);
        builder.morphism(contactToName, contact, name);
        builder.morphism(orderToContact, order, contact);
    }

    public InstanceCategory buildExpectedInstanceCategory_syntheticProperty(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        buildExpectedOrder_syntheticProperty(builder, "2043", "1");
        buildExpectedOrder_syntheticProperty(builder, "1653", "1");
        
        return instance;
    }

    public ActiveDomainRow buildExpectedOrder_syntheticProperty(SimpleInstanceCategoryBuilder builder, String orderNumber, String customerId)
    {
        var order = buildExpectedOrder_basic(builder, orderNumber);
        var id = builder.value(Signature.Empty(), customerId).object(idKey);
        builder.morphism(orderToId, order, id);
        return order;
    }

    public InstanceCategory buildExpectedInstanceCategory_missingSimple(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        var order1 = buildExpectedOrder_basic(builder, "2043");
        var nestedDoc1 = builder.value(Signature.Empty(), "0").object(nestedDocKey);
        var propertyA1 = builder.value(Signature.Empty(), "hodnotaA").object(propertyAKey);
        var propertyC1 = builder.value(Signature.Empty(), "hodnotaC").object(propertyCKey);
        
        builder.morphism(orderToNestedDoc, order1, nestedDoc1);
        builder.morphism(nestedDocToPropertyA, nestedDoc1, propertyA1);
        builder.morphism(nestedDocToPropertyC, nestedDoc1, propertyC1);
        
        var order2 = buildExpectedOrder_basic(builder, "1653");
        var nestedDoc2 = builder.value(Signature.Empty(), "1").object(nestedDocKey);
        var propertyA2 = builder.value(Signature.Empty(), "hodnotaA2").object(propertyAKey);
        var propertyC2 = builder.value(Signature.Empty(), "hodnotaC2").object(propertyCKey);
        
        builder.morphism(orderToNestedDoc, order2, nestedDoc2);
        builder.morphism(nestedDocToPropertyA, nestedDoc2, propertyA2);
        builder.morphism(nestedDocToPropertyC, nestedDoc2, propertyC2);
        
        return instance;
    }

    public InstanceCategory buildExpectedInstanceCategory_missingComplex(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        buildExpectedOrder_basic(builder, "2043");
        buildExpectedOrder_basic(builder, "1653");
        
        return instance;
    }

    public InstanceCategory buildExpectedInstanceCategory_emptyArray(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        buildExpectedOrder_basic(builder, "2043");
        
        return instance;
    }

    public InstanceCategory buildExpectedInstanceCategory_complexMap(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        var order1 = buildExpectedOrder_basic(builder, "2043");
        addExpectedAddressInstance(builder, order1, "0", "2043", "city", "Praha", "cs");
        addExpectedAddressInstance(builder, order1, "1", "2043", "country", "Czech republic", "en");

        var order2 = buildExpectedOrder_basic(builder, "1653");
        addExpectedAddressInstance(builder, order2, "2", "1653", "location", "Praha", "cs");
        addExpectedAddressInstance(builder, order2, "3", "1653", "country", "Česká republika", "cs");
        
        return instance;
    }

    private void addExpectedAddressInstance(SimpleInstanceCategoryBuilder builder, ActiveDomainRow order, String uniqueId, String number, String label, String text, String locale)
    {
        var address = builder.value(addressToNumber, number).value(addressToLabel, label).object(addressKey);
        var labelRow = builder.value(Signature.Empty(), label).object(labelKey);
        var contentRow = builder.value(Signature.Empty(), uniqueId).object(contentKey);
        var textRow = builder.value(Signature.Empty(), text).object(textKey);
        var localeRow = builder.value(Signature.Empty(), locale).object(localeKey);
        
        builder.morphism(addressToLabel, address, labelRow);
        builder.morphism(addressToContent, address, contentRow);
        builder.morphism(contentToText, contentRow, textRow);
        builder.morphism(contentToLocale, contentRow, localeRow);
        builder.morphism(orderToAddress, order, address);
    }

    public ComplexProperty buildPath_order()
    {
        return new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber)
        );
    }

    public ComplexProperty buildPath_structure()
    {
        return new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("nested", orderToNestedDoc,
                new SimpleProperty("propertyA", nestedDocToPropertyA),
                new SimpleProperty("propertyB", nestedDocToPropertyB),
                new SimpleProperty("propertyC", nestedDocToPropertyC)
            )
        );
    }

    public ComplexProperty buildPath_array()
    {
        return new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new SimpleProperty("array", orderToArray)
        );
    }

    public ComplexProperty buildPath_items()
    {
        return new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("items", orderToItems,
                new SimpleProperty("productId", itemsToPid),
                new SimpleProperty("name", itemsToPname),
                new SimpleProperty("price", itemsToPrice),
                new SimpleProperty("quantity", itemsToQuantity)
            )
        );
    }

    public ComplexProperty buildPath_contact()
    {
        return new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("contact", orderToContact,
                new SimpleProperty(contactToName, contactToValue)
            )
        );
    }

    public ComplexProperty buildPath_ordered()
    {
        return new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new ComplexProperty("_id", Signature.Null(),
                new SimpleProperty("customer", orderToCustomer),
                new SimpleProperty("number", orderToNumber)
            )
        );
    }

    public ComplexProperty buildPath_address()
    {
        return new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("address", orderToAddress,
                new ComplexProperty(addressToLabel, addressToContent,
                    new SimpleProperty("text", contentToText),
                    new SimpleProperty("locale", contentToLocale)
                )
            )
        );
    }
}
