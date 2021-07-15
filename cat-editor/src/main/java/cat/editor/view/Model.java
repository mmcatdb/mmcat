/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor.view;

import cat.editor.view.cell.AvailableKindCell;
import cat.editor.view.cell.AvailableNameCell;
import cat.editor.view.cell.AvailableObjectCell;
import cat.editor.view.cell.AvailablePropertyCell;
import cat.editor.view.edge.CategoricalEdge;
//import cat.editor.view.cell.CategoricalMorphismCell;
import cat.editor.view.cell.CategoricalObjectCell;
import cat.editor.view.cell.CellType;
import cat.editor.view.cell.Cell;
import cat.editor.view.cell.ERAttributeCell;
import cat.editor.view.cell.EREntityCell;
import cat.editor.view.cell.ERIdentifierCell;
import cat.editor.view.cell.ERRelationshipCell;
import cat.editor.view.cell.KindCell;
import cat.editor.view.cell.NameCell;
import cat.editor.view.cell.PropertyCell;
import cat.editor.view.cell.RootCell;
import cat.editor.view.cell.SelectedKindCell;
import cat.editor.view.cell.SelectedNameCell;
import cat.editor.view.cell.SelectedPropertyObjectCell;
import cat.editor.view.edge.EREdge;
import cat.editor.view.edge.Edge;
import cat.editor.view.edge.EdgeType;
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
			default ->
				cell = null;
		}

		addCell(cell);
	}

	private void addCell(Cell cell) {

		addedCells.add(cell);

		cellMap.put(cell.getCellId(), cell);

	}

//	public void addEdge(String id, String sourceId, String targetId) {
//
//		Cell sourceCell = cellMap.get(sourceId);
//		Cell targetCell = cellMap.get(targetId);
//
//		CategoricalEdge edge = new CategoricalEdge(id, sourceCell, targetCell);
//
//		addedEdges.add(edge);
//
//	}

	public void addEdge(String id, String sourceId, String targetId, EdgeType type) {
		Cell sourceCell = cellMap.get(sourceId);
		Cell targetCell = cellMap.get(targetId);

		Edge edge;

		switch (type) {
			case CATEGORICAL ->
				edge = new CategoricalEdge(id, sourceCell, targetCell);
			case ER ->
				edge = new EREdge(sourceCell, targetCell);
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
