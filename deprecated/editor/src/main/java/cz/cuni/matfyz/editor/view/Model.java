package cz.cuni.matfyz.editor.view;

import cz.cuni.matfyz.editor.view.cell.AvailableKindCell;
import cz.cuni.matfyz.editor.view.cell.AvailableNameCell;
import cz.cuni.matfyz.editor.view.cell.AvailableObjectCell;
import cz.cuni.matfyz.editor.view.cell.AvailablePropertyCell;
import cz.cuni.matfyz.editor.view.edge.CategoricalEdge;
//import cat.editor.view.cell.CategoricalMorphismCell;
import cz.cuni.matfyz.editor.view.cell.CategoricalObjectCell;
import cz.cuni.matfyz.editor.view.cell.CellType;
import cz.cuni.matfyz.editor.view.cell.Cell;
import cz.cuni.matfyz.editor.view.cell.ERAttributeCell;
import cz.cuni.matfyz.editor.view.cell.EREntityCell;
import cz.cuni.matfyz.editor.view.cell.ERIdentifierCell;
import cz.cuni.matfyz.editor.view.cell.ERRelationshipCell;
import cz.cuni.matfyz.editor.view.cell.ERWeakIdentifierCell;
import cz.cuni.matfyz.editor.view.cell.KindCell;
import cz.cuni.matfyz.editor.view.cell.MongoDBKindCell;
import cz.cuni.matfyz.editor.view.cell.NameCell;
import cz.cuni.matfyz.editor.view.cell.PostgreSQLKindCell;
import cz.cuni.matfyz.editor.view.cell.PropertyCell;
import cz.cuni.matfyz.editor.view.cell.RootCell;
import cz.cuni.matfyz.editor.view.cell.SelectedCell;
import cz.cuni.matfyz.editor.view.cell.SelectedKindCell;
import cz.cuni.matfyz.editor.view.cell.SelectedNameCell;
import cz.cuni.matfyz.editor.view.cell.SelectedPropertyObjectCell;
import cz.cuni.matfyz.editor.view.edge.AvailableCategoricalEdge;
import cz.cuni.matfyz.editor.view.edge.EREdge;
import cz.cuni.matfyz.editor.view.edge.Edge;
import cz.cuni.matfyz.editor.view.edge.EdgeType;
import cz.cuni.matfyz.editor.view.edge.NameCategoricalEdge;
import cz.cuni.matfyz.editor.view.edge.PropertyCategoricalEdge;
import cz.cuni.matfyz.editor.view.edge.PropertyIncompleteCategoricalEdge;
import cz.cuni.matfyz.editor.view.edge.PropertyNonbaseCategoricalEdge;
import cz.cuni.matfyz.editor.view.edge.SelectedCategoricalEdge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model {

	RootCell graphParent;

	List<Cell> allCells;
	List<Cell> addedCells;
	List<Cell> removedCells;

	List<Edge> allEdges;
	List<Edge> addedEdges;
	List<Edge> removedEdges;

	Map<String, Cell> cellMap; // <id,cell>

	public Model() {

		graphParent = new RootCell("_ROOT_");

		// clear model, create lists
		clear();
	}

	public void clear() {

		allCells = new ArrayList<>();
		addedCells = new ArrayList<>();
		removedCells = new ArrayList<>();

		allEdges = new ArrayList<>();
		addedEdges = new ArrayList<>();
		removedEdges = new ArrayList<>();

		cellMap = new HashMap<>(); // <id,cell>

	}

	public void clearAddedLists() {
		addedCells.clear();
		addedEdges.clear();
	}

	public List<Cell> getAddedCells() {
		return addedCells;
	}

	public List<Cell> getRemovedCells() {
		return removedCells;
	}

	public List<Cell> getAllCells() {
		return allCells;
	}

	public List<Edge> getAddedEdges() {
		return addedEdges;
	}

	public List<Edge> getRemovedEdges() {
		return removedEdges;
	}

	public List<Edge> getAllEdges() {
		return allEdges;
	}

	public void addCell(String id, String name, double x, double y, CellType type) {

		Cell cell;
		switch (type) {

			case MAPPING_AVAILABLE_PROPERTY ->
				cell = new AvailablePropertyCell(id, name, x, y);
			case MAPPING_AVAILABLE_KIND ->
				cell = new AvailableKindCell(id, name, x, y);
			case MAPPING_AVAILABLE_NAME ->
				cell = new AvailableNameCell(id, name, x, y);
			case MAPPING_SELECTED_NAME ->
				cell = new SelectedNameCell(id, name, x, y);
			case CATEGORICAL_OBJECT ->
				cell = new CategoricalObjectCell(id, name, x, y);
			case MAPPING_KIND ->
				cell = new KindCell(id, name, x, y);
			case MAPPING_PROPERTY ->
				cell = new PropertyCell(id, name, x, y);
			case MAPPING_SELECTED_KIND ->
				cell = new SelectedKindCell(id, name, x, y);
			case MAPPING_SELECTED_PROPERTY ->
				cell = new SelectedPropertyObjectCell(id, name, x, y);
			case MAPPING_AVAILABLE ->
				cell = new AvailableObjectCell(id, name, x, y);
			case MAPPING_NAME ->
				cell = new NameCell(id, name, x, y);
			case ER_ENTITY ->
				cell = new EREntityCell(id, name, x, y);
			case ER_RELATIONSHIP ->
				cell = new ERRelationshipCell(id, name, x, y);
			case ER_ATTRIBUTE ->
				cell = new ERAttributeCell(id, name, x, y);
			case ER_IDENTIFIER ->
				cell = new ERIdentifierCell(id, name, x, y);
			case ER_WEAK_IDENTIFIER ->
				cell = new ERWeakIdentifierCell(id, name, x, y);
			case SELECTED ->
				cell = new SelectedCell(id, name, x, y);
			case MONGODB_KIND ->
				cell = new MongoDBKindCell(id, name, x, y);
			case POSTGRESQL_KIND ->
				cell = new PostgreSQLKindCell(id, name, x, y);
			default ->
				cell = null;
		}

		addCell(cell);
	}

	private void addCell(Cell cell) {

		addedCells.add(cell);

		cellMap.put(cell.getCellId(), cell);

	}

	public void addEdge(String id, String sourceId, String targetId, EdgeType type) {
		Cell sourceCell = cellMap.get(sourceId);
		Cell targetCell = cellMap.get(targetId);

		Edge edge;

		switch (type) {
			case CATEGORICAL ->
				edge = new CategoricalEdge(id, sourceCell, targetCell);
			case AVAILABLE_CATEGORICAL ->
				edge = new AvailableCategoricalEdge(id, sourceCell, targetCell);
			case PROPERTY_CATEGORICAL ->
				edge = new PropertyCategoricalEdge(id, sourceCell, targetCell);
			case PROPERTY_NONBASE_CATEGORICAL ->
				edge = new PropertyNonbaseCategoricalEdge(id, sourceCell, targetCell);
			case PROPERTY_INCOMPLETE_CATEGORICAL ->
				edge = new PropertyIncompleteCategoricalEdge(id, sourceCell, targetCell);
			case SELECTED_CATEGORICAL ->
				edge = new SelectedCategoricalEdge(id, sourceCell, targetCell);
			case NAME_CATEGORICAL ->
				edge = new NameCategoricalEdge(id, sourceCell, targetCell);
			case ER ->
				edge = new EREdge(id, sourceCell, targetCell);
			default ->
				edge = null;
		}
		addedEdges.add(edge);

	}

	/**
	 * Attach all cells which don't have a parent to graphParent
	 *
	 * @param cellList
	 */
	public void attachOrphansToGraphParent(List<Cell> cellList) {

		for (Cell cell : cellList) {
			if (cell.getCellParents().isEmpty()) {
				graphParent.addCellChild(cell);
			}
		}

	}

	/**
	 * Remove the graphParent reference if it is set
	 *
	 * @param cellList
	 */
	public void disconnectFromGraphParent(List<Cell> cellList) {

		for (Cell cell : cellList) {
			graphParent.removeCellChild(cell);
		}
	}

	public void merge() {

		// cells
		allCells.addAll(addedCells);
		allCells.removeAll(removedCells);

		addedCells.clear();
		removedCells.clear();

		// edges
		allEdges.addAll(addedEdges);
		allEdges.removeAll(removedEdges);

		addedEdges.clear();
		removedEdges.clear();

	}

}
