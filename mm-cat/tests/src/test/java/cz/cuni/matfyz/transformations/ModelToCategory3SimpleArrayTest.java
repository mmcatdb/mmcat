package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory3SimpleArrayTest extends ModelToCategoryExtendedBase
{
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
        
        System.out.println("# Schema Category");
		System.out.println(schema);
		return schema;
    }

    @Override
	protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var orderProperty = new ComplexProperty("", Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new SimpleProperty("array", orderToArray)
        );
        
        System.out.println("# Access Path");
		System.out.println(orderProperty);
        return orderProperty;
	}
}
