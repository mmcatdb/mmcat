/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.transformations;


import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.record.RootRecord;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.transformations.ModelToCategory;
import java.util.Set;
import java.util.TreeSet;
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

	private static final int EPSILON = -1;

	private SchemaCategory buildSchemaCategoryScenario() {
		SchemaCategory schema = new SchemaCategory();

		// staci vytvorit jednoduchou schematickou kategorii pro objednavku s 3 property (pro zacatek), zatim tedy nebudeme uvazovat vnorene property
		// a k tomu vytvorime mapovani
		// a pak take vytvorime dokumenty (tri vzorove dokumenty, jeden se vsemi properties, jeden s jednou property (jen identifikator) a jeden uplne prazdny dokument, zadna property
		

		return schema;
	}

	private InstanceCategory buildInstanceScenario(SchemaCategory schema) {
		InstanceCategory instance = new InstanceCategory(schema);
		// je treba implementovat jeste tento konstruktor

		return instance;
	}
	
	/**
	 * Test of algorithm method, of class ModelToCategory.
	 */
	@org.junit.jupiter.api.Test
	public void testAlgorithm() {
		System.out.println("algorithm");
		SchemaCategory schema = buildSchemaCategoryScenario();
		InstanceCategory instance = buildInstanceScenario(schema);
		ForestOfRecords forest = new ForestOfRecords();
		
		RootRecord record = new RootRecord();
		record.addSimpleRecord(name, this, signature);	// je treba umet vkladat value
		record.addComplexRecord(name, signature); // complexRecord nemusi mit vzdy prirazenou signaturu, a nekdy se muze jednat o pole
		
		
		forest.addRecord(record);
		Mapping mapping = null;
		ModelToCategory transformation = new ModelToCategory();
		transformation.input(schema, instance, forest, mapping);
		transformation.algorithm();
		// vystupem transformace je naplnena nebo obohacena instancni kategorie
		
		// budeme potrebovat equals a toString metody nad vsemi tridami - schema/instance category, forestOfRecords, record, ...
//		fail("The test case is a prototype.");
	}

}
