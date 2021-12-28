package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory6SyntheticPropertyTest extends ModelToCategoryExtendedBase
{
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
}
