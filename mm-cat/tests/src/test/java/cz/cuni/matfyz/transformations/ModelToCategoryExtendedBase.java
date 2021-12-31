package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.category.*;
import cz.cuni.matfyz.wrapperDummy.DummyPullWrapper;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public abstract class ModelToCategoryExtendedBase extends ModelToCategoryBase
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategoryExtendedBase.class);
	
    protected abstract String getFileName();
    
    protected final Key customerKey = new Key(100);
    protected final Key idKey = new Key(101);
    protected final Key orderedKey = new Key(102);
    protected final Key orderKey = new Key(103);
    protected final Key numberKey = new Key(104);
    protected final Key arrayKey = new Key(105);
    protected final Key contactKey = new Key(106);
    protected final Key valueKey = new Key(107);
    protected final Key typeKey = new Key(108);
    protected final Key nameKey = new Key(109);
    protected final Key nestedDocKey = new Key(110);
    protected final Key propertyAKey = new Key(111);
    protected final Key propertyBKey = new Key(112);
    protected final Key propertyCKey = new Key(113);
    protected final Key itemsKey = new Key(114);
    protected final Key quantityKey = new Key(115);
    protected final Key productKey = new Key(116);
    protected final Key pidKey = new Key(117);
    protected final Key pnameKey = new Key(118);
    protected final Key priceKey = new Key(119);
    
    protected final Signature customerToId = new Signature(1);
    protected final Signature customerToOrdered = new Signature(2);
    protected final Signature orderedToOrder = new Signature(3);
    protected final Signature orderToNestedDoc = new Signature(4);
    protected final Signature nestedDocToPropertyA = new Signature(5);
    protected final Signature nestedDocToPropertyB = new Signature(6);
    protected final Signature nestedDocToPropertyC = new Signature(7);
    protected final Signature orderToItems = new Signature(8);
    protected final Signature itemsToProduct = new Signature(9);
    protected final Signature productToPid = new Signature(10);
    protected final Signature productToPrice = new Signature(11);
    protected final Signature productToPname = new Signature(12);
    protected final Signature itemsToQuantity = new Signature(13);
    protected final Signature orderToContact = new Signature(14);
    protected final Signature contactToType = new Signature(15);
    protected final Signature typeToName = new Signature(16);
    protected final Signature contactToValue = new Signature(17);
    protected final Signature orderToArray = new Signature(18);
    protected final Signature orderToNumber = new Signature(19);
    
    protected final Signature itemsToNumber = orderToItems.dual().concatenate(orderToNumber);
    protected final Signature itemsToPid = itemsToProduct.concatenate(productToPid);
    protected final Signature itemsToPname = itemsToProduct.concatenate(productToPname);
    protected final Signature itemsToPrice = itemsToProduct.concatenate(productToPrice);
    
    protected final Signature contactToNumber = orderToContact.dual().concatenate(orderToNumber);
    protected final Signature contactToName = contactToType.concatenate(typeToName);
    
    protected final Signature orderedToNumber = orderedToOrder.concatenate(orderToNumber);
    protected final Signature orderedToId = customerToOrdered.dual().concatenate(customerToId);
    protected final Signature orderToId = orderedToOrder.dual().concatenate(orderedToId);
    
    protected SchemaObject buildOrder(SchemaCategory schema)
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
    
    protected void addItems(SchemaCategory schema, SchemaObject order, SchemaObject number)
    {
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
    
    protected void addContact(SchemaCategory schema, SchemaObject order)
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
    
    protected void addNestedDoc(SchemaCategory schema, SchemaObject order)
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
    
    protected void addOrdered(SchemaCategory schema, SchemaObject order)
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
    
    protected SchemaObject createSchemaObject(Key key, String name, Id id)
    {
        return new SchemaObject(key, name, id, Set.of(id));
    }
    
    protected void addMorphismWithDual(SchemaCategory schema, Signature signature, SchemaObject dom, SchemaObject cod)
    {
        var morphism = new SchemaMorphism(signature, dom, cod, SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR);
        schema.addMorphism(morphism);
        schema.addMorphism(morphism.createDual(SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR));
    }

    @Override
	protected ForestOfRecords buildForestOfRecords(ComplexProperty path) throws Exception
    {
		DummyPullWrapper wrapper = new DummyPullWrapper();
		ForestOfRecords forest = wrapper.pullForest(getFileName(), path);
        
		return forest;
	}
	
    @Override
	protected Mapping buildMapping(SchemaCategory schema, ComplexProperty path)
    {
		return new Mapping(schema.keyToObject(orderKey), path);
	}
}
