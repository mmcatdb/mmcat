/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms;

import cat.transformations.category.EntityObject;
import cat.transformations.category.InstanceCategory;
import cat.transformations.commons.Pair;
import cat.transformations.model.RelationalInstance;
import cat.transformations.model.RelationalTable;
import cat.transformations.model.Schema;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.contos
 */
public abstract class Algorithms {

	public static RelationalInstance instToRel(InstanceCategory category) {
		var relationalInstance = new RelationalInstance();

// ----- BEGINNING -----------------------------------------------------------------------------------------------------
		var objectNames = category.getObjectNames();
		for (var objectName : objectNames) {
//			System.out.println("Processing " + objectName);
			// iteruj vsechny objekty a pokud se jedna o entitni typ, tak na zaklade nej vytvor tabulku a schema a pridej do ni postupne sloupce, cizi klice a tak dale
			if (!category.isEntity(objectName)) {
//				System.out.println("NOT ENTITY!");
				continue;
			}
			// WARN: podle vseho musis nejprve pridavat sloupce, a teprve potom urcovat cizi klice/foreign klice... bylo by lepsi udelat to jinak
			int columnIndex = 0;
			Schema schema = new Schema(objectName);

			var outgoingMorphisms = category.findOutMorphisms(objectName);
			for (var morphism : outgoingMorphisms) {
				if (category.isEntity(morphism.getCodomain())) {
					// jedna se o FK, takze nalezni sloupce a podle toho vytvor hodnoty a rovnou mapovani
					var object = category.getObject(morphism.getCodomain());	// API morfismu by melo umoznovat primy pristup k objektu, nejen jmeno!
					List<Integer> foreignKey = new ArrayList<>();
					// zpracuj prvni sloupec FK
					schema.addColumn(object.getName() + "ERROR:PRIDEJ_DALSI_SLOUPCE_FK", null, columnIndex);
					foreignKey.add(columnIndex);
					columnIndex++;
					// zpracuj dalsi sloupce FK
//					foreignKey.add(2);
					schema.addForeignKey("Customers", foreignKey);
				} else {
					// jinak se v tuhle chvili jedna o atribut, takze ho pridej... vztahy ignorujeme
					var object = category.getObject(morphism.getCodomain());
					schema.addColumn(object.getName(), null, columnIndex++);
				}
			}

			var object = category.getObject(objectName);

			// TODO: PK atribut name!
			System.out.println("TODO: Klice, ale ted je nepotrebujes, protoze sloupce to bude obsahovat! Jen potrebujes kvuli schematu");

//			List<Integer> primaryKey = new ArrayList<>();
//			primaryKey.add();
//			primaryKey.add(1);
//			schema.addPrimaryKey(primaryKey);
			RelationalTable table = new RelationalTable(schema);
			relationalInstance.addTable(table);

			// ted mas vytvoreno schema, tak jej napln daty!
			// daty muzes naplnit uz v ramci jedne tabulky, takze nejprve projdi vsechny identifikatory a pote projdi dal!
			// tady budes potrebovat na pozadi efektivnejsi implementaci, tohle je hrozne naivni a pomale
			for (int index = 0; index < object.size(); ++index) {
				// zjisti pocet atributu, abys podle toho vytvarel 
				int columnsCount = category.getColumnsCount(objectName);
//				System.out.println("POCET ATRIBUTU: " + columnsCount + " : U TABULKY " + objectName);
				// nebo to ziskavat podle indexu radku?
				Object[] record = category.getRelationalRecord(objectName, index);

				if (record != null) {
					table.addRecord(record);
				}
			}

		}
// ----- END -----------------------------------------------------------------------------------------------------------
		return relationalInstance;
	}

	private static String morphismName(String domain, String codomain) {
		return domain + "->" + codomain;
	}

