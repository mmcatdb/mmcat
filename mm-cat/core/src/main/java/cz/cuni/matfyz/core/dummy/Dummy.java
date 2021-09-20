/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.dummy;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.category.Signature2;
import cz.cuni.matfyz.core.schema.Key;
import cz.cuni.matfyz.core.schema.Property;
import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.core.schema.SchemaMorphism;
import cz.cuni.matfyz.core.schema.SchemaObject;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.koupil
 */
public class Dummy {

	private static final int EPSILON = -1;

	public static SchemaCategory buildSchemaCategoryScenario() {
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

//		model.addWidget("100", "Customer", 10, 260, WidgetType.CATEGORICAL_OBJECT);
//		model.addWidget("101", "Id", 10, 380, WidgetType.MAPPING_PROPERTY);
//		model.addWidget("110", "Orders", 10, 140, WidgetType.CATEGORICAL_OBJECT);
//		model.addWidget("111", "Order", 130, 140, WidgetType.MAPPING_KIND);
//		model.addWidget("112", "Number", 130, 20, WidgetType.MAPPING_PROPERTY);
//		model.addWidget("113", "Contact", 250, 140, WidgetType.MAPPING_PROPERTY);
//		model.addWidget("114", "Type", 370, 140, WidgetType.CATEGORICAL_OBJECT);
//		model.addWidget("115", "Name", 370, 20, WidgetType.MAPPING_NAME);
//		model.addWidget("116", "Value", 250, 20, WidgetType.MAPPING_PROPERTY);
//		model.addWidget("117", "Items", 130, 260, WidgetType.MAPPING_PROPERTY);
//		model.addWidget("118", "Quantity", 250, 260, WidgetType.MAPPING_PROPERTY);
//		model.addWidget("121", "Product", 130, 380, WidgetType.CATEGORICAL_OBJECT);
//		model.addWidget("122", "Id", 10, 500, WidgetType.MAPPING_PROPERTY);
//		model.addWidget("123", "Name", 250, 500, WidgetType.MAPPING_PROPERTY);
//		model.addWidget("124", "Price", 250, 380, WidgetType.MAPPING_PROPERTY);
		SchemaObject _100 = new SchemaObject(100, "Customer", superid100, ids100, 10, 260);
		SchemaObject _101 = new SchemaObject(101, "Id", superid101, ids101, 10, 380);
		SchemaObject _102 = new SchemaObject(102, "Name", superid102, ids102);
		SchemaObject _103 = new SchemaObject(103, "Tag", superid103, ids103);
		SchemaObject _104 = new SchemaObject(104, "Surname", superid104, ids104);
		SchemaObject _105 = new SchemaObject(105, "Address", superid105, ids105);
		SchemaObject _106 = new SchemaObject(106, "Street", superid106, ids106);
		SchemaObject _107 = new SchemaObject(107, "City", superid107, ids107);
		SchemaObject _108 = new SchemaObject(108, "PostalCode", superid108, ids108);
		SchemaObject _109 = new SchemaObject(109, "Friend", superid109, ids109);
		SchemaObject _110 = new SchemaObject(110, "Orders", superid110, ids110, 10, 140);
		SchemaObject _111 = new SchemaObject(111, "Order", superid111, ids111, 130, 140);
		SchemaObject _112 = new SchemaObject(112, "Number", superid112, ids112, 130, 20);
		SchemaObject _113 = new SchemaObject(113, "Contact", superid113, ids113, 250, 140);
		SchemaObject _114 = new SchemaObject(114, "Type", superid114, ids114, 370, 140);
		SchemaObject _115 = new SchemaObject(115, "Name", superid115, ids115, 370, 20);
		SchemaObject _116 = new SchemaObject(116, "Value", superid116, ids116, 250, 20);
		SchemaObject _117 = new SchemaObject(117, "Items", superid117, ids117, 130, 260);
		SchemaObject _118 = new SchemaObject(118, "Quantity", superid118, ids118, 250, 260);
		SchemaObject _119 = new SchemaObject(119, "Cart", superid119, ids119);
		SchemaObject _120 = new SchemaObject(120, "Quantity", superid120, ids120);
		SchemaObject _121 = new SchemaObject(121, "Product", superid121, ids121, 130, 380);
		SchemaObject _122 = new SchemaObject(122, "Id", superid122, ids122, 10, 500);
		SchemaObject _123 = new SchemaObject(123, "Name", superid123, ids123, 250, 500);
		SchemaObject _124 = new SchemaObject(124, "Price", superid124, ids124, 250, 380);
		SchemaObject _125 = new SchemaObject(125, "Audiobook", superid125, ids125);
		SchemaObject _126 = new SchemaObject(126, "Length", superid126, ids126);
		SchemaObject _127 = new SchemaObject(127, "Book", superid127, ids127);
		SchemaObject _128 = new SchemaObject(128, "Pages", superid128, ids128);
		SchemaObject _129 = new SchemaObject(129, "Publishes", superid129, ids129);
		SchemaObject _130 = new SchemaObject(130, "Publisher", superid130, ids130);
		SchemaObject _131 = new SchemaObject(131, "Name", superid131, ids131);

		schema.addObject(_100);
		schema.addObject(_101);
		schema.addObject(_102);
		schema.addObject(_103);
		schema.addObject(_104);
		schema.addObject(_105);
		schema.addObject(_106);
		schema.addObject(_107);
		schema.addObject(_108);
		schema.addObject(_109);
		schema.addObject(_110);
		schema.addObject(_111);
		schema.addObject(_112);
		schema.addObject(_113);
		schema.addObject(_114);
		schema.addObject(_115);
		schema.addObject(_116);
		schema.addObject(_117);
		schema.addObject(_118);
		schema.addObject(_119);
		schema.addObject(_120);
		schema.addObject(_121);
		schema.addObject(_122);
		schema.addObject(_123);
		schema.addObject(_124);
		schema.addObject(_125);
		schema.addObject(_126);
		schema.addObject(_127);
		schema.addObject(_128);
		schema.addObject(_129);
		schema.addObject(_130);
		schema.addObject(_131);

		SchemaMorphism _1 = new SchemaMorphism(new Signature(1), _100, _101, 111, 111);
		SchemaMorphism _2 = new SchemaMorphism(new Signature(2), _101, _100, 111, 111);
		SchemaMorphism _3 = new SchemaMorphism(new Signature(3), _100, _102, 111, 111);
		SchemaMorphism _4 = new SchemaMorphism(new Signature(4), _102, _100, 111, 111);
		SchemaMorphism _5 = new SchemaMorphism(new Signature(5), _100, _103, 111, 111);
		SchemaMorphism _6 = new SchemaMorphism(new Signature(6), _103, _100, 111, 111);
		SchemaMorphism _7 = new SchemaMorphism(new Signature(7), _100, _104, 111, 111);
		SchemaMorphism _8 = new SchemaMorphism(new Signature(8), _104, _100, 111, 111);
		SchemaMorphism _9 = new SchemaMorphism(new Signature(9), _100, _105, 111, 111);
		SchemaMorphism _10 = new SchemaMorphism(new Signature(10), _105, _100, 111, 111);
		SchemaMorphism _11 = new SchemaMorphism(new Signature(11), _105, _106, 111, 111);
		SchemaMorphism _12 = new SchemaMorphism(new Signature(12), _106, _105, 111, 111);
		SchemaMorphism _13 = new SchemaMorphism(new Signature(13), _105, _107, 111, 111);
		SchemaMorphism _14 = new SchemaMorphism(new Signature(14), _107, _105, 111, 111);
		SchemaMorphism _15 = new SchemaMorphism(new Signature(15), _105, _108, 111, 111);
		SchemaMorphism _16 = new SchemaMorphism(new Signature(16), _108, _105, 111, 111);
		SchemaMorphism _17 = new SchemaMorphism(new Signature(17), _109, _100, 111, 111);
		SchemaMorphism _18 = new SchemaMorphism(new Signature(18), _100, _109, 111, 111);
		SchemaMorphism _19 = new SchemaMorphism(new Signature(19), _109, _100, 111, 111);
		SchemaMorphism _20 = new SchemaMorphism(new Signature(20), _100, _109, 111, 111);
		SchemaMorphism _21 = new SchemaMorphism(new Signature(21), _110, _100, 111, 111);
		SchemaMorphism _22 = new SchemaMorphism(new Signature(22), _100, _110, 111, 111);
		SchemaMorphism _23 = new SchemaMorphism(new Signature(23), _110, _111, 111, 111);
		SchemaMorphism _24 = new SchemaMorphism(new Signature(24), _111, _110, 111, 111);
		SchemaMorphism _25 = new SchemaMorphism(new Signature(25), _111, _112, 111, 111);
		SchemaMorphism _26 = new SchemaMorphism(new Signature(26), _112, _111, 111, 111);
		SchemaMorphism _27 = new SchemaMorphism(new Signature(27), _111, _113, 111, 111);
		SchemaMorphism _28 = new SchemaMorphism(new Signature(28), _113, _111, 111, 111);
		SchemaMorphism _29 = new SchemaMorphism(new Signature(29), _113, _114, 111, 111);
		SchemaMorphism _30 = new SchemaMorphism(new Signature(30), _114, _113, 111, 111);
		SchemaMorphism _31 = new SchemaMorphism(new Signature(31), _114, _115, 111, 111);
		SchemaMorphism _32 = new SchemaMorphism(new Signature(32), _115, _114, 111, 111);
		SchemaMorphism _33 = new SchemaMorphism(new Signature(33), _113, _116, 111, 111);
		SchemaMorphism _34 = new SchemaMorphism(new Signature(34), _116, _113, 111, 111);
		SchemaMorphism _35 = new SchemaMorphism(new Signature(35), _111, _117, 111, 111);
		SchemaMorphism _36 = new SchemaMorphism(new Signature(36), _117, _111, 111, 111);
		SchemaMorphism _37 = new SchemaMorphism(new Signature(37), _117, _118, 111, 111);
		SchemaMorphism _38 = new SchemaMorphism(new Signature(38), _118, _117, 111, 111);
		SchemaMorphism _39 = new SchemaMorphism(new Signature(39), _117, _121, 111, 111);
		SchemaMorphism _40 = new SchemaMorphism(new Signature(40), _121, _117, 111, 111);
		SchemaMorphism _41 = new SchemaMorphism(new Signature(41), _100, _119, 111, 111);
		SchemaMorphism _42 = new SchemaMorphism(new Signature(42), _119, _100, 111, 111);
		SchemaMorphism _43 = new SchemaMorphism(new Signature(43), _119, _120, 111, 111);
		SchemaMorphism _44 = new SchemaMorphism(new Signature(44), _120, _119, 111, 111);
		SchemaMorphism _45 = new SchemaMorphism(new Signature(45), _119, _121, 111, 111);
		SchemaMorphism _46 = new SchemaMorphism(new Signature(46), _121, _119, 111, 111);
		SchemaMorphism _47 = new SchemaMorphism(new Signature(47), _121, _122, 111, 111);
		SchemaMorphism _48 = new SchemaMorphism(new Signature(48), _122, _121, 111, 111);
		SchemaMorphism _49 = new SchemaMorphism(new Signature(49), _121, _123, 111, 111);
		SchemaMorphism _50 = new SchemaMorphism(new Signature(50), _123, _121, 111, 111);
		SchemaMorphism _51 = new SchemaMorphism(new Signature(51), _121, _124, 111, 111);
		SchemaMorphism _52 = new SchemaMorphism(new Signature(52), _124, _121, 111, 111);
		SchemaMorphism _53 = new SchemaMorphism(new Signature(53), _121, _125, 111, 111);
		SchemaMorphism _54 = new SchemaMorphism(new Signature(54), _125, _121, 111, 111);
		SchemaMorphism _55 = new SchemaMorphism(new Signature(55), _125, _126, 111, 111);
		SchemaMorphism _56 = new SchemaMorphism(new Signature(56), _126, _125, 111, 111);
		SchemaMorphism _57 = new SchemaMorphism(new Signature(57), _121, _127, 111, 111);
		SchemaMorphism _58 = new SchemaMorphism(new Signature(58), _127, _121, 111, 111);
		SchemaMorphism _59 = new SchemaMorphism(new Signature(59), _127, _128, 111, 111);
		SchemaMorphism _60 = new SchemaMorphism(new Signature(60), _128, _127, 111, 111);
		SchemaMorphism _61 = new SchemaMorphism(new Signature(61), _127, _129, 111, 111);
		SchemaMorphism _62 = new SchemaMorphism(new Signature(62), _129, _127, 111, 111);
		SchemaMorphism _63 = new SchemaMorphism(new Signature(63), _129, _130, 111, 111);
		SchemaMorphism _64 = new SchemaMorphism(new Signature(64), _130, _129, 111, 111);
		SchemaMorphism _65 = new SchemaMorphism(new Signature(65), _130, _131, 111, 111);
		SchemaMorphism _66 = new SchemaMorphism(new Signature(66), _131, _130, 111, 111);
		SchemaMorphism _e100 = new SchemaMorphism(new Signature(100), _100, _100, 111, 111);
		SchemaMorphism _e101 = new SchemaMorphism(new Signature(100), _101, _101, 111, 111);
		SchemaMorphism _e102 = new SchemaMorphism(new Signature(100), _102, _102, 111, 111);
		SchemaMorphism _e103 = new SchemaMorphism(new Signature(100), _103, _103, 111, 111);
		SchemaMorphism _e104 = new SchemaMorphism(new Signature(100), _104, _104, 111, 111);
		SchemaMorphism _e105 = new SchemaMorphism(new Signature(100), _105, _105, 111, 111);
		SchemaMorphism _e106 = new SchemaMorphism(new Signature(100), _106, _106, 111, 111);
		SchemaMorphism _e107 = new SchemaMorphism(new Signature(100), _107, _107, 111, 111);
		SchemaMorphism _e108 = new SchemaMorphism(new Signature(100), _108, _108, 111, 111);
		SchemaMorphism _e109 = new SchemaMorphism(new Signature(100), _109, _109, 111, 111);
		SchemaMorphism _e110 = new SchemaMorphism(new Signature(100), _110, _110, 111, 111);
		SchemaMorphism _e111 = new SchemaMorphism(new Signature(100), _111, _111, 111, 111);
		SchemaMorphism _e112 = new SchemaMorphism(new Signature(100), _112, _112, 111, 111);
		SchemaMorphism _e113 = new SchemaMorphism(new Signature(100), _113, _113, 111, 111);
		SchemaMorphism _e114 = new SchemaMorphism(new Signature(100), _114, _114, 111, 111);
		SchemaMorphism _e115 = new SchemaMorphism(new Signature(100), _115, _115, 111, 111);
		SchemaMorphism _e116 = new SchemaMorphism(new Signature(100), _116, _116, 111, 111);
		SchemaMorphism _e117 = new SchemaMorphism(new Signature(100), _117, _117, 111, 111);
		SchemaMorphism _e118 = new SchemaMorphism(new Signature(100), _118, _118, 111, 111);
		SchemaMorphism _e119 = new SchemaMorphism(new Signature(100), _119, _119, 111, 111);
		SchemaMorphism _e120 = new SchemaMorphism(new Signature(100), _120, _120, 111, 111);
		SchemaMorphism _e121 = new SchemaMorphism(new Signature(100), _121, _121, 111, 111);
		SchemaMorphism _e122 = new SchemaMorphism(new Signature(100), _122, _122, 111, 111);
		SchemaMorphism _e123 = new SchemaMorphism(new Signature(100), _123, _123, 111, 111);
		SchemaMorphism _e124 = new SchemaMorphism(new Signature(100), _124, _124, 111, 111);
		SchemaMorphism _e125 = new SchemaMorphism(new Signature(100), _125, _125, 111, 111);
		SchemaMorphism _e126 = new SchemaMorphism(new Signature(100), _126, _126, 111, 111);
		SchemaMorphism _e127 = new SchemaMorphism(new Signature(100), _127, _127, 111, 111);
		SchemaMorphism _e128 = new SchemaMorphism(new Signature(100), _128, _128, 111, 111);
		SchemaMorphism _e129 = new SchemaMorphism(new Signature(100), _129, _129, 111, 111);
		SchemaMorphism _e130 = new SchemaMorphism(new Signature(100), _130, _130, 111, 111);
		SchemaMorphism _e131 = new SchemaMorphism(new Signature(100), _131, _131, 111, 111);

		schema.addMorphism(_1);
		schema.addMorphism(_2);
		schema.addMorphism(_3);
		schema.addMorphism(_4);
		schema.addMorphism(_5);
		schema.addMorphism(_6);
		schema.addMorphism(_7);
		schema.addMorphism(_8);
		schema.addMorphism(_9);
		schema.addMorphism(_10);
		schema.addMorphism(_11);
		schema.addMorphism(_12);
		schema.addMorphism(_13);
		schema.addMorphism(_14);
		schema.addMorphism(_15);
		schema.addMorphism(_16);
		schema.addMorphism(_17);
		schema.addMorphism(_18);
		schema.addMorphism(_19);
		schema.addMorphism(_20);
		schema.addMorphism(_21);
		schema.addMorphism(_22);
		schema.addMorphism(_23);
		schema.addMorphism(_24);
		schema.addMorphism(_25);
		schema.addMorphism(_26);
		schema.addMorphism(_27);
		schema.addMorphism(_28);
		schema.addMorphism(_29);
		schema.addMorphism(_30);
		schema.addMorphism(_31);
		schema.addMorphism(_32);
		schema.addMorphism(_33);
		schema.addMorphism(_34);
		schema.addMorphism(_35);
		schema.addMorphism(_36);
		schema.addMorphism(_37);
		schema.addMorphism(_38);
		schema.addMorphism(_39);
		schema.addMorphism(_40);
		schema.addMorphism(_41);
		schema.addMorphism(_42);
		schema.addMorphism(_43);
		schema.addMorphism(_44);
		schema.addMorphism(_45);
		schema.addMorphism(_46);
		schema.addMorphism(_47);
		schema.addMorphism(_48);
		schema.addMorphism(_49);
		schema.addMorphism(_50);
		schema.addMorphism(_51);
		schema.addMorphism(_52);
		schema.addMorphism(_53);
		schema.addMorphism(_54);
		schema.addMorphism(_55);
		schema.addMorphism(_56);
		schema.addMorphism(_57);
		schema.addMorphism(_58);
		schema.addMorphism(_59);
		schema.addMorphism(_60);
		schema.addMorphism(_61);
		schema.addMorphism(_62);
		schema.addMorphism(_63);
		schema.addMorphism(_64);
		schema.addMorphism(_65);
		schema.addMorphism(_66);
		schema.addMorphism(_e100);
		schema.addMorphism(_e101);
		schema.addMorphism(_e102);
		schema.addMorphism(_e103);
		schema.addMorphism(_e104);
		schema.addMorphism(_e105);
		schema.addMorphism(_e106);
		schema.addMorphism(_e107);
		schema.addMorphism(_e108);
		schema.addMorphism(_e109);
		schema.addMorphism(_e110);
		schema.addMorphism(_e111);
		schema.addMorphism(_e112);
		schema.addMorphism(_e113);
		schema.addMorphism(_e114);
		schema.addMorphism(_e115);
		schema.addMorphism(_e116);
		schema.addMorphism(_e117);
		schema.addMorphism(_e118);
		schema.addMorphism(_e119);
		schema.addMorphism(_e120);
		schema.addMorphism(_e121);
		schema.addMorphism(_e122);
		schema.addMorphism(_e123);
		schema.addMorphism(_e124);
		schema.addMorphism(_e125);
		schema.addMorphism(_e126);
		schema.addMorphism(_e127);
		schema.addMorphism(_e128);
		schema.addMorphism(_e129);
		schema.addMorphism(_e130);
		schema.addMorphism(_e131);

		return schema;
	}
}
