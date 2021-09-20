/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.editor.model;

import cz.cuni.matfyz.core.schema.SchemaCategory;
import cz.cuni.matfyz.editor.model.widgets.CategoricalMorphismWidget;
import cz.cuni.matfyz.editor.model.widgets.CategoricalObjectWidget;
import cz.cuni.matfyz.editor.model.widgets.RootWidget;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author pavel.koupil
 */
public class Model {

	public static Model load(SchemaCategory category) {
		Model result = new Model();
		for (var object : category.objects()) {
			result.addWidget(object.objectId() + "", object.label(), object.x(), object.y(), WidgetType.CATEGORICAL_OBJECT);
		}

		for (var edge : category.morphisms()) {
			result.addEdge(edge.signature().toString(), edge.dom().objectId() + "", edge.cod().objectId() + "", EdgeType.CATEGORICAL);
		}

		return result;
	}

	private Widget parent;

	private List<Widget> widgets = new ArrayList<>();
	private List<Widget> addedWidgets = new ArrayList<>();
	private List<Widget> removedWidgets = new ArrayList<>();

	private List<Edge> edges = new ArrayList<>();
	private List<Edge> addedEdges = new ArrayList<>();
	private List<Edge> removedEdges = new ArrayList<>();

	private Map<String, Widget> quickAccess = new TreeMap<>();

	public Model() {

		parent = new RootWidget("_ROOT_");

	}

	public void clear() {
		widgets.clear();
		addedWidgets.clear();
		removedWidgets.clear();
		edges.clear();
		addedEdges.clear();
		removedEdges.clear();
		quickAccess.clear();
	}

	public void clearAddedLists() {
		addedWidgets.clear();
		addedEdges.clear();
	}

	public List<Widget> getAddedWidgets() {
		return addedWidgets;
	}

	public List<Widget> getRemovedWidgets() {
		return removedWidgets;
	}

	public List<Widget> getWidgets() {
		return widgets;
	}

	public List<Edge> getAddedEdges() {
		return addedEdges;
	}

	public List<Edge> getRemovedEdges() {
		return removedEdges;
	}

	public List<Edge> getEdges() {
		return edges;
	}

	public void addWidget(String id, String name, double x, double y, WidgetType type) {

		Widget widget;
		switch (type) {

//			case MAPPING_AVAILABLE_PROPERTY ->
//				widget = new AvailablePropertyCell(id, name, x, y);
//			case MAPPING_AVAILABLE_KIND ->
//				widget = new AvailableKindCell(id, name, x, y);
//			case MAPPING_AVAILABLE_NAME ->
//				widget = new AvailableNameCell(id, name, x, y);
//			case MAPPING_SELECTED_NAME ->
//				widget = new SelectedNameCell(id, name, x, y);
//			case CATEGORICAL_OBJECT ->
//				widget = new CategoricalObjectCell(id, name, x, y);
//			case MAPPING_KIND ->
//				widget = new KindCell(id, name, x, y);
//			case MAPPING_PROPERTY ->
//				widget = new PropertyCell(id, name, x, y);
//			case MAPPING_SELECTED_KIND ->
//				widget = new SelectedKindCell(id, name, x, y);
//			case MAPPING_SELECTED_PROPERTY ->
//				widget = new SelectedPropertyObjectCell(id, name, x, y);
//			case MAPPING_AVAILABLE ->
//				widget = new AvailableObjectCell(id, name, x, y);
//			case MAPPING_NAME ->
//				widget = new NameCell(id, name, x, y);
//			case ER_ENTITY ->
//				widget = new EREntityCell(id, name, x, y);
//			case ER_RELATIONSHIP ->
//				widget = new ERRelationshipCell(id, name, x, y);
//			case ER_ATTRIBUTE ->
//				widget = new ERAttributeCell(id, name, x, y);
//			case ER_IDENTIFIER ->
//				widget = new ERIdentifierCell(id, name, x, y);
//			case ER_WEAK_IDENTIFIER ->
//				widget = new ERWeakIdentifierCell(id, name, x, y);
//			case SELECTED ->
//				widget = new SelectedCell(id, name, x, y);
//			case MONGODB_KIND ->
//				widget = new MongoDBKindCell(id, name, x, y);
//			case POSTGRESQL_KIND ->
//				widget = new PostgreSQLKindCell(id, name, x, y);
			default ->
//				widget = null;
				widget = new CategoricalObjectWidget("NAME", id, name, x, y, 50, 50);
		}

		addWidget(widget);
	}

	public void addWidget(Widget widget) {
		addedWidgets.add(widget);
		quickAccess.put(widget.getId(), widget);
	}

	public void addEdge(String id, String sourceId, String targetId, EdgeType type) {
		Widget source = quickAccess.get(sourceId);
		Widget target = quickAccess.get(targetId);

		Edge edge;

		switch (type) {
//			case CATEGORICAL ->
//				edge = new CategoricalEdge(id, source, target);
//			case AVAILABLE_CATEGORICAL ->
//				edge = new AvailableCategoricalEdge(id, source, target);
//			case PROPERTY_CATEGORICAL ->
//				edge = new PropertyCategoricalEdge(id, source, target);
//			case PROPERTY_NONBASE_CATEGORICAL ->
//				edge = new PropertyNonbaseCategoricalEdge(id, source, target);
//			case PROPERTY_INCOMPLETE_CATEGORICAL ->
//				edge = new PropertyIncompleteCategoricalEdge(id, source, target);
//			case SELECTED_CATEGORICAL ->
//				edge = new SelectedCategoricalEdge(id, source, target);
//			case NAME_CATEGORICAL ->
//				edge = new NameCategoricalEdge(id, source, target);
//			case ER ->
//				edge = new EREdge(id, source, target);
			default ->
				edge = new CategoricalMorphismWidget(id, source, target);
//				edge = null;
		}
		System.out.println("TODO: ODKOMENTUJ EDGE V MODELU! JINAK SE NEBUDOU PRIDAVAT");
		addedEdges.add(edge);

	}

//	/**
//	 * Attach all cells which don't have a parent to graphParent
//	 *
//	 * @param orphans
//	 */
//	public void attachOrphansToGraphParent(List<Widget> orphans) {
//
//		for (Widget widget : orphans) {
//			if (widget.getParents().isEmpty()) {
//				parent.addChild(widget);
//			}
//		}
//
//	}
//
//	/**
//	 * Remove the graphParent reference if it is set
//	 *
//	 * @param widgets
//	 */
//	public void disconnectFromGraphParent(List<Widget> widgets) {
//
//		widgets.forEach(widget -> {
//			parent.removeChild(widget);
//		});
//	}
	public void merge() {
		widgets.addAll(addedWidgets);
		widgets.removeAll(removedWidgets);

		addedWidgets.clear();
		removedWidgets.clear();

		edges.addAll(addedEdges);
		edges.removeAll(removedEdges);

		addedEdges.clear();
		removedEdges.clear();
	}

}
