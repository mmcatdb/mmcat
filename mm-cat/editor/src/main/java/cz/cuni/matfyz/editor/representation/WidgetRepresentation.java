/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.editor.representation;

import cz.cuni.matfyz.editor.model.Widget;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 *
 * @author pavel.koupil
 */
public abstract class WidgetRepresentation extends Pane {

	private final String cellId;

	private final Widget model;

	private final List<WidgetRepresentation> parents = new ArrayList<>();	// POTREBUJES VICE PARENTS, PROTOZE TO MAS V REPREZENTACI HRANY!

	private final List<WidgetRepresentation> children = new ArrayList<>();

	private Node view;

	public WidgetRepresentation(Widget model) {
		this.model = model;
		this.cellId = model.getId();
	}

	public Widget getModel() {
		return model;
	}

	public void addCellChild(WidgetRepresentation representation) {
		children.add(representation);
	}

	public List<WidgetRepresentation> getCellChildren() {
		return children;
	}

	public void addCellParent(WidgetRepresentation representation) {
		parents.add(representation);
	}

	public List<WidgetRepresentation> getCellParents() {
		return parents;
	}

	public void removeCellChild(WidgetRepresentation representation) {
		children.remove(representation);
	}

	public void setView(Node view) {
		this.view = view;
		getChildren().add(view);

	}

	public Node getView() {
		return this.view;
	}

	public String getCellId() {
		return cellId;
	}
}
