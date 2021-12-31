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
public class ModelToCategory8MissingComplexTest extends ModelToCategoryExtendedBase
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategory8MissingComplexTest.class);
	
    @Override
    protected String getFileName()
    {
        return "8MissingComplexTest.json";
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
        var orderProperty = new ComplexProperty(Name.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("nested", orderToNestedDoc,
                new SimpleProperty("propertyA", nestedDocToPropertyA),
                new SimpleProperty("propertyB", nestedDocToPropertyB),
                new SimpleProperty("propertyC", nestedDocToPropertyB)
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
