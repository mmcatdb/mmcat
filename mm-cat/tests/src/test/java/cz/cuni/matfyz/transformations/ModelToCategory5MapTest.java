package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory5MapTest extends ModelToCategoryExtendedBase
{
    @Override
    protected String getFileName()
    {
        return "5MapTest.json";
    }
    
    @Override
    protected SchemaCategory buildSchemaCategoryScenario()
    {
        SchemaCategory schema = new SchemaCategory();
        var order = buildOrder(schema);
        addContact(schema, order);
        
        System.out.println("# Schema Category");
		System.out.println(schema);
		return schema;
    }

    @Override
	protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var orderProperty = new ComplexProperty("", Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("contact", orderToContact,
                new SimpleProperty(contactToType.concatenate(typeToName), contactToValue)
            )
        );
        
        System.out.println("# Access Path");
		System.out.println(orderProperty);
        return orderProperty;
	}
}
