/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.ForestOfRecords;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author pavel.koupil
 */
public class ModelToCategoryTest {

	public ModelToCategoryTest() {
	}

	@BeforeAll
	public static void setUpClass() {
	}

	@AfterAll
	public static void tearDownClass() {
	}

	@BeforeEach
	public void setUp() {
	}

	@AfterEach
	public void tearDown() {
	}

	/**
	 * Test of algorithm method, of class ModelToCategory.
	 */
	@org.junit.jupiter.api.Test
	public void testAlgorithm() {
		System.out.println("algorithm");
		SchemaCategory schema = new SchemaCategory();
		InstanceCategory instance = null;
		ForestOfRecords forest = null;
		Mapping mapping = null;
		ModelToCategory transformation = new ModelToCategory();
		transformation.algorithm(schema, instance, forest, mapping);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

}
