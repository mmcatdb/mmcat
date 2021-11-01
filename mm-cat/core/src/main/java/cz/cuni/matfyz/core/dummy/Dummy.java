/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core.dummy;

import cz.cuni.matfyz.core.category.Signature;
import cz.cuni.matfyz.core.schema.*;
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

		Property id_1 = new Property(new Signature(1), "Id");
		Property name_3 = new Property(new Signature(3), "Name");
		Property tag_5 = new Property(new Signature(5), "Tag");
		Property surname_7 = new Property(new Signature(7), "Surname");
		Property id_epsilon = new Property(new Signature(EPSILON), "Id");
		Property name_epsilon = new Property(new Signature(EPSILON), "Name");
		Property tag_epsilon = new Property(new Signature(EPSILON), "Tag");
		Property surname_epsilon = new Property(new Signature(EPSILON), "Surname");
//		Property street_11 = new Property(new Signature(11), "Street");
//		Property city_13 = new Property(new Signature(13), "City");
//		Property postalCode_15 = new Property(new Signature(15), "PostalCode");
		Property street_epsilon = new Property(new Signature(EPSILON), "Street");
		Property city_epsilon = new Property(new Signature(EPSILON), "City");
		Property postalCode_epsilon = new Property(new Signature(EPSILON), "PostalCode");
