/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.editor.representation;

import cz.cuni.matfyz.editor.controller.MouseGestures;
import cz.cuni.matfyz.editor.model.Edge;
import cz.cuni.matfyz.editor.model.Model;
import cz.cuni.matfyz.editor.model.Widget;
import cz.cuni.matfyz.editor.model.widgets.CategoricalMorphismWidget;
import cz.cuni.matfyz.editor.model.widgets.CategoricalObjectWidget;
import cz.cuni.matfyz.editor.representation.widgets.CategoricalMorphismRepresentation;
import cz.cuni.matfyz.editor.representation.widgets.CategoricalObjectWidgetRepresentation;
import cz.cuni.matfyz.editor.view.CellLayer;
import cz.cuni.matfyz.editor.view.ZoomableScrollPane;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 *
 * @author pavel.koupil
 */
public class MappingRepresentation extends ZoomableScrollPane {

	private Model model;

//	private Group canvas; = content!
	private MouseGestures mouseGestures;
	private CellLayer contentPane;

	public ZoomableScrollPane getScrollPane() {
		return this;
	}

	public MappingRepresentation(Model model) {
		super(new Group());
		this.model = model;

		contentPane = new CellLayer();

		content.getChildren().add(contentPane);

		this.setFitToWidth(true);
		this.setFitToHeight(true);
		mouseGestures = new MouseGestures(this);
	}

	public void setGestures(MouseGestures mouseGestures) {
		this.mouseGestures = mouseGestures;
	}

	public Pane getCellLayer() {
		return contentPane;
	}

	public Model getModel() {
		return model;
	}

	public void beginUpdate() {
	}

	// WARN: MUSIS ODTUD ODSTRANOVAT HRANY, KTERE UZ NEPOUZIVAS!
	private Map<String, WidgetRepresentation> quickAccess = new TreeMap<>();

	public void endUpdate() {

		// add components to graph pane
		List<WidgetRepresentation> added = new ArrayList<>();

		for (Widget widget : model.getAddedWidgets()) {
			WidgetRepresentation representation = new CategoricalObjectWidgetRepresentation((CategoricalObjectWidget) widget);
			added.add(representation);
			quickAccess.put(representation.getModel().getId(), representation);
		}

//		if (contentPane == null) {
//			System.out.println("contentPane je NULL");
//		}
//		if (contentPane.getChildren() == null) {
//			System.out.println("jeho getChildren je NULL");
//		}
		for (WidgetRepresentation representation : added) {
//			System.out.println(representation == null ? "NULL" : representation.toString());
			contentPane.getChildren().add(representation);
		}

		for (Edge edge : model.getAddedEdges()) {
			EdgeRepresentation representation = new CategoricalMorphismRepresentation((CategoricalMorphismWidget) edge, quickAccess.get(edge.getSource().getId()), quickAccess.get(edge.getTarget().getId()));
			contentPane.getChildren().add(representation);
		}

		// remove components from graph pane
		System.out.println("MappingRepresentation -> ODSTRAN SPRAVNE REPRESENTATION OBJEKTY, NE MODELOVE Z CONTENT PANE!");
		contentPane.getChildren().removeAll(model.getRemovedWidgets());	// WARN: ERROR - TOHLE MUSIS DODELAT SPRAVNE
		contentPane.getChildren().removeAll(model.getRemovedEdges());

		// enable dragging of cells
		for (WidgetRepresentation representation : added) {
			mouseGestures.makeDraggable(representation);

		}

		// every cell must have a parent, if it doesn't, then the graphParent is
		// the parent
		System.out.println("TODO: ATTACH TO PARENT!");
//		model.attachOrphansToGraphParent(model.getAddedWidgets());
//
//		// remove reference to graphParent
//		model.disconnectFromGraphParent(model.getRemovedWidgets());

		// merge added & removed cells with all cells
		model.merge();

	}

	public double getScale() {
		return this.getScaleValue();
	}

}
