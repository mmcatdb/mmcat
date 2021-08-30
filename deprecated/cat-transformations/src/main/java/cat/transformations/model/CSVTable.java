/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.model;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pavel.contos
 */
public class CSVTable extends AbstractTable {

	private final List<Object[]> table = new ArrayList();

	private int[] maxLengths = null;

	private Schema schema;

	public CSVTable() {
	}

	public CSVTable(String tableName, String resource) {

		try (CSVReader reader = new CSVReader(new FileReader(resource))) {
			String[] record;
			boolean schemaProcessed = false;
			while ((record = reader.readNext()) != null) {
				if (!schemaProcessed) {
					schemaProcessed = processSchema(tableName, record);
				} else {
					addRecord(record);
				}
			}
		} catch (FileNotFoundException ex) {
			System.out.println("FileNotFoundException");
			Logger.getLogger(CSVTable.class.getName()).log(Level.SEVERE, null, ex);
		} catch (IOException ex) {
			System.out.println("IOException");
			Logger.getLogger(CSVTable.class.getName()).log(Level.SEVERE, null, ex);
		} catch (CsvValidationException ex) {
			System.out.println("CsvValidationException");
			Logger.getLogger(CSVTable.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	@Override
	public Schema getSchema() {
		return schema;
	}

	private boolean processSchema(String name, String[] record) {
		schema = new Schema(name);

		List<Integer> keyIndices = new ArrayList<>();

		int index = 0;
		for (var value : record) {
			// process PK - all columns
			keyIndices.add(index);
			// process FK - none, takze ani nevolas addColumn

			// process attribute
			schema.addColumn(value, null, index);

			index++;
		}

		schema.addPrimaryKey(keyIndices);

		// initialize maxLengths
		maxLengths = new int[this.schema.size()];

		for (index = 0; index < schema.size(); ++index) {
			if (schema.getColumnName(index).length() > maxLengths[index]) {
				maxLengths[index] = schema.getColumnName(index).length();
			}
		}

		return true;
	}

	@Override
	public final void addRecord(Object[] record) {
		if (record.length != schema.size()) {
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
	public Object[] getRecord(int id) {
		return table.get(id);
	}

	@Override
	public int size() {
		return table.size();
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
