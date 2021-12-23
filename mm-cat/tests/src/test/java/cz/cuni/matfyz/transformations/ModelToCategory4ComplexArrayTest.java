package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory4ComplexArrayTest extends ModelToCategoryExtendedBase
{
    @Override
    protected String getFileName()
    {
        return "4ComplexArrayTest.json";
    }
    
    @Override
    protected SchemaCategory buildSchemaCategoryScenario()
    {
        SchemaCategory schema = new SchemaCategory();
        var order = buildOrder(schema);
        addItems(schema, order, schema.keyToObject(numberKey));
        
        System.out.println("# Schema Category");
		System.out.println(schema);
		return schema;
    }

    @Override
	protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var orderProperty = new ComplexProperty("", Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("items", orderToItems,
                new SimpleProperty("productId", itemsToPid),
                new SimpleProperty("name", itemsToPname),
                new SimpleProperty("price", itemsToPrice),
                new SimpleProperty("quantity", itemsToQuantity)
            )
        );
        
        System.out.println("# Access Path");
		System.out.println(orderProperty);
        return orderProperty;
	}
}
