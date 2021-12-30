package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory9EmptyArrayTest extends ModelToCategoryExtendedBase
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategory9EmptyArrayTest.class);
	
    @Override
    protected String getFileName()
    {
        return "9EmptyArrayTest.json";
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
        var orderProperty = new ComplexProperty(Name.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("items", orderToItems,
                new SimpleProperty("productId", itemsToProduct.concatenate(productToPid)),
                new SimpleProperty("name", itemsToProduct.concatenate(productToPname)),
                new SimpleProperty("price", itemsToProduct.concatenate(productToPrice)),
                new SimpleProperty("quantity", itemsToQuantity)
            )
        );
        
        return orderProperty;
	}

	@Override
	protected InstanceCategory buildExpectedInstanceCategory(SchemaCategory schema) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}
	
	@Test
	public void execute() throws Exception {
		super.testAlgorithm();
	}
}
