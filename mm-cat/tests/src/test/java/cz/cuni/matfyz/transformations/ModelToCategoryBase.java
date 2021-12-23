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
import cz.cuni.matfyz.wrapperDummy.DummyPullWrapper;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author pavel.koupil, jachymb.bartik
 */
public abstract class ModelToCategoryBase
{
	public ModelToCategoryBase() {}

	@BeforeAll
	public static void setUpClass() {}

	@AfterAll
	public static void tearDownClass() {}

	@BeforeEach
	public void setUp() {}

	@AfterEach
	public void tearDown() {}

	protected static final int EPSILON = -1;

	/**
	 * Test of algorithm method, of class ModelToCategory.
	 */
	@org.junit.jupiter.api.Test
	public void testAlgorithm() throws Exception
    {
		SchemaCategory schema = buildSchemaCategoryScenario();
		InstanceCategory instance = buildInstanceScenario(schema);
		ComplexProperty path = buildComplexPropertyPath(schema);
		ForestOfRecords forest = buildForestOfRecords(path);
		Mapping mapping = buildMapping(schema, path);

		// execute transformation
		ModelToCategory transformation = new ModelToCategory();
		transformation.input(schema, instance, forest, mapping);
		transformation.algorithm();
        
		// vystupem transformace je naplnena nebo obohacena instancni kategorie

        System.out.println("# Instance Category");
		System.out.println(instance);
		
		// budeme potrebovat equals a toString metody nad vsemi tridami - schema/instance category, forestOfRecords, record, ...
//		fail("The test case is a prototype.");
        
        
	}

    protected abstract SchemaCategory buildSchemaCategoryScenario();

	protected InstanceCategory buildInstanceScenario(SchemaCategory schema)
    {
        return new InstanceCategoryBuilder().setSchemaCategory(schema).build();
	}

	protected abstract ComplexProperty buildComplexPropertyPath(SchemaCategory schema);

	protected abstract ForestOfRecords buildForestOfRecords(ComplexProperty path) throws Exception;
	
	protected abstract Mapping buildMapping(SchemaCategory schema, ComplexProperty path);
}
