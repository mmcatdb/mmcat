/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author pavel.contos
 */
public class RelationalTable extends AbstractTable {

	public static final class PrimaryKey implements Comparable<PrimaryKey> {

		private List<List<Comparable>> setOfIdentifiers;

		public PrimaryKey(List<List<Comparable>> setOfIdentifiers) {
			this.setOfIdentifiers = setOfIdentifiers;
		}

		public int size() {
			return setOfIdentifiers.size();
		}

		public List<Comparable> getIdentifier(int index) {
			return setOfIdentifiers.get(index);
		}

		private void compareIdentifiers(List<Comparable> idA, List<Comparable> idB) {
			
		}
		
		@Override
		public int compareTo(PrimaryKey other) {
			for (var identifier : setOfIdentifiers) {
				for (var otherIdentifier : other.setOfIdentifiers) {
					// ----- POROVNAVANI KLICU ----- ----- -----
					int score = 0;
					for (var attribute : identifier) {
						for (var otherAttribute : identifier) {
							if (attribute.compareTo(otherAttribute) == 0) {
								score += 1;
								continue;
							} else {

							}
						}
					}
					// ----- POROVNAVANI KLICU ----- ----- -----
				}
			}
			return 0;
		}
	}

	private final List<Object[]> table = new ArrayList();

	private final Map<PrimaryKey, Object[]> table2 = new TreeMap<>();

	private int[] maxLengths = null;

	private Schema schema;

	public RelationalTable() {
	}

	public RelationalTable(String tableName, Object TODO_RESOURCE) {
		// fetch schema
		// create schema object - name, columns, constraints
		// set schema and initiate maxLengths
		// add records to the table
	}

	public RelationalTable(Schema schema) {
		this.schema = schema;

		// initialize maxLengths
		maxLengths = new int[this.schema.size()];

		for (int index = 0; index < schema.size(); ++index) {
			if (schema.getColumnName(index).length() > maxLengths[index]) {
				maxLengths[index] = schema.getColumnName(index).length();
			}
		}
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	@Override
	public Object[] getRecord(int id) {
		return table.get(id);
	}

	@Override
	public int size() {
		return table.size();
	}

	@Override
	public void addRecord(Object[] record) {
		if (record.length != schema.size()) {
//			System.out.println("NESPRAVNA VELIKOST: " + record.length + " != " + schema.size() + " AT: " + record[0].toString());
			// TODO: nespravna velikost pridavaneho radku, throw error
		}

		table.add(record);

		for (int index = 0; index < record.length; ++index) {
			if (record[index].toString().length() > maxLengths[index]) {
				maxLengths[index] = record[index].toString().length();
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		// print header (schema)
		boolean firstColumn = true;
		for (int index = 0; index < schema.size(); ++index) {
			if (firstColumn) {
				firstColumn = !firstColumn;
			} else {
				builder.append(" | ");
			}

			var column = schema.getColumnName(index);
			int diff = maxLengths[index] - column.length();

			builder.append(column);

			while (diff > 0) {
				builder.append(" ");
				--diff;
			}
		}
		builder.append("\n");

		firstColumn = true;
		for (int index = 0; index < schema.size(); ++index) {
			if (firstColumn) {
				firstColumn = !firstColumn;
			} else {
				builder.append("-+-");
			}
			for (int current = 0; current < maxLengths[index]; ++current) {
				builder.append("-");
			}
		}

		builder.append("\n");

		// print data
		boolean firstRecord = true;
		for (Object[] record : table) {
			if (firstRecord) {
				firstRecord = !firstRecord;
			} else {
				builder.append("\n");
			}

			boolean firstValue = true;
			for (int index = 0; index < record.length; ++index) {
				if (firstValue) {
					firstValue = !firstValue;
				} else {
					builder.append(" | ");
				}

				int diff = maxLengths[index] - record[index].toString().length();

				builder.append(record[index]);

				while (diff > 0) {
					builder.append(" ");
					--diff;
				}

			}
		}

		return builder.toString();
	}

}
