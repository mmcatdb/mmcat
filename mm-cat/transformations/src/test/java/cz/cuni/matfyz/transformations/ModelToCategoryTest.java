/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.transformations;

import cz.cuni.matfyz.core.ForestOfRecords;
import cz.cuni.matfyz.core.category.Signature2;
import cz.cuni.matfyz.core.instance.InstanceCategory;
import cz.cuni.matfyz.core.mapping.Mapping;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.Property;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaObject;
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

		Property id_1 = new Property(new Signature2(1), "Id");
		Property name_3 = new Property(new Signature2(3), "Name");
		Property tag_5 = new Property(new Signature2(5), "Tag");
		Property surname_7 = new Property(new Signature2(7), "Surname");
		Property id_epsilon = new Property(new Signature2(EPSILON), "Id");
		Property name_epsilon = new Property(new Signature2(EPSILON), "Name");
		Property tag_epsilon = new Property(new Signature2(EPSILON), "Tag");
		Property surname_epsilon = new Property(new Signature2(EPSILON), "Surname");
//		Property street_11 = new Property(new Signature2(11), "Street");
//		Property city_13 = new Property(new Signature2(13), "City");
//		Property postalCode_15 = new Property(new Signature2(15), "PostalCode");
		Property street_epsilon = new Property(new Signature2(EPSILON), "Street");
		Property city_epsilon = new Property(new Signature2(EPSILON), "City");
		Property postalCode_epsilon = new Property(new Signature2(EPSILON), "PostalCode");
