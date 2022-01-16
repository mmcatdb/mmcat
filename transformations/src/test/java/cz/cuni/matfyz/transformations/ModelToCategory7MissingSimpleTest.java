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
public class ModelToCategory7MissingSimpleTest extends ModelToCategoryExtendedBase
{
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategory7MissingSimpleTest.class);
	
    @Override
    protected String getFileName()
    {
        return "7MissingSimpleTest.json";
    }
    
    @Override
    protected SchemaCategory buildSchemaCategoryScenario()
    {
        SchemaCategory schema = new SchemaCategory();
        var order = buildOrder(schema);
        addNestedDoc(schema, order);
        
		return schema;
    }

    @Override
	protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var orderProperty = new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("nested", orderToNestedDoc,
                new SimpleProperty("propertyA", nestedDocToPropertyA),
                new SimpleProperty("propertyB", nestedDocToPropertyB),
                new SimpleProperty("propertyC", nestedDocToPropertyC)
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
        var nestedDoc1 = builder.value(Signature.Empty(), "0").object(nestedDocKey);
        var propertyA1 = builder.value(Signature.Empty(), "hodnotaA").object(propertyAKey);
        //var propertyB1 = builder.value(Signature.Empty(), "hodnotaB").object(propertyBKey);
        var propertyC1 = builder.value(Signature.Empty(), "hodnotaC").object(propertyCKey);
        
        builder.morphism(orderToNumber, order1, number1);
        builder.morphism(orderToNestedDoc, order1, nestedDoc1);
        builder.morphism(nestedDocToPropertyA, nestedDoc1, propertyA1);
        //builder.morphism(nestedDocToPropertyB, nestedDoc1, propertyB1);
        builder.morphism(nestedDocToPropertyC, nestedDoc1, propertyC1);
        
        var order2 = builder.value(orderToNumber, "1653").object(orderKey);
        var number2 = builder.value(Signature.Empty(), "1653").object(numberKey);
        var nestedDoc2 = builder.value(Signature.Empty(), "1").object(nestedDocKey);
        var propertyA2 = builder.value(Signature.Empty(), "hodnotaA2").object(propertyAKey);
        var propertyC2 = builder.value(Signature.Empty(), "hodnotaC2").object(propertyCKey);
        
        builder.morphism(orderToNumber, order2, number2);
        builder.morphism(orderToNestedDoc, order2, nestedDoc2);
        builder.morphism(nestedDocToPropertyA, nestedDoc2, propertyA2);
        builder.morphism(nestedDocToPropertyC, nestedDoc2, propertyC2);
        
        return instance;
    }
	
	@Test
	public void execute()
    {
		super.testAlgorithm();
	}
}
