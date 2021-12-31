package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.Signature;
<<<<<<< HEAD
import cz.cuni.matfyz.core.instance.*;
=======
import cz.cuni.matfyz.core.instance.InstanceCategory;
>>>>>>> 66f706ae0835f5d7d324eaeb2884a9aa4c9b3376
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory6SyntheticPropertyTest extends ModelToCategoryExtendedBase
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategory6SyntheticPropertyTest.class);
	
    @Override
    protected int getDebugLevel()
    {
        //return 0;
        return 5;
    }
    
    @Override
    protected String getFileName()
    {
        return "6SyntheticPropertyTest.json";
    }
    
    @Override
    protected SchemaCategory buildSchemaCategoryScenario()
    {
        SchemaCategory schema = new SchemaCategory();
        var order = buildOrder(schema);
        addOrdered(schema, order);
        
		return schema;
    }

    @Override
	protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var orderProperty = new ComplexProperty(Name.Anonymous(), Signature.Null(),
            new ComplexProperty("_id", Signature.Null(),
                new SimpleProperty("customer", orderedToOrder.dual().concatenate(customerToOrdered.dual()).concatenate(customerToId)),
                new SimpleProperty("number", orderToNumber)
            )
        );
        
        return orderProperty;
	}
<<<<<<< HEAD
    
    @Override
    protected InstanceCategory buildExpectedInstanceCategory(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        var order1 = builder.value(orderToNumber, "2043").object(orderKey);
        var number1 = builder.value(Signature.Empty(), "2043").object(numberKey);
        builder.morphism(orderToNumber, order1, number1);
        var id1 = builder.value(Signature.Empty(), "1").object(idKey);
        builder.morphism(orderToId, order1, id1);
        
        var order2 = builder.value(orderToNumber, "1653").object(orderKey);
        var number2 = builder.value(Signature.Empty(), "1653").object(numberKey);
        builder.morphism(orderToNumber, order2, number2);
        var id2 = builder.value(Signature.Empty(), "1").object(idKey);
        builder.morphism(orderToId, order2, id2);
        
        return instance;
    }
=======

	@Override
	protected InstanceCategory buildExpectedInstanceCategory(SchemaCategory schema) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	@Test
	public void execute() throws Exception {
		super.testAlgorithm();
	}
>>>>>>> 66f706ae0835f5d7d324eaeb2884a9aa4c9b3376
}
