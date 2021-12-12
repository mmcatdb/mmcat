package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.category.*;
import cz.cuni.matfyz.wrapperDummy.DummyPullWrapper;

import java.util.*;

/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory1BasicTest extends ModelToCategoryBase
{
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
        
        System.out.println("# Schema Category");
		System.out.println(schema);
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
        
        System.out.println("# Access Path");
		System.out.println(order);
		return order;
	}

    @Override
	protected ForestOfRecords buildForestOfRecords(ComplexProperty path) throws Exception
    {
		DummyPullWrapper wrapper = new DummyPullWrapper();
		ForestOfRecords forest = wrapper.pullForest("1BasicTest.json", path);
        
        System.out.println("# Forest of Records");
		System.out.println(forest);
		return forest;
	}
	
    @Override
	protected Mapping buildMapping(SchemaCategory schema, ComplexProperty path)
    {
		return new Mapping(schema.keyToObject(orderKey), path);
	}
}
