package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.category.*;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.wrapperDummy.DummyPullWrapper;

import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategoryPaperTest extends ModelToCategoryBase
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategoryPaperTest.class);
	
    private final Signature orderToOrders = new Signature(24);
    private final Signature ordersToCustomer = new Signature(21);
    private final Signature customerToCustomerId = new Signature(1);
    private final Signature orderToNumber = new Signature(25);
    private final Signature orderToContact = new Signature(27);
    private final Signature contactToValue = new Signature(33);
    private final Signature contactToType = new Signature(29);
    private final Signature typeToName = new Signature(31);
    private final Signature orderToItems = new Signature(35);
    private final Signature itemsToQuantity = new Signature(37);
    private final Signature itemsToProduct = new Signature(39);
    private final Signature productToId = new Signature(47);
    private final Signature productToName = new Signature(49);
    private final Signature productToPrice = new Signature(51);
    
    private final Signature orderToCustomerId = orderToOrders.concatenate(ordersToCustomer).concatenate(customerToCustomerId);
    private final Signature contactToContactName = contactToType.concatenate(typeToName);
    private final Signature itemsToId = itemsToProduct.concatenate(productToId);
    private final Signature itemsToName = itemsToProduct.concatenate(productToName);
    private final Signature itemsToPrice = itemsToProduct.concatenate(productToPrice);
    
    private final Key customerIdKey = new Key(101);
    private final Key customerKey = new Key(100);
    private final Key ordersKey = new Key(110);
    private final Key numberKey = new Key(112);
    private final Key orderKey = new Key(111);
    private final Key contactKey = new Key(113);
    private final Key valueKey = new Key(116);
    private final Key typeKey = new Key(114);
    private final Key contactNameKey = new Key(115);
    private final Key itemsKey = new Key(117);
    private final Key quantityKey = new Key(118);
    private final Key productKey = new Key(121);
    private final Key productIdKey = new Key(122);
    private final Key productNameKey = new Key(123);
    private final Key priceKey = new Key(124);
            
    @Override
    protected SchemaCategory buildSchemaCategoryScenario()
    {
        SchemaCategory schema = new SchemaCategory();
        
        Id orderId = new Id(orderToCustomerId, orderToNumber);
        var order = new SchemaObject(
            orderKey,
            "Order",
            orderId,
            Set.of(orderId)
        );
        schema.addObject(order);
        
        var customerId = new SchemaObject(
            customerIdKey,
            "Id",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(customerId);
        
        var orderToCustomerIdMorphism = new SchemaMorphism(orderToCustomerId, order, customerId, SchemaMorphism.Min.ONE, SchemaMorphism.Max.STAR);
        schema.addMorphism(orderToCustomerIdMorphism);
        schema.addMorphism(orderToCustomerIdMorphism.createDual(SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR));
        
        var number = new SchemaObject(
            numberKey,
            "Number",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(number);
        
        var orderToNumberMorphism = new SchemaMorphism(orderToNumber, order, number, SchemaMorphism.Min.ONE, SchemaMorphism.Max.STAR);
        schema.addMorphism(orderToNumberMorphism);
        schema.addMorphism(orderToNumberMorphism.createDual(SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR));

        // Contact
        var contact = new SchemaObject(
            contactKey,
            "Contact",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(contact);
        
        var orderToContactMorphism = new SchemaMorphism(orderToContact, order, contact, SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR);
        schema.addMorphism(orderToContactMorphism);
        schema.addMorphism(orderToContactMorphism.createDual(SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR));
        
        var value = new SchemaObject(
            valueKey,
            "Value",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(value);
        
        var contactToValueMorphism = new SchemaMorphism(contactToValue, contact, value, SchemaMorphism.Min.ONE, SchemaMorphism.Max.STAR);
        schema.addMorphism(contactToValueMorphism);
        schema.addMorphism(contactToValueMorphism.createDual(SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR));
        
        var contactName = new SchemaObject(
            contactNameKey,
            "Name",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(contactName);
        
        var contactToContactNameMorphism = new SchemaMorphism(contactToContactName, contact, contactName, SchemaMorphism.Min.ONE, SchemaMorphism.Max.STAR);
        schema.addMorphism(contactToContactNameMorphism);
        schema.addMorphism(contactToContactNameMorphism.createDual(SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR));
        
        System.out.println("# Schema Category");
		System.out.println(schema);
		return schema;
    }

    @Override
	protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var customerIdProperty = new SimpleProperty(new Name("customer"), new SimpleValue(orderToCustomerId));
        var numberProperty = new SimpleProperty(new Name("number"), new SimpleValue(orderToNumber));
        
        var idProperty = new ComplexProperty(
            new Name("_id"),
            null,
            customerIdProperty, numberProperty
        );
        
        var nameValueProperty = new SimpleProperty(new Name(contactToContactName), new SimpleValue(contactToValue));
        
        var contactProperty = new ComplexProperty(
            new Name("contact"),
            orderToContact,
            nameValueProperty
        );
        
        var orderProperty = new ComplexProperty(
            Name.Anonymous(),
            Signature.Empty(),
            idProperty, contactProperty
        );
        
        return orderProperty;
        
        /*
        var totalPriceValue = new SimpleValue(orderToTotalPrice);
        String totalPriceLabel = schema.keyToObject(totalPriceKey).label();
        var totalPrice = new SimpleProperty(new Name(totalPriceLabel), totalPriceValue);
        
        var addressValue = new SimpleValue(orderToAddress);
        String addressLabel = schema.keyToObject(addressKey).label();
        var address = new SimpleProperty(new Name(addressLabel), addressValue);
        
        
        String orderLabel = schema.keyToObject(orderKey).label();
        var order = new ComplexProperty(
            new Name(orderLabel),
            null,
            id, totalPrice, address
        );
        
        System.out.println("# Access Path");
		System.out.println(order);
		return order;
        */
	}

    @Override
	protected ForestOfRecords buildForestOfRecords(ComplexProperty path) throws Exception
    {
		DummyPullWrapper wrapper = new DummyPullWrapper();
		ForestOfRecords forest = wrapper.pullForest("paperTest.json", path);
        
        System.out.println("# Forest of Records");
		System.out.println(forest);
		return forest;
	}
	
    @Override
	protected Mapping buildMapping(SchemaCategory schema, ComplexProperty path)
    {
		return new Mapping(schema.keyToObject(orderKey), path);
	}

	@Override
	protected InstanceCategory buildExpectedInstanceCategory(SchemaCategory schema) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
}
