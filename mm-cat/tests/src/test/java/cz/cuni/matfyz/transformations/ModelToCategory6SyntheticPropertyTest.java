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
        
        System.out.println("# Schema Category");
		System.out.println(schema);
		return schema;
    }

    @Override
	protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var orderProperty = new ComplexProperty("", Signature.Null(),
            new ComplexProperty("_id", Signature.Null(),
                new SimpleProperty("customer", orderedToOrder.dual().concatenate(customerToOrdered.dual()).concatenate(customerToId)),
                new SimpleProperty("number", orderToNumber)
            )
        );
        
        // TODO upravit všude signature na null
        // TODO testSuite - vytvoření množiny testů, co se provede najednou
        // TODO assert - equals podle příkladů
        //      - upravit porovnávání řádků z domén
        //      - porovnání instančních objektů
        // další meetingy
        //    - 27 - 28, domluvit se dopředu
        //    - 4 ve 20:00
        
        System.out.println("# Access Path");
		System.out.println(orderProperty);
        return orderProperty;
	}
}
