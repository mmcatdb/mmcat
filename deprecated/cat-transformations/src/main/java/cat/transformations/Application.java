package cat.transformations;

import cat.transformations.algorithms.Algorithms;
import cat.transformations.model.AbstractTable;
import cat.transformations.model.CSVTable;
import cat.transformations.category.InstanceCategory;
import cat.transformations.model.RelationalInstance;
import cat.transformations.model.RelationalTable;
import cat.transformations.model.Schema;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.contos
 */
public class Application {

	public static void scenarioCSV() {
		AbstractTable csv = new CSVTable("People", "data.csv");
		System.out.println(csv);

		RelationalInstance instance = new RelationalInstance();
		instance.addTable(csv);

		InstanceCategory category = Algorithms.relToInst(instance);

		System.out.println(category);

		System.out.println("---------- ---------- ----------");

		RelationalInstance result = Algorithms.instToRel(category);
		System.out.println("COUNT TABLES: " + result.countTables());
		for (int index = 0; index < result.countTables(); ++index) {
			System.out.println(result.getTable(index));
		}
	}

	public static void scenarioRelational() {
		String[][] rows = {
			{"Alfalfa", "Aloysius", "123-45-6789", "40.0", "90.0", "100.0", "83.0", "49.0", "D-"},
			{"Alfred", "University", "123-12-1234", "41.0", "97.0", "96.0", "97.0", "48.0", "D+"},
			{"Gerty", "Gramma", "567-89-0123", "41.0", "80.0", "60.0", "40.0", "44.0", "C"},
			{"Android", "Electric", "087-65-4321", "42.0", "23.0", "36.0", "45.0", "47.0", "B-"},
			{"Bumpkin", "Fred", "456-78-9012", "43.0", "78.0", "88.0", "77.0", "45.0", "A-"},
			{"Rubble", "Betty", "234-56-7890", "44.0", "90.0", "80.0", "90.0", "46.0", "C-"},
			{"Noshow", "Cecil", "345-67-8901", "45.0", "11.0", "-1.0", "4.0", "43.0", "F"},
			{"Buff", "Bif", "632-79-9939", "46.0", "20.0", "30.0", "40.0", "50.0", "B+"},
			{"Airpump", "Andrew", "223-45-6789", "49.0", "1.0", "90.0", "100.0", "83.0", "A"},
			{"Backus", "Jim", "143-12-1234", "48.0", "1.0", "97.0", "96.0", "97.0", "A+"},
			{"Carnivore", "Art", "565-89-0123", "44.0", "1.0", "80.0", "60.0", "40.0", "D+"},
			{"Dandy", "Jim", "087-75-4321", "47.0", "1.0", "23.0", "36.0", "45.0", "C+"},
			{"Elephant", "Ima", "456-71-9012", "45.0", "1.0", "78.0", "88.0", "77.0", "B-"},
			{"Franklin", "Benny", "234-56-2890", "50.0", "1.0", "90.0", "80.0", "90.0", "B-"},
			{"George", "Boy", "345-67-3901", "40.0", "1.0", "11.0", "-1.0", "4.0", "B"},
			{"Heffalump", "Harvey", "632-79-9439", "30.0", "1.0", "20.0", "30.0", "40.0", "C"}
		};

		String[] columns = {"Lastname", "Firstname", "SSN", "Test1", "Test2", "Test3", "Test4", "Final", "Grade"};

		String[][] rowsOrders = {
			{"1", "Alfaalfa", "Aloysius", "250.0", "C1"},
			{"2", "Alfred", "University", "478.0", "C2"},
			{"3", "Franklin", "Benny", "675.0", "C2"},
			{"4", "Airpump", "Andrew", "456.0", "C2"},
			{"5", "Alfaalfa", "Aloysius", "167.0", "C2"}
		};

		String[] columnsOrders = {"OrderId", "LastnameREF", "FirstnameREF", "TotalPrice", "CarREF"};
		// WARN: POZOR NA STEJNA JMENA ATRIBUTU! PREPISES TO V MAPE!

		String[][] rowsCars = {
			{"C1", "Skoda", "880000"},
			{"C2", "Ferrari", "2200000"},
			{"C3", "Porsche", "1200000"},
			{"C4", "Skoda", "120000"},
			{"C5", "Skoda", "510000"},
			{"C6", "Skoda", "590000"},
			{"C7", "Fiat", "410000"},
			{"C8", "Skoda", "300000"},
			{"C9", "Volvo", "250000"},
			{"C0", "Hyundai", "150000"}
		};

		String[] columnsCars = {"CarId", "Type", "Price"};

		// ----- TABLE Customers -----
		Schema schemaCustomers = new Schema("Customers");
		int index = 0;
		for (var column : columns) {
			schemaCustomers.addColumn(column, null, index++);
		}

		List<Integer> primaryKey = new ArrayList<>();
		primaryKey.add(0);
		primaryKey.add(1);
		schemaCustomers.addPrimaryKey(primaryKey);
		primaryKey = new ArrayList<>();
		primaryKey.add(1);
		primaryKey.add(2);
		schemaCustomers.addPrimaryKey(primaryKey);

		AbstractTable customersTable = new RelationalTable(schemaCustomers);
		for (var row : rows) {
			customersTable.addRecord(row);
		}

		System.out.println(customersTable);

		// ----- TABLE Orders -----
		Schema schemaOrders = new Schema("Orders");
		index = 0;
		for (var column : columnsOrders) {
			schemaOrders.addColumn(column, null, index);
		}

		primaryKey = new ArrayList<>();
		primaryKey.add(0);
		schemaOrders.addPrimaryKey(primaryKey);

		List<Integer> foreignKey = new ArrayList<>();
		foreignKey.add(1);
		foreignKey.add(2);
		schemaOrders.addForeignKey("Customers", foreignKey);
		foreignKey = new ArrayList<>();
		foreignKey.add(4);
		schemaOrders.addForeignKey("Cars", foreignKey);

		AbstractTable ordersTable = new RelationalTable(schemaOrders);
		for (var row : rowsOrders) {
			ordersTable.addRecord(row);
		}

		System.out.println(ordersTable);

		// ----- TABLE Cars ----------
		Schema carsSchema = new Schema("Cars");
		index = 0;
		for (var column : columnsCars) {
			carsSchema.addColumn(column, null, index);
		}

		primaryKey = new ArrayList<>();
		primaryKey.add(0);
		carsSchema.addPrimaryKey(primaryKey);

		AbstractTable carsTable = new RelationalTable(carsSchema);
		for (var row : rowsCars) {
			carsTable.addRecord(row);
		}

		System.out.println(carsTable);

		// ----- INSTANCE -----
		RelationalInstance instance2 = new RelationalInstance();
		instance2.addTable(customersTable);
		instance2.addTable(ordersTable);
		instance2.addTable(carsTable);

		InstanceCategory category2 = Algorithms.relToInst(instance2);

		System.out.println(category2);
		
		System.out.println("---------- ---------- ----------");

		RelationalInstance result = Algorithms.instToRel(category2);
		System.out.println("COUNT TABLES: " + result.countTables());
		for (index = 0; index < result.countTables(); ++index) {
			System.out.println(result.getTable(index));
		}
	}

	public static void main(String... args) {
//		scenarioCSV();
		scenarioRelational();

	}

}