//		Property address_9 = new Property(new Signature(9), "Address");
		Property address_epsilon = new Property(new Signature(EPSILON), "Address");
		Property customer_17 = new Property(new Signature(1, 17), "Id.1.17");
		Property customer_19 = new Property(new Signature(1, 19), "Id.1.19");
		Property name_115_epsilon = new Property(new Signature(EPSILON), "Name");
		Property name_31 = new Property(new Signature(31), "Name");
		Property name_31_29 = new Property(new Signature(31, 29), "Name");
		Property value_epsilon = new Property(new Signature(EPSILON), "Value");
		Property value_33 = new Property(new Signature(33), "Value");
		Property number_epsilon = new Property(new Signature(EPSILON), "Number");
		Property number_25 = new Property(new Signature(25), "Number");
		Property number_25_28 = new Property(new Signature(25, 28), "Number");
		Property id_1_21_24 = new Property(new Signature(1, 21, 24), "Id");
		Property id_1_21_24_28 = new Property(new Signature(1, 21, 24, 28), "Id");
		Property id_1_21 = new Property(new Signature(1, 21), "Id");

		Property number_25_23 = new Property(new Signature(25, 23), "Number");
		Property quantity_epsilon = new Property(new Signature(EPSILON), "Quantity");
		Property quantity_120_epsilon = new Property(new Signature(EPSILON), "Quantity");
		Property id_47_39 = new Property(new Signature(47, 39), "Id");
		Property id_1_21_24_36 = new Property(new Signature(1, 21, 24, 36), "Id");
		Property number_25_36 = new Property(new Signature(25, 36), "Number");
		Property id_1_42 = new Property(new Signature(1, 42), "Id");
		Property id_47_45 = new Property(new Signature(47, 45), "Id");
		Property id_47 = new Property(new Signature(47), "Id");
		Property id_122_epsilon = new Property(new Signature(EPSILON), "Id");
		Property name_123_epsilon = new Property(new Signature(EPSILON), "Name");
		Property price_epsilon = new Property(new Signature(EPSILON), "Price");
		Property length_epsilon = new Property(new Signature(EPSILON), "Length");
		Property pages_epsilon = new Property(new Signature(EPSILON), "Pages");
		Property id_47_54 = new Property(new Signature(47, 54), "Id");
		Property id_47_58 = new Property(new Signature(47, 58), "Id");
		Property name_131_epsilon = new Property(new Signature(EPSILON), "Name");
		Property name_65 = new Property(new Signature(65), "Name");
		Property name_65_63 = new Property(new Signature(65, 63), "Name");
		Property id_47_58_62 = new Property(new Signature(47, 58, 62), "Id");

		Set<Property> properties100 = new TreeSet<>();
        SuperId superid100 = new SuperId(properties100);
		Set<SuperId> ids100 = new TreeSet<>();
		properties100.add(id_1);
		properties100.add(name_3);
		properties100.add(tag_5);
		properties100.add(surname_7);
		ids100.add(new SuperId(id_1));
		ids100.add(new SuperId(name_3, tag_5));
		ids100.add(new SuperId(surname_7, tag_5));

		Set<Property> properties101 = new TreeSet<>();
        SuperId superid101 = new SuperId(properties101);
		Set<SuperId> ids101 = new TreeSet<>();
		properties101.add(id_epsilon);
		ids101.add(new SuperId(id_epsilon));

		Set<Property> properties102 = new TreeSet<>();
        SuperId superid102 = new SuperId(properties102);
		Set<SuperId> ids102 = new TreeSet<>();
		properties102.add(name_epsilon);
		ids102.add(new SuperId(name_epsilon));

		Set<Property> properties103 = new TreeSet<>();
        SuperId superid103 = new SuperId(properties103);
		Set<SuperId> ids103 = new TreeSet<>();
		properties103.add(tag_epsilon);
		ids103.add(new SuperId(tag_epsilon));

		Set<Property> properties104 = new TreeSet<>();
        SuperId superid104 = new SuperId(properties104);
		Set<SuperId> ids104 = new TreeSet<>();
		properties104.add(surname_epsilon);
		ids104.add(new SuperId(surname_epsilon));

		Set<Property> properties105 = new TreeSet<>();
        SuperId superid105 = new SuperId(properties105);
		Set<SuperId> ids105 = new TreeSet<>();
		properties105.add(address_epsilon);
		ids105.add(new SuperId(address_epsilon));

		Set<Property> properties106 = new TreeSet<>();
        SuperId superid106 = new SuperId(properties106);
		Set<SuperId> ids106 = new TreeSet<>();
		properties106.add(street_epsilon);
		ids106.add(new SuperId(street_epsilon));

		Set<Property> properties107 = new TreeSet<>();
        SuperId superid107 = new SuperId(properties107);
		Set<SuperId> ids107 = new TreeSet<>();
		properties107.add(city_epsilon);
		ids107.add(new SuperId(city_epsilon));

		Set<Property> properties108 = new TreeSet<>();
        SuperId superid108 = new SuperId(properties108);
		Set<SuperId> ids108 = new TreeSet<>();
		properties108.add(postalCode_epsilon);
		ids108.add(new SuperId(postalCode_epsilon));

		Set<Property> properties109 = new TreeSet<>();
        SuperId superid109 = new SuperId(properties109);
		Set<SuperId> ids109 = new TreeSet<>();
		properties109.add(customer_17);
		properties109.add(customer_19);
		ids109.add(new SuperId(customer_17, customer_19));

		Set<Property> properties110 = new TreeSet<>();
        SuperId superid110 = new SuperId(properties110);
		Set<SuperId> ids110 = new TreeSet<>();
		properties110.add(id_1_21);
		properties110.add(number_25_23);
		ids110.add(new SuperId(id_1_21, number_25_23));

		Set<Property> properties111 = new TreeSet<>();
        SuperId superid111 = new SuperId(properties111);
		Set<SuperId> ids111 = new TreeSet<>();
		properties111.add(id_1_21_24);
		properties111.add(number_25);
		ids111.add(new SuperId(id_1_21_24, number_25));

		Set<Property> properties112 = new TreeSet<>();
        SuperId superid112 = new SuperId(properties112);
		Set<SuperId> ids112 = new TreeSet<>();
		properties112.add(number_epsilon);
		ids112.add(new SuperId(number_epsilon));

		Set<Property> properties113 = new TreeSet<>();
        SuperId superid113 = new SuperId(properties113);
		Set<SuperId> ids113 = new TreeSet<>();
		properties113.add(id_1_21_24_28);
		properties113.add(number_25_28);
		properties113.add(name_31_29);
		properties113.add(value_33);
		ids113.add(new SuperId(id_1_21_24_28, number_25_28, name_31_29, value_33));

		Set<Property> properties114 = new TreeSet<>();
        SuperId superid114 = new SuperId(properties114);
		Set<SuperId> ids114 = new TreeSet<>();
		properties114.add(name_31);
		ids114.add(new SuperId(name_31));

		Set<Property> properties115 = new TreeSet<>();
        SuperId superid115 = new SuperId(properties115);
		Set<SuperId> ids115 = new TreeSet<>();
		properties115.add(name_115_epsilon);
		ids115.add(new SuperId(name_115_epsilon));

		Set<Property> properties116 = new TreeSet<>();
        SuperId superid116 = new SuperId(properties116);
		Set<SuperId> ids116 = new TreeSet<>();
		properties116.add(value_epsilon);
		ids116.add(new SuperId(value_epsilon));

		Set<Property> properties117 = new TreeSet<>();
        SuperId superid117 = new SuperId(properties117);
		Set<SuperId> ids117 = new TreeSet<>();
		properties117.add(id_1_21_24_36);
		properties117.add(number_25_36);
		properties117.add(id_47_39);
		ids117.add(new SuperId(id_1_21_24_36, number_25_36, id_47_39));

		Set<Property> properties118 = new TreeSet<>();
        SuperId superid118 = new SuperId(properties118);
		Set<SuperId> ids118 = new TreeSet<>();
		properties118.add(quantity_epsilon);
		ids118.add(new SuperId(quantity_epsilon));

		Set<Property> properties119 = new TreeSet<>();
        SuperId superid119 = new SuperId(properties119);
		Set<SuperId> ids119 = new TreeSet<>();
		properties119.add(id_1_42);
		properties119.add(id_47_45);
		ids119.add(new SuperId(id_1_42, id_47_45));

		Set<Property> properties120 = new TreeSet<>();
        SuperId superid120 = new SuperId(properties120);
		Set<SuperId> ids120 = new TreeSet<>();
		properties120.add(quantity_120_epsilon);
		ids120.add(new SuperId(quantity_120_epsilon));

		Set<Property> properties121 = new TreeSet<>();
        SuperId superid121 = new SuperId(properties121);
		Set<SuperId> ids121 = new TreeSet<>();
		properties121.add(id_47);
		ids121.add(new SuperId(id_47));

		Set<Property> properties122 = new TreeSet<>();
        SuperId superid122 = new SuperId(properties122);
		Set<SuperId> ids122 = new TreeSet<>();
		properties122.add(id_122_epsilon);
		ids122.add(new SuperId(id_122_epsilon));

		Set<Property> properties123 = new TreeSet<>();
        SuperId superid123 = new SuperId(properties123);
		Set<SuperId> ids123 = new TreeSet<>();
		properties123.add(name_123_epsilon);
		ids123.add(new SuperId(name_123_epsilon));

		Set<Property> properties124 = new TreeSet<>();
        SuperId superid124 = new SuperId(properties124);
		Set<SuperId> ids124 = new TreeSet<>();
		properties124.add(price_epsilon);
		ids124.add(new SuperId(price_epsilon));

		Set<Property> properties125 = new TreeSet<>();
        SuperId superid125 = new SuperId(properties125);
		Set<SuperId> ids125 = new TreeSet<>();
		properties125.add(id_47_54);
		ids125.add(new SuperId(id_47_54));

		Set<Property> properties126 = new TreeSet<>();
        SuperId superid126 = new SuperId(properties126);
		Set<SuperId> ids126 = new TreeSet<>();
		properties126.add(length_epsilon);
		ids126.add(new SuperId(length_epsilon));

		Set<Property> properties127 = new TreeSet<>();
        SuperId superid127 = new SuperId(properties127);
		Set<SuperId> ids127 = new TreeSet<>();
		properties127.add(id_47_58);
		ids127.add(new SuperId(id_47_58));

		Set<Property> properties128 = new TreeSet<>();
        SuperId superid128 = new SuperId(properties128);
		Set<SuperId> ids128 = new TreeSet<>();
		properties128.add(pages_epsilon);
		ids128.add(new SuperId(pages_epsilon));

		Set<Property> properties129 = new TreeSet<>();
        SuperId superid129 = new SuperId(properties129);
		Set<SuperId> ids129 = new TreeSet<>();
		properties129.add(id_47_58_62);
		properties129.add(name_65_63);
		ids129.add(new SuperId(id_47_58_62, name_65_63));

		Set<Property> properties130 = new TreeSet<>();
        SuperId superid130 = new SuperId(properties130);
		Set<SuperId> ids130 = new TreeSet<>();
		properties130.add(name_65);
		ids130.add(new SuperId(name_65));

		Set<Property> properties131 = new TreeSet<>();
        SuperId superid131 = new SuperId(properties131);
		Set<SuperId> ids131 = new TreeSet<>();
		properties131.add(name_131_epsilon);
		ids131.add(new SuperId(name_131_epsilon));

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
		SchemaObject _100 = new SchemaObject(new Key(100), "Customer", superid100, ids100, 10, 260);
		SchemaObject _101 = new SchemaObject(new Key(101), "Id", superid101, ids101, 10, 380);
		SchemaObject _102 = new SchemaObject(new Key(102), "Name", superid102, ids102);
		SchemaObject _103 = new SchemaObject(new Key(103), "Tag", superid103, ids103);
		SchemaObject _104 = new SchemaObject(new Key(104), "Surname", superid104, ids104);
		SchemaObject _105 = new SchemaObject(new Key(105), "Address", superid105, ids105);
		SchemaObject _106 = new SchemaObject(new Key(106), "Street", superid106, ids106);
		SchemaObject _107 = new SchemaObject(new Key(107), "City", superid107, ids107);
		SchemaObject _108 = new SchemaObject(new Key(108), "PostalCode", superid108, ids108);
		SchemaObject _109 = new SchemaObject(new Key(109), "Friend", superid109, ids109);
		SchemaObject _110 = new SchemaObject(new Key(110), "Orders", superid110, ids110, 10, 140);
		SchemaObject _111 = new SchemaObject(new Key(111), "Order", superid111, ids111, 130, 140);
		SchemaObject _112 = new SchemaObject(new Key(112), "Number", superid112, ids112, 130, 20);
		SchemaObject _113 = new SchemaObject(new Key(113), "Contact", superid113, ids113, 250, 140);
		SchemaObject _114 = new SchemaObject(new Key(114), "Type", superid114, ids114, 370, 140);
		SchemaObject _115 = new SchemaObject(new Key(115), "Name", superid115, ids115, 370, 20);
		SchemaObject _116 = new SchemaObject(new Key(116), "Value", superid116, ids116, 250, 20);
		SchemaObject _117 = new SchemaObject(new Key(117), "Items", superid117, ids117, 130, 260);
		SchemaObject _118 = new SchemaObject(new Key(118), "Quantity", superid118, ids118, 250, 260);
		SchemaObject _119 = new SchemaObject(new Key(119), "Cart", superid119, ids119);
		SchemaObject _120 = new SchemaObject(new Key(120), "Quantity", superid120, ids120);
		SchemaObject _121 = new SchemaObject(new Key(121), "Product", superid121, ids121, 130, 380);
		SchemaObject _122 = new SchemaObject(new Key(122), "Id", superid122, ids122, 10, 500);
		SchemaObject _123 = new SchemaObject(new Key(123), "Name", superid123, ids123, 250, 500);
		SchemaObject _124 = new SchemaObject(new Key(124), "Price", superid124, ids124, 250, 380);
		SchemaObject _125 = new SchemaObject(new Key(125), "Audiobook", superid125, ids125);
		SchemaObject _126 = new SchemaObject(new Key(126), "Length", superid126, ids126);
		SchemaObject _127 = new SchemaObject(new Key(127), "Book", superid127, ids127);
		SchemaObject _128 = new SchemaObject(new Key(128), "Pages", superid128, ids128);
		SchemaObject _129 = new SchemaObject(new Key(129), "Publishes", superid129, ids129);
		SchemaObject _130 = new SchemaObject(new Key(130), "Publisher", superid130, ids130);
		SchemaObject _131 = new SchemaObject(new Key(131), "Name", superid131, ids131);

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
