package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory10ComplexMapTest extends ModelToCategoryExtendedBase
{
//	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategory10ComplexMapTest);

    @Override
    protected String getFileName()
    {
        return "10ComplexMapTest.json";
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
        addAddress(schema, order);
        
		return schema;
    }

    @Override
    protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var orderProperty = new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("number", orderToNumber),
            new ComplexProperty("address", orderToAddress,
                new ComplexProperty(addressToLabel, addressToContent,
                    new SimpleProperty("text", contentToText),
                    new SimpleProperty("locale", contentToLocale)
                )
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
        var order_number1 = builder.value(Signature.Empty(), "2043").object(numberKey);
        builder.morphism(orderToNumber, order1, order_number1);
        
        addExpectedAddressInstance(builder, order1, "0", "2043", "city", "Praha", "cs");
        addExpectedAddressInstance(builder, order1, "1", "2043", "country", "Czech republic", "en");

        var order2 = builder.value(orderToNumber, "1653").object(orderKey);
        var order_number2 = builder.value(Signature.Empty(), "1653").object(numberKey);
        builder.morphism(orderToNumber, order2, order_number2);

        addExpectedAddressInstance(builder, order2, "2", "1653", "location", "Praha", "cs");
        addExpectedAddressInstance(builder, order2, "3", "1653", "country", "Česká republika", "cs");
        
        return instance;
    }

    private void addExpectedAddressInstance(SimpleInstanceCategoryBuilder builder, ActiveDomainRow order, String uniqueId, String number, String label, String text, String locale)
    {
        var address = builder.value(addressToNumber, number).value(addressToLabel, label).object(addressKey);
        var labelRow = builder.value(Signature.Empty(), label).object(labelKey);
        var contentRow = builder.value(Signature.Empty(), uniqueId).object(contentKey);
        var textRow = builder.value(Signature.Empty(), text).object(textKey);
        var localeRow = builder.value(Signature.Empty(), locale).object(localeKey);
        
        builder.morphism(addressToLabel, address, labelRow);
        builder.morphism(addressToContent, address, contentRow);
        builder.morphism(contentToText, contentRow, textRow);
        builder.morphism(contentToLocale, contentRow, localeRow);
        builder.morphism(orderToAddress, order, address);
    }

    @Test
    public void execute()
    {
        super.testAlgorithm();
    }
}
