/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.instance.*;
import cz.cuni.matfyz.core.mapping.*;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.utils.Debug;
import java.net.MalformedURLException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 *
 * @author pavel.koupil, jachymb.bartik
 */
public abstract class ModelToCategoryBase
{
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(ModelToCategoryBase.class);
	
    protected int getDebugLevel()
    {
        return 5;
    }
    
    @BeforeEach
	public void setUp()
    {
        Debug.setLevel(getDebugLevel());
    }

	protected static final int EPSILON = -1;

	/**
	 * Test of algorithm method, of class ModelToCategory.
	 */
	public void testAlgorithm() throws Exception
    {
		SchemaCategory schema = buildSchemaCategoryScenario();
        if (Debug.shouldLog(3))
        {
			System.out.println(String.format("# Schema Category\n%s", schema));
        }
        
		InstanceCategory instance = buildInstanceScenario(schema);
		ComplexProperty path = buildComplexPropertyPath(schema);
        if (Debug.shouldLog(3))
        {
			System.out.println(String.format("# Access Path\n%s", path));
        }
        
		ForestOfRecords forest = buildForestOfRecords(path);
        if (Debug.shouldLog(3))
        {
			System.out.println(String.format("# Forest of Records\n%s", forest));
        }
        
		Mapping mapping = buildMapping(schema, path);

		ModelToCategory transformation = new ModelToCategory();
		transformation.input(schema, instance, forest, mapping);
		transformation.algorithm();

        if (Debug.shouldLog(4))
        {
			System.out.println(String.format("# Instance CategoryRecords\n%s", instance));
        }
		
        InstanceCategory expectedInstance = buildExpectedInstanceCategory(schema);
        
        Assertions.assertEquals(expectedInstance.objects(), instance.objects(), "Test objects differs from the expected objects.");
        Assertions.assertEquals(expectedInstance.morphisms(), instance.morphisms(), "Test morphisms differs from the expected morphisms.");
	}

    protected abstract SchemaCategory buildSchemaCategoryScenario();

	protected InstanceCategory buildInstanceScenario(SchemaCategory schema)
    {
        return new InstanceCategoryBuilder().setSchemaCategory(schema).build();
	}

	protected abstract ComplexProperty buildComplexPropertyPath(SchemaCategory schema);

	protected abstract ForestOfRecords buildForestOfRecords(ComplexProperty path) throws Exception;
	
	protected abstract Mapping buildMapping(SchemaCategory schema, ComplexProperty path);
    
    protected abstract InstanceCategory buildExpectedInstanceCategory(SchemaCategory schema);
}
