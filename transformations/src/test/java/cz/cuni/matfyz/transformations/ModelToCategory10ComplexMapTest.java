package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.schema.*;
import cz.cuni.matfyz.core.category.*;

import org.junit.jupiter.api.Test;
import java.util.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;


/**
 *
 * @author jachymb.bartik
 */
public class ModelToCategory10ComplexMapTest extends ModelToCategoryBase
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
	
    private final Key rootKey = new Key(100);
    private final Key idKey = new Key(101);
    private final Key innerKey = new Key(102);
    private final Key nameKey = new Key(103);
    private final Key itemKey = new Key(104);
    private final Key contentKey = new Key(105);
    private final Key languageKey = new Key(106);

    private final Signature rootToId = new Signature(1);
    private final Signature rootToInner = new Signature(2);
    private final Signature innerToName = new Signature(3);
    private final Signature innerToItem = new Signature(4);
    private final Signature itemToContent = new Signature(5);
    private final Signature itemToLanguage = new Signature(6);

    private final Signature innerToId = rootToInner.dual().concatenate(rootToId);
            
    @Override
    protected SchemaCategory buildSchemaCategoryScenario()
    {
        SchemaCategory schema = new SchemaCategory();
        
        var root = new SchemaObject(
            rootKey,
            "root",
            new Id(rootToId),
            Set.of(new Id(rootToId))
        );
        schema.addObject(root);
        
        var id = new SchemaObject(
            idKey,
            "id",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(id);
        
        var rootToIdMorphism = new SchemaMorphism(rootToId, root, id, SchemaMorphism.Min.ONE, SchemaMorphism.Max.ONE);
        schema.addMorphism(rootToIdMorphism);
        schema.addMorphism(rootToIdMorphism.createDual(SchemaMorphism.Min.ONE, SchemaMorphism.Max.ONE));
        
        var innerId = new Id(innerToId, innerToName);
        var inner = new SchemaObject(
            innerKey,
            "inner",
            innerId,
            Set.of(innerId)
        );
        schema.addObject(inner);

        var rootToInnerMorphism = new SchemaMorphism(rootToInner, root, inner, SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR);
        schema.addMorphism(rootToInnerMorphism);
        schema.addMorphism(rootToInnerMorphism.createDual(SchemaMorphism.Min.ONE, SchemaMorphism.Max.ONE));

        var name = new SchemaObject(
            nameKey,
            "name",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(name);
        
        var innerToNameMorphism = new SchemaMorphism(innerToName, inner, name, SchemaMorphism.Min.ONE, SchemaMorphism.Max.ONE);
        schema.addMorphism(innerToNameMorphism);
        schema.addMorphism(innerToNameMorphism.createDual(SchemaMorphism.Min.ONE, SchemaMorphism.Max.STAR));

        var item = new SchemaObject(
            itemKey,
            "item",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(item);
        
        var innerToItemMorphism = new SchemaMorphism(innerToItem, inner, item, SchemaMorphism.Min.ONE, SchemaMorphism.Max.STAR);
        schema.addMorphism(innerToItemMorphism);
        schema.addMorphism(innerToItemMorphism.createDual(SchemaMorphism.Min.ONE, SchemaMorphism.Max.ONE));
        
        var content = new SchemaObject(
            contentKey,
            "content",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(content);
        
        var itemToContentMorphism = new SchemaMorphism(itemToContent, item, content, SchemaMorphism.Min.ONE, SchemaMorphism.Max.ONE);
        schema.addMorphism(itemToContentMorphism);
        schema.addMorphism(itemToContentMorphism.createDual(SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR));

        var language = new SchemaObject(
            languageKey,
            "language",
            Id.Empty(),
            Set.of(Id.Empty())
        );
        schema.addObject(language);
        
        var itemToLanguageMorphism = new SchemaMorphism(itemToLanguage, item, language, SchemaMorphism.Min.ONE, SchemaMorphism.Max.ONE);
        schema.addMorphism(itemToLanguageMorphism);
        schema.addMorphism(itemToLanguageMorphism.createDual(SchemaMorphism.Min.ZERO, SchemaMorphism.Max.STAR));

        return schema;
    }

    @Override
    protected ComplexProperty buildComplexPropertyPath(SchemaCategory schema)
    {
        var rootProperty = new ComplexProperty(StaticName.Anonymous(), Signature.Null(),
            new SimpleProperty("id", rootToId),
            new ComplexProperty("inner", rootToInner,
                new ComplexProperty(innerToName, innerToItem,
                    new SimpleProperty("content", itemToContent),
                    new SimpleProperty("language", itemToLanguage)
                )
            )
        );
        
        return rootProperty;
    }

    @Override
    protected Mapping buildMapping(SchemaCategory schema, ComplexProperty path)
    {
        return new Mapping(schema.keyToObject(rootKey), path);
    }

    @Override
    protected InstanceCategory buildExpectedInstanceCategory(SchemaCategory schema)
    {
        InstanceCategory instance = buildInstanceScenario(schema);
        var builder = new SimpleInstanceCategoryBuilder(instance);
        
        var root1 = builder.value(rootToId, "1").object(rootKey);
        var root_id1 = builder.value(Signature.Empty(), "1").object(idKey);
        
        var inner1 = buildExpectedInnerInstance(builder, "0", "1", "city", "Praha", "cs");
        var inner2 = buildExpectedInnerInstance(builder, "1", "1", "country", "Czech republic", "en");
        
        builder.morphism(rootToId, root1, root_id1);
        builder.morphism(rootToInner, root1, inner1);
        builder.morphism(rootToInner, root1, inner2);
        
        var root2 = builder.value(rootToId, "2").object(rootKey);
        var root_id2 = builder.value(Signature.Empty(), "2").object(idKey);
        var inner3 = buildExpectedInnerInstance(builder, "2", "2", "location", "Praha", "cs");
        var inner4 = buildExpectedInnerInstance(builder, "3", "2", "country", "Česká republika", "cs");
        
        builder.morphism(rootToId, root2, root_id2);
        builder.morphism(rootToInner, root2, inner3);
        builder.morphism(rootToInner, root2, inner4);
        
        return instance;
    }

    private ActiveDomainRow buildExpectedInnerInstance(SimpleInstanceCategoryBuilder builder, String uniqueId, String id, String name, String content, String language)
    {
        var innerRow = builder.value(innerToId, id).value(innerToName, name).object(innerKey);
        var nameRow = builder.value(Signature.Empty(), name).object(nameKey);

        var itemRow = builder.value(Signature.Empty(), uniqueId).object(itemKey);
        var contentRow = builder.value(Signature.Empty(), content).object(contentKey);
        var languageRow = builder.value(Signature.Empty(), language).object(languageKey);
        
        builder.morphism(innerToName, innerRow, nameRow);
        builder.morphism(innerToItem, innerRow, itemRow);
        builder.morphism(itemToContent, itemRow, contentRow);
        builder.morphism(itemToLanguage, itemRow, languageRow);
        
        return innerRow;
    }

    @Test
    public void execute()
    {
        super.testAlgorithm();
    }
}