	public static InstanceCategory relToInst(RelationalInstance relational) {
		var instanceCategory = new InstanceCategory();	// builder/factory, empty category

		List<Pair<String, String>> foreignKeys = new ArrayList<>();

		// iterate through all the tables of the relational instance (one-by-one process all the tables)
		for (var currentTable : relational.getTables()) {
			// for each table insert 1 entityObject in the instance category
			var currentSchema = currentTable.getSchema();
			var currentTableName = currentSchema.getName();
			instanceCategory.addEntityObject(currentTableName);

			// for each attribute that is not a foreign key compound insert 1 attributeObject in the instance category
			// add also one attribute morphism for each (table, attribute) pair
			for (var index = 0; index < currentSchema.size(); ++index) {
				var currentColumn = currentSchema.getColumn(index);
				if (!currentColumn.isForeignKeyCompound()) {
					System.out.println("CURRENT COLUMN ADDED: " + currentColumn.getName());
					instanceCategory.addAttributeObject(currentColumn.getName());
					String morphismName = morphismName(currentTableName, currentColumn.getName());
					instanceCategory.addAttributeMorphism(morphismName, currentTableName, currentColumn.getName());
				} else {
					System.out.println("CURRENT COLUMN SKIPPED: " + currentColumn.getName());
				}
			}

			// queue foreign keys - references might not be processed
			var foreignKeyReferences = currentSchema.getFKReferences();
			for (var reference : foreignKeyReferences) {
				foreignKeys.add(new Pair<>(currentTableName, reference));
			}
		}

		// for each foreign key add a morphism (table, referencedTable)
		for (var tuple : foreignKeys) {
			instanceCategory.addRelationshipMorphism(morphismName(tuple.getX(), tuple.getY()), tuple.getX(), tuple.getY());
		}

		// iterate through all the tables of the relational instance (one-by-one process all the tables)
		for (var currentTable : relational.getTables()) {
			var currentSchema = currentTable.getSchema();
			var currentTableName = currentSchema.getName();
			var foreignKeyReferences = currentSchema.getFKReferences();

			// one-by-one process all the rows in the current table
			for (int rowIndex = 0; rowIndex < currentTable.size(); ++rowIndex) {
				var currentRow = currentTable.getRecord(rowIndex);

				// first, for each identifier process its compounds ("extend active domain" of identifier)
				List<List<Object>> keys = new ArrayList<>();
				for (int keyIndex = 0; keyIndex < currentSchema.sizePrimaryKeys(); ++keyIndex) {
					var currentKey = currentSchema.getPrimaryKey(keyIndex);

					List<Object> currentKeyAttributes = new ArrayList<>();
					for (var pkAttributeIndex : currentKey.getIndices()) {
						currentKeyAttributes.add(currentRow[pkAttributeIndex]);
					}
					keys.add(currentKeyAttributes);
				}
				var key = new EntityObject.EntityValue(keys);
				instanceCategory.addEntityObjectValue(currentTableName, key);

				// second, process all the ordinary (attribute) columns, i.e. extend particular attribute active domains and extend morphism mappings
				// here, we do not process foreign key compounds
				// NOTE: For this purpose, we only need to distinguish FK compound and ordinary attributes; i.e. we do not need to distinguish PK compounds
				for (var attIndex = 0; attIndex < currentSchema.size(); ++attIndex) {
					var currentColumn = currentSchema.getColumn(attIndex);
					if (!currentColumn.isForeignKeyCompound()) {

						instanceCategory.addAttributeObjectValue(currentSchema.getColumnName(attIndex), currentRow[attIndex]);
						String morphismName = morphismName(currentTableName, currentSchema.getColumnName(attIndex));
						instanceCategory.addAttributeMorphismMapping(morphismName, currentTableName, currentSchema.getColumnName(attIndex), key, currentRow[attIndex]);

					}
				}

				// finally, process all the (remaining) FK compound attributes
				for (var reference : foreignKeyReferences) {
					List<Object> currentForeignKeyAttributes = new ArrayList<>();

					var currentForeignKey = currentSchema.getForeignKey(reference);
					for (var index : currentForeignKey.getIndices()) {
						currentForeignKeyAttributes.add(currentRow[index]);
					}

					instanceCategory.addRelationshipMorphismMapping(morphismName(currentTableName, reference), currentTableName, reference, key, currentForeignKeyAttributes);
					System.out.println("A CO MORFISMY OBRACENYM SMEREM? NEKDY TABULKA Z DRUHE STRANY MA REFERENCI SAMA, NEKDY NE");

				}

			}

		}

		return instanceCategory;
	}

}
