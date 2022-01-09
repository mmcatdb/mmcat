package cat.transformations.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author pavel.contos
 */
public class RelationalInstance extends AbstractInstance {

	private final List<AbstractTable> tables = new ArrayList<>();

	public Set<String> getTableNames() {
		Set<String> result = new TreeSet<>();

		for (var table : tables) {
			result.add(table.getSchema().getName());
		}

		return result;
	}
	
	public void addTable(AbstractTable table) {
		tables.add(table);
	}

	public AbstractTable getTable(int index) {
		return tables.get(index);
	}

	public int countTables() {
		return tables.size();
	}

	public List<AbstractTable> getTables() {
		return tables;
	}

}
