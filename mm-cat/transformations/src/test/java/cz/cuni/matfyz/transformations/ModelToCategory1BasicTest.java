package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.category.*;
import cz.cuni.matfyz.core.utils.Debug;
import cz.cuni.matfyz.wrapperDummy.DummyPullWrapper;
import java.nio.file.Paths;

import java.util.*;
import org.junit.jupiter.api.Test;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory1BasicTest extends ModelToCategoryBase
{
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategory1BasicTest.class);
	
    private final Signature orderToId = new Signature(1);
    private final Signature orderToTotalPrice = new Signature(2);
    private final Signature orderToAddress = new Signature(3);
    
    private final Key orderKey = new Key(100);
    private final Key idKey = new Key(101);
    private final Key totalPriceKey = new Key(102);
    private final Key addressKey = new Key(103);
            
    @Override
    protected SchemaCategory buildSchemaCategoryScenario()
    {
        SchemaCategory schema = new SchemaCategory();
        
        var order = new SchemaObject(
            orderKey,
            "order",
            //new Id(orderToId, orderToTotalPrice, orderToAddress),
            new Id(orderToId),
            Set.of(new Id(orderToId))
        );
        schema.addObject(order);
        
        var id = new SchemaObject(
            idKey,
            "_id",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(id);
        
        var orderToIdMorphism = new SchemaMorphism(orderToId, order, id, SchemaMorphism.Min.ONE, SchemaMorphism.Max.ONE);
        schema.addMorphism(orderToIdMorphism);
        schema.addMorphism(orderToIdMorphism.createDual(SchemaMorphism.Min.ONE, SchemaMorphism.Max.ONE));
        
        var totalPrice = new SchemaObject(
            totalPriceKey,
            "totalPrice",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(totalPrice);
        
        var orderToTotalPriceMorphism = new SchemaMorphism(orderToTotalPrice, order, totalPrice, SchemaMorphism.Min.ZERO, SchemaMorphism.Max.ONE);
        schema.addMorphism(orderToTotalPriceMorphism);
        schema.addMorphism(orderToTotalPriceMorphism.createDual(SchemaMorphism.Min.ONE, SchemaMorphism.Max.ONE));
        
        var address = new SchemaObject(
            addressKey,
            "address",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(address);
        
        var orderToAddressMorphism = new SchemaMorphism(orderToAddress, order, address, SchemaMorphism.Min.ZERO, SchemaMorphism.Max.ONE);
        schema.addMorphism(orderToAddressMorphism);
        schema.addMorphism(orderToAddressMorphism.createDual(SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR));
        
		return schema;
    }

    @Override
	protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        String idLabel = schema.keyToObject(idKey).label();
        String totalPriceLabel = schema.keyToObject(totalPriceKey).label();
        String addressLabel = schema.keyToObject(addressKey).label();
        
        String orderLabel = schema.keyToObject(orderKey).label();
        var order = new ComplexProperty(orderLabel, Signature.Empty(),
            new SimpleProperty(idLabel, orderToId),
            new SimpleProperty(totalPriceLabel, orderToTotalPrice),
            new SimpleProperty(addressLabel, orderToAddress)
        );
        
		return order;
	}

    @Override
	protected ForestOfRecords buildForestOfRecords(ComplexProperty path) throws Exception
    {
		DummyPullWrapper wrapper = new DummyPullWrapper();
        
        var url = ClassLoader.getSystemResource("1BasicTest.json");
        String fileName = Paths.get(url.toURI()).toAbsolutePath().toString();
		ForestOfRecords forest = wrapper.pullForest(fileName, path);
        
		return forest;
	}
	
    @Override
	protected Mapping buildMapping(SchemaCategory schema, ComplexProperty path)
    {
		return new Mapping(schema.keyToObject(orderKey), path);
	}
    
    @Override
    protected InstanceCategory buildExpectedInstanceCategory(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        var order1 = builder.value(orderToId, "1").object(orderKey);
        var order_id1 = builder.value(Signature.Empty(), "1").object(idKey);
        var order_totalPrice1 = builder.value(Signature.Empty(), "20").object(totalPriceKey);
        var order_address1 = builder.value(Signature.Empty(), "Street No., ZIP City").object(addressKey);
        
        builder.morphism(orderToId, order1, order_id1);
        builder.morphism(orderToTotalPrice, order1, order_totalPrice1);
        builder.morphism(orderToAddress, order1, order_address1);
        
        var order2 = builder.value(orderToId, "2").object(orderKey);
        var order_id2 = builder.value(Signature.Empty(), "2").object(idKey);
        var order_totalPrice2 = builder.value(Signature.Empty(), "40").object(totalPriceKey);
        var order_address2 = builder.value(Signature.Empty(), "Another address").object(addressKey);
        
        builder.morphism(orderToId, order2, order_id2);
        builder.morphism(orderToTotalPrice, order2, order_totalPrice2);
        builder.morphism(orderToAddress, order2, order_address2);
        
        return instance;
    }
	
	@Test
	public void execute() throws Exception {
		super.testAlgorithm();
	}
}