//		Property address_9 = new Property(new Signature2(9), "Address");
		Property address_epsilon = new Property(new Signature2(EPSILON), "Address");
		Property customer_17 = new Property(new Signature2(1, 17), "Id.1.17");
		Property customer_19 = new Property(new Signature2(1, 19), "Id.1.19");
		Property name_115_epsilon = new Property(new Signature2(EPSILON), "Name");
		Property name_31 = new Property(new Signature2(31), "Name");
		Property name_31_29 = new Property(new Signature2(31, 29), "Name");
		Property value_epsilon = new Property(new Signature2(EPSILON), "Value");
		Property value_33 = new Property(new Signature2(33), "Value");
		Property number_epsilon = new Property(new Signature2(EPSILON), "Number");
		Property number_25 = new Property(new Signature2(25), "Number");
		Property number_25_28 = new Property(new Signature2(25, 28), "Number");
		Property id_1_21_24 = new Property(new Signature2(1, 21, 24), "Id");
		Property id_1_21_24_28 = new Property(new Signature2(1, 21, 24, 28), "Id");
		Property id_1_21 = new Property(new Signature2(1, 21), "Id");

		Property number_25_23 = new Property(new Signature2(25, 23), "Number");
		Property quantity_epsilon = new Property(new Signature2(EPSILON), "Quantity");
		Property quantity_120_epsilon = new Property(new Signature2(EPSILON), "Quantity");
		Property id_47_39 = new Property(new Signature2(47, 39), "Id");
		Property id_1_21_24_36 = new Property(new Signature2(1, 21, 24, 36), "Id");
		Property number_25_36 = new Property(new Signature2(25, 36), "Number");
		Property id_1_42 = new Property(new Signature2(1, 42), "Id");
		Property id_47_45 = new Property(new Signature2(47, 45), "Id");
		Property id_47 = new Property(new Signature2(47), "Id");
		Property id_122_epsilon = new Property(new Signature2(EPSILON), "Id");
		Property name_123_epsilon = new Property(new Signature2(EPSILON), "Name");
		Property price_epsilon = new Property(new Signature2(EPSILON), "Price");
		Property length_epsilon = new Property(new Signature2(EPSILON), "Length");
		Property pages_epsilon = new Property(new Signature2(EPSILON), "Pages");
		Property id_47_54 = new Property(new Signature2(47, 54), "Id");
		Property id_47_58 = new Property(new Signature2(47, 58), "Id");
		Property name_131_epsilon = new Property(new Signature2(EPSILON), "Name");
		Property name_65 = new Property(new Signature2(65), "Name");
		Property name_65_63 = new Property(new Signature2(65, 63), "Name");
		Property id_47_58_62 = new Property(new Signature2(47, 58, 62), "Id");

		Set<Property> superid100 = new TreeSet<>();
		Set<Key> ids100 = new TreeSet<>();
		superid100.add(id_1);
		superid100.add(name_3);
		superid100.add(tag_5);
		superid100.add(surname_7);
		ids100.add(new Key(id_1));
		ids100.add(new Key(name_3, tag_5));
		ids100.add(new Key(surname_7, tag_5));

		Set<Property> superid101 = new TreeSet<>();
		Set<Key> ids101 = new TreeSet<>();
		superid101.add(id_epsilon);
		ids101.add(new Key(id_epsilon));

		Set<Property> superid102 = new TreeSet<>();
		Set<Key> ids102 = new TreeSet<>();
		superid102.add(name_epsilon);
		ids102.add(new Key(name_epsilon));

		Set<Property> superid103 = new TreeSet<>();
		Set<Key> ids103 = new TreeSet<>();
		superid103.add(tag_epsilon);
		ids103.add(new Key(tag_epsilon));

		Set<Property> superid104 = new TreeSet<>();
		Set<Key> ids104 = new TreeSet<>();
		superid104.add(surname_epsilon);
		ids104.add(new Key(surname_epsilon));

		Set<Property> superid105 = new TreeSet<>();
		Set<Key> ids105 = new TreeSet<>();
		superid105.add(address_epsilon);
		ids105.add(new Key(address_epsilon));

		Set<Property> superid106 = new TreeSet<>();
		Set<Key> ids106 = new TreeSet<>();
		superid106.add(street_epsilon);
		ids106.add(new Key(street_epsilon));

		Set<Property> superid107 = new TreeSet<>();
		Set<Key> ids107 = new TreeSet<>();
		superid107.add(city_epsilon);
		ids107.add(new Key(city_epsilon));

		Set<Property> superid108 = new TreeSet<>();
		Set<Key> ids108 = new TreeSet<>();
		superid108.add(postalCode_epsilon);
		ids108.add(new Key(postalCode_epsilon));

		Set<Property> superid109 = new TreeSet<>();
		Set<Key> ids109 = new TreeSet<>();
		superid109.add(customer_17);
		superid109.add(customer_19);
		ids109.add(new Key(customer_17, customer_19));

		Set<Property> superid110 = new TreeSet<>();
		Set<Key> ids110 = new TreeSet<>();
		superid110.add(id_1_21);
		superid110.add(number_25_23);
		ids110.add(new Key(id_1_21, number_25_23));

		Set<Property> superid111 = new TreeSet<>();
		Set<Key> ids111 = new TreeSet<>();
		superid111.add(id_1_21_24);
		superid111.add(number_25);
		ids111.add(new Key(id_1_21_24, number_25));

		Set<Property> superid112 = new TreeSet<>();
		Set<Key> ids112 = new TreeSet<>();
		superid112.add(number_epsilon);
		ids112.add(new Key(number_epsilon));

		Set<Property> superid113 = new TreeSet<>();
		Set<Key> ids113 = new TreeSet<>();
		superid113.add(id_1_21_24_28);
		superid113.add(number_25_28);
		superid113.add(name_31_29);
		superid113.add(value_33);
		ids113.add(new Key(id_1_21_24_28, number_25_28, name_31_29, value_33));

		Set<Property> superid114 = new TreeSet<>();
		Set<Key> ids114 = new TreeSet<>();
		superid114.add(name_31);
		ids114.add(new Key(name_31));

		Set<Property> superid115 = new TreeSet<>();
		Set<Key> ids115 = new TreeSet<>();
		superid115.add(name_115_epsilon);
		ids115.add(new Key(name_115_epsilon));

		Set<Property> superid116 = new TreeSet<>();
		Set<Key> ids116 = new TreeSet<>();
		superid116.add(value_epsilon);
		ids116.add(new Key(value_epsilon));

		Set<Property> superid117 = new TreeSet<>();
		Set<Key> ids117 = new TreeSet<>();
		superid117.add(id_1_21_24_36);
		superid117.add(number_25_36);
		superid117.add(id_47_39);
		ids117.add(new Key(id_1_21_24_36, number_25_36, id_47_39));

		Set<Property> superid118 = new TreeSet<>();
		Set<Key> ids118 = new TreeSet<>();
		superid118.add(quantity_epsilon);
		ids118.add(new Key(quantity_epsilon));

		Set<Property> superid119 = new TreeSet<>();
		Set<Key> ids119 = new TreeSet<>();
		superid119.add(id_1_42);
		superid119.add(id_47_45);
		ids119.add(new Key(id_1_42, id_47_45));

		Set<Property> superid120 = new TreeSet<>();
		Set<Key> ids120 = new TreeSet<>();
		superid120.add(quantity_120_epsilon);
		ids120.add(new Key(quantity_120_epsilon));

		Set<Property> superid121 = new TreeSet<>();
		Set<Key> ids121 = new TreeSet<>();
		superid121.add(id_47);
		ids121.add(new Key(id_47));

		Set<Property> superid122 = new TreeSet<>();
		Set<Key> ids122 = new TreeSet<>();
		superid122.add(id_122_epsilon);
		ids122.add(new Key(id_122_epsilon));

		Set<Property> superid123 = new TreeSet<>();
		Set<Key> ids123 = new TreeSet<>();
		superid123.add(name_123_epsilon);
		ids123.add(new Key(name_123_epsilon));

		Set<Property> superid124 = new TreeSet<>();
		Set<Key> ids124 = new TreeSet<>();
		superid124.add(price_epsilon);
		ids124.add(new Key(price_epsilon));

		Set<Property> superid125 = new TreeSet<>();
		Set<Key> ids125 = new TreeSet<>();
		superid125.add(id_47_54);
		ids125.add(new Key(id_47_54));

		Set<Property> superid126 = new TreeSet<>();
		Set<Key> ids126 = new TreeSet<>();
		superid126.add(length_epsilon);
		ids126.add(new Key(length_epsilon));

		Set<Property> superid127 = new TreeSet<>();
		Set<Key> ids127 = new TreeSet<>();
		superid127.add(id_47_58);
		ids127.add(new Key(id_47_58));

		Set<Property> superid128 = new TreeSet<>();
		Set<Key> ids128 = new TreeSet<>();
		superid128.add(pages_epsilon);
		ids128.add(new Key(pages_epsilon));

		Set<Property> superid129 = new TreeSet<>();
		Set<Key> ids129 = new TreeSet<>();
		superid129.add(id_47_58_62);
		superid129.add(name_65_63);
		ids129.add(new Key(id_47_58_62, name_65_63));

		Set<Property> superid130 = new TreeSet<>();
		Set<Key> ids130 = new TreeSet<>();
		superid130.add(name_65);
		ids130.add(new Key(name_65));

		Set<Property> superid131 = new TreeSet<>();
		Set<Key> ids131 = new TreeSet<>();
		superid131.add(name_131_epsilon);
		ids131.add(new Key(name_131_epsilon));

		SchemaObject _100 = new SchemaObject(100, "Customer", superid100, ids100);
		SchemaObject _101 = new SchemaObject(101, "Id", superid101, ids101);
		SchemaObject _102 = new SchemaObject(102, "Name", superid102, ids102);
		SchemaObject _103 = new SchemaObject(103, "Tag", superid103, ids103);
		SchemaObject _104 = new SchemaObject(104, "Surname", superid104, ids104);
		SchemaObject _105 = new SchemaObject(105, "Address", superid105, ids105);
		SchemaObject _106 = new SchemaObject(106, "Street", superid106, ids106);
		SchemaObject _107 = new SchemaObject(107, "City", superid107, ids107);
		SchemaObject _108 = new SchemaObject(108, "PostalCode", superid108, ids108);
		SchemaObject _109 = new SchemaObject(109, "Friend", superid109, ids109);
		SchemaObject _110 = new SchemaObject(110, "Orders", superid110, ids110);
		SchemaObject _111 = new SchemaObject(111, "Order", superid111, ids111);
		SchemaObject _112 = new SchemaObject(112, "Number", superid112, ids112);
		SchemaObject _113 = new SchemaObject(113, "Contact", superid113, ids113);
		SchemaObject _114 = new SchemaObject(114, "Type", superid114, ids114);
		SchemaObject _115 = new SchemaObject(115, "Name", superid115, ids115);
		SchemaObject _116 = new SchemaObject(116, "Value", superid116, ids116);
		SchemaObject _117 = new SchemaObject(117, "Items", superid117, ids117);
		SchemaObject _118 = new SchemaObject(118, "Quantity", superid118, ids118);
		SchemaObject _119 = new SchemaObject(119, "Cart", superid119, ids119);
		SchemaObject _120 = new SchemaObject(120, "Quantity", superid120, ids120);
		SchemaObject _121 = new SchemaObject(121, "Product", superid121, ids121);
		SchemaObject _122 = new SchemaObject(122, "Id", superid122, ids122);
		SchemaObject _123 = new SchemaObject(123, "Name", superid123, ids123);
		SchemaObject _124 = new SchemaObject(124, "Price", superid124, ids124);
		SchemaObject _125 = new SchemaObject(125, "Audiobook", superid125, ids125);
		SchemaObject _126 = new SchemaObject(126, "Length", superid126, ids126);
		SchemaObject _127 = new SchemaObject(127, "Book", superid127, ids127);
		SchemaObject _128 = new SchemaObject(128, "Pages", superid128, ids128);
		SchemaObject _129 = new SchemaObject(129, "Publishes", superid129, ids129);
		SchemaObject _130 = new SchemaObject(130, "Publisher", superid130, ids130);
		SchemaObject _131 = new SchemaObject(131, "Name", superid131, ids131);

		return schema;
	}

	/**
	 * Test of algorithm method, of class ModelToCategory.
	 */
	@org.junit.jupiter.api.Test
	public void testAlgorithm() {
		System.out.println("algorithm");
		SchemaCategory schema = buildSchemaCategoryScenario();
		InstanceCategory instance = null;
		ForestOfRecords forest = null;
		Mapping mapping = null;
		ModelToCategory transformation = new ModelToCategory();
		transformation.algorithm(schema, instance, forest, mapping);
		// TODO review the generated test code and remove the default call to fail.
		fail("The test case is a prototype.");
	}

}
