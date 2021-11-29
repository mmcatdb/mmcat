/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.AccessPath;
import cz.cuni.matfyz.core.mapping.ComplexProperty;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.record.ForestOfRecords;
import cz.cuni.matfyz.core.record.RootRecord;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import cz.cuni.matfyz.transformations.ModelToCategory;
import cz.cuni.matfyz.wrapper.dummy.DataProvider;
import cz.cuni.matfyz.wrapper.dummy.DummyPullWrapper;
import java.util.Collection;
import java.util.List;
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
		// Kind: Order (interni identifikator _id)
		// property _id ... identifikátor, simple
		// property totalPrice
		// property address (jeden String ve tvaru "Street, City, postalCode")
		
		// A->B, k nemu dualni B->A
		// A->A, k nemu dualni A->A
		
		
		// staci vytvorit jednoduchou schematickou kategorii pro objednavku s 3 property (pro zacatek), zatim tedy nebudeme uvazovat vnorene property
		// a k tomu vytvorime mapovani
		// a pak take vytvorime dokumenty (tri vzorove dokumenty, jeden se vsemi properties, jeden s jednou property (jen identifikator) a jeden uplne prazdny dokument, zadna property
		System.out.println(schema);
		return schema;
	}

	private InstanceCategory buildInstanceScenario(SchemaCategory schema) {
		InstanceCategory instance = new InstanceCategory(schema);
		// je treba implementovat jeste tento konstruktor

		return instance;
	}

	private ComplexProperty buildComplexPropertyPath(SchemaCategory schema) {
//		ComplexProperty path = new ComplexProperty();

		// Kind: Order
		// property _id ... identifikátor, simple
		// property totalPrice
		// property address (jeden String ve tvaru "Street, City, postalCode")
		System.out.println(path);
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	private ForestOfRecords buildForestOfRecords(ComplexProperty path) throws Exception {
		// s pomoci accessPath a dat vytvorit forestOfRecords
		DummyPullWrapper wrapper = new DummyPullWrapper();
		// wrapper by mel vratit 3 dokumenty (forest se 3 record)
		ForestOfRecords forest = wrapper.pullForest("SELECT_ALL", path);
		System.out.println(forest);
		return forest;
	}
	
	private Mapping buildMapping(ComplexProperty path) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	/**
	 * Test of algorithm method, of class ModelToCategory.
	 */
	@org.junit.jupiter.api.Test
	public void testAlgorithm() throws Exception {
		SchemaCategory schema = buildSchemaCategoryScenario();
		InstanceCategory instance = buildInstanceScenario(schema);
		ComplexProperty path = buildComplexPropertyPath(schema);
		ForestOfRecords forest = buildForestOfRecords(path);
		Mapping mapping = buildMapping(path);

		// execute transformation
		ModelToCategory transformation = new ModelToCategory();
		transformation.input(schema, instance, forest, mapping);
		transformation.algorithm();
		// vystupem transformace je naplnena nebo obohacena instancni kategorie

		System.out.println(instance);
		
		// budeme potrebovat equals a toString metody nad vsemi tridami - schema/instance category, forestOfRecords, record, ...
//		fail("The test case is a prototype.");
	}

	

}
