/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author pavel.contos
 */
public class Schema {

	private static enum COLUMN_TYPE {
		ATTRIBUTE, PRIMARY_KEY, FOREIGN_KEY, PRIMARY_AND_FOREIGN_KEY;
	}

	public static final class Column {

		private final String name;
		private final String dataType;
		private final int index;
		private Schema.COLUMN_TYPE type;

		private Column(String name, String dataType, int index, Schema.COLUMN_TYPE type) {
			this.name = name;
			this.dataType = dataType;
			this.index = index;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public String getDataType() {
			return dataType;
		}

		public int getIndex() {
			return index;
		}

		public boolean isAttribute() {
			return type == Schema.COLUMN_TYPE.ATTRIBUTE;
		}

		private void setType(Schema.COLUMN_TYPE type) {
			this.type = type;
		}

		public boolean isPrimaryKeyCompound() {
			return type == Schema.COLUMN_TYPE.PRIMARY_KEY || type == Schema.COLUMN_TYPE.PRIMARY_AND_FOREIGN_KEY;
		}

		public boolean isForeignKeyCompound() {
			return type == Schema.COLUMN_TYPE.FOREIGN_KEY || type == Schema.COLUMN_TYPE.PRIMARY_AND_FOREIGN_KEY;
		}
	}

	public static final class PrimaryKey {

		private final List<Integer> compoundIndices;// = new ArrayList<>();
//		private final List<String> compoundNames = new ArrayList<>();

		public PrimaryKey(List<Integer> compoundIndices) {
			this.compoundIndices = compoundIndices;
		}

		public List<Integer> getIndices() {
			return compoundIndices;
		}
	}

	public static final class ForeignKey {

		private final List<Integer> compoundIndices;// = new ArrayList<>();
//		private final List<String> compoundNames = new ArrayList<>();

		public ForeignKey(List<Integer> compoundIndices) {
			this.compoundIndices = compoundIndices;
		}

		public List<Integer> getIndices() {
			return compoundIndices;
		}
	}

	// tohle schema je platne pouze pro relacni schema, protoze ma specificke vlastnosti!
	private final String name;

	private final List<Column> columns = new ArrayList();	// list vsech column v tabulce

	private final List<Schema.PrimaryKey> primaryKeys = new ArrayList();	// nepotrebujes u PK znat jejich nazvy, ale staci ti pouze souradnice! PROC? Protoze jejich jmena tahas z columns a bylo by to duplicitni

	private final Map<String, Schema.ForeignKey> foreignKeys = new TreeMap();	// WARN: Nepotrebujes u FK znat nazvy sloupcu, staci ti pouze souradnice a reference?

	public int getColumnIndex(String columnName) {
		// WARN: Velmi pomala implementace, budes potrebovat mapovani columns!

		for (var column : columns) {
			if (column.getName().equals(columnName)) {
				return column.getIndex();
			}
		}

		return -1;
	}

	public Schema(String name) {
		this.name = name;
	}

	public Schema.Column getColumn(int index) {
		return columns.get(index);
	}

	public int size() {
		return columns.size();
	}

	public int sizePrimaryKeys() {
		return primaryKeys.size();
	}

	public int sizeForeignKeys() {
		return foreignKeys.size();
	}

	public String getName() {
		return name;
	}

	public void addColumn(String name, String dataType, Integer index) {
		columns.add(new Schema.Column(name, dataType, index, COLUMN_TYPE.ATTRIBUTE));
	}

	public void addPrimaryKey(List<Integer> primaryKey) {
		primaryKeys.add(new Schema.PrimaryKey(primaryKey));
		primaryKey.forEach(index -> {
			if (columns.get(index).isForeignKeyCompound()) {
				columns.get(index).setType(COLUMN_TYPE.PRIMARY_AND_FOREIGN_KEY);
			} else {
				columns.get(index).setType(COLUMN_TYPE.PRIMARY_KEY);
			}
		});
	}

	public void addForeignKey(String reference, List<Integer> foreignKey) {
		foreignKeys.put(reference, new Schema.ForeignKey(foreignKey));
		foreignKey.forEach(index -> {
			if (columns.get(index).isPrimaryKeyCompound()) {
				columns.get(index).setType(COLUMN_TYPE.PRIMARY_AND_FOREIGN_KEY);
			} else {
				columns.get(index).setType(COLUMN_TYPE.FOREIGN_KEY);
			}
		});
	}

	public String getColumnName(int index) {
		return columns.get(index).getName();
	}

	public Set<String> getFKReferences() {
		return foreignKeys.keySet();
	}

	public Schema.ForeignKey getForeignKey(String reference) {
		return foreignKeys.get(reference);
	}

	public Schema.PrimaryKey getPrimaryKey(int index) {
		return primaryKeys.get(index);
	}

}
