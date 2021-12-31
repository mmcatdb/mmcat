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
public class ModelToCategory3SimpleArrayTest extends ModelToCategoryExtendedBase
{
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategory3SimpleArrayTest.class);
	
    @Override
    protected String getFileName()
    {
        return "3SimpleArrayTest.json";
    }
    
    @Override
    protected SchemaCategory buildSchemaCategoryScenario()
    {
        SchemaCategory schema = new SchemaCategory();
        var order = buildOrder(schema);
        
        var array = createSchemaObject(
            arrayKey,
            "Array",
            Id.Empty()
        );
        schema.addObject(array);
        addMorphismWithDual(schema, orderToArray, order, array);
        
		return schema;
    }

    @Override
	protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var orderProperty = new ComplexProperty(Name.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new SimpleProperty("array", orderToArray)
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
        var array1 = builder.value(Signature.Empty(), "123").object(arrayKey);
        var array2 = builder.value(Signature.Empty(), "456").object(arrayKey);
        var array3 = builder.value(Signature.Empty(), "789").object(arrayKey);
        
        builder.morphism(orderToNumber, order1, number1);
        builder.morphism(orderToArray, order1, array1);
        builder.morphism(orderToArray, order1, array2);
        builder.morphism(orderToArray, order1, array3);
        
        var order2 = builder.value(orderToNumber, "1653").object(orderKey);
        var number2 = builder.value(Signature.Empty(), "1653").object(numberKey);
        var array4 = builder.value(Signature.Empty(), "String456").object(arrayKey);
        var array5 = builder.value(Signature.Empty(), "String789").object(arrayKey);
        
        builder.morphism(orderToNumber, order2, number2);
        builder.morphism(orderToArray, order2, array1);
        builder.morphism(orderToArray, order2, array4);
        builder.morphism(orderToArray, order2, array5);
        
        return instance;
    }
	
	@Test
	public void execute() throws Exception {
		super.testAlgorithm();
	}
	
}
