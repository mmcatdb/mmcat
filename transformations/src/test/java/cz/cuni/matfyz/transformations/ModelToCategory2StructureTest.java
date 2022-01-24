package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.ActiveDomainRow;
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
public class ModelToCategory2StructureTest extends ModelToCategoryExtendedBase
{
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategory2StructureTest.class);
	
    @Override
    protected String getFileName()
    {
        return "2StructureTest.json";
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
        var nestedDoc1 = buildExpectedNestedDocInstance(builder, "0", "hodnotaA", "hodnotaB", "hodnotaC");
        
        builder.morphism(orderToNumber, order1, number1);
        builder.morphism(orderToNestedDoc, order1, nestedDoc1);
        
        var order2 = builder.value(orderToNumber, "1653").object(orderKey);
        var number2 = builder.value(Signature.Empty(), "1653").object(numberKey);
        var nestedDoc2 = buildExpectedNestedDocInstance(builder, "1", "hodnotaA2", "hodnotaB2", "hodnotaC2");
        
        builder.morphism(orderToNumber, order2, number2);
        builder.morphism(orderToNestedDoc, order2, nestedDoc2);
        
        return instance;
    }

    private ActiveDomainRow buildExpectedNestedDocInstance(SimpleInstanceCategoryBuilder builder, String uniqueId, String valueA, String valueB, String valueC)
    {
        var nestedDoc = builder.value(Signature.Empty(), uniqueId).object(nestedDocKey);
        var propertyA = builder.value(Signature.Empty(), valueA).object(propertyAKey);
        var propertyB = builder.value(Signature.Empty(), valueB).object(propertyBKey);
        var propertyC = builder.value(Signature.Empty(), valueC).object(propertyCKey);
        
        builder.morphism(nestedDocToPropertyA, nestedDoc, propertyA);
        builder.morphism(nestedDocToPropertyB, nestedDoc, propertyB);
        builder.morphism(nestedDocToPropertyC, nestedDoc, propertyC);

        return nestedDoc;
    }
	
	@Test
	public void execute()
    {
		super.testAlgorithm();
	}
}
