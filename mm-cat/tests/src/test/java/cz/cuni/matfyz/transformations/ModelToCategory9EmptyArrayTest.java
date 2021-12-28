package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory9EmptyArrayTest extends ModelToCategoryExtendedBase
{
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
}
