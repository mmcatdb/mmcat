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
public class ModelToCategory5MapTest extends ModelToCategoryExtendedBase
{
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategory5MapTest.class);
	
    @Override
    protected int getDebugLevel()
    {
        return 0;
        //return 5;
    }
    
    @Override
    protected String getFileName()
    {
        return "5MapTest.json";
    }
    
    @Override
    protected SchemaCategory buildSchemaCategoryScenario()
    {
        SchemaCategory schema = new SchemaCategory();
        var order = buildOrder(schema);
        addContact(schema, order);
        
		return schema;
    }

    @Override
	protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var orderProperty = new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("contact", orderToContact,
                new SimpleProperty(contactToName, contactToValue)
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
        
        var contact1 = buildExpectedContactInstance(builder, "2043", "anna@seznam.cz", "email");
        var contact2 = buildExpectedContactInstance(builder, "2043", "+420777123456", "cellphone");
        builder.morphism(orderToContact, order1, contact1);
        builder.morphism(orderToContact, order1, contact2);
        
        var order2 = builder.value(orderToNumber, "1653").object(orderKey);
        var number2 = builder.value(Signature.Empty(), "1653").object(numberKey);
        builder.morphism(orderToNumber, order2, number2);
        
        var contact3 = buildExpectedContactInstance(builder, "1653", "skype123", "skype");
        var contact4 = buildExpectedContactInstance(builder, "1653", "+420123456789", "cellphone");
        builder.morphism(orderToContact, order2, contact3);
        builder.morphism(orderToContact, order2, contact4);
        
        return instance;
    }
    
    private ActiveDomainRow buildExpectedContactInstance(SimpleInstanceCategoryBuilder builder, String numberValue, String valueValue, String nameValue)
    {
        var contact = builder.value(contactToNumber, numberValue).value(contactToValue, valueValue).value(contactToName, nameValue).object(contactKey);
        var value = builder.value(Signature.Empty(), valueValue).object(valueKey);
        var name = builder.value(Signature.Empty(), nameValue).object(nameKey);
        
        builder.morphism(contactToValue, contact, value);
        builder.morphism(contactToName, contact, name);
        
        return contact;
    }
	
	@Test
	public void execute()
    {
		super.testAlgorithm();
	}
}
