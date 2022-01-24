package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory4ComplexArrayTest extends ModelToCategoryExtendedBase
{
//	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategory4ComplexArrayTest.class);
	
    @Override
    protected String getFileName()
    {
        return "4ComplexArrayTest.json";
    }

    @Override
    protected int getDebugLevel()
    {
        return 0;
        //return 5;
    }
    
    @Override
    protected SchemaCategory buildSchemaCategoryScenario()
    {
        SchemaCategory schema = new SchemaCategory();
        var order = buildOrder(schema);
        addItems(schema, order, schema.keyToObject(numberKey));
        
		return schema;
    }

    @Override
	protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var orderProperty = new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("items", orderToItems,
                new SimpleProperty("productId", itemsToPid),
                new SimpleProperty("name", itemsToPname),
                new SimpleProperty("price", itemsToPrice),
                new SimpleProperty("quantity", itemsToQuantity)
            )
        );
        
        return orderProperty;
	}
    
    @Override
    protected InstanceCategory buildExpectedInstanceCategory(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        var order1 = builder.value(orderToNumber, "2043").object(orderKey);
        var number1 = builder.value(Signature.Empty(), "2043").object(numberKey);
        builder.morphism(orderToNumber, order1, number1);
        
        var items1 = buildExpectedItemInstance(builder, "2043", "123", "Toy", "125", "1");
        var items2 = buildExpectedItemInstance(builder, "2043", "765", "Book", "199", "2");
        var items3 = buildExpectedItemInstance(builder, "2043", "457", "Knife", "299", "7");
        var items4 = buildExpectedItemInstance(builder, "2043", "734", "Doll", "350", "3");
        
        builder.morphism(orderToItems, order1, items1);
        builder.morphism(orderToItems, order1, items2);
        builder.morphism(orderToItems, order1, items3);
        builder.morphism(orderToItems, order1, items4);
        
        return instance;
    }
    
    private ActiveDomainRow buildExpectedItemInstance(SimpleInstanceCategoryBuilder builder, String orderNumber, String pidValue, String pnameValue, String priceValue, String quantityValue)
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
        
        return items;
    }
	
	@Test
	public void execute()
    {
		super.testAlgorithm();
	}
	
}
