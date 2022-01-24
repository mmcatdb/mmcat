package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory1BasicTest extends ModelToCategoryExtendedBase
{
//	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategory1BasicTest.class);
	
    @Override
    protected String getFileName()
    {
        return "1BasicTest.json";
    }
	
    @Override
    protected SchemaCategory buildSchemaCategoryScenario()
    {
        SchemaCategory schema = new SchemaCategory();
        buildOrder(schema);
        
		return schema;
    }

    @Override
	protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var orderProperty = new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber)
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
        
        var order2 = builder.value(orderToNumber, "1653").object(orderKey);
        var number2 = builder.value(Signature.Empty(), "1653").object(numberKey);
        
        builder.morphism(orderToNumber, order2, number2);
        
        return instance;
    }
	
	@Test
	public void execute()
    {
		super.testAlgorithm();
	}
}
