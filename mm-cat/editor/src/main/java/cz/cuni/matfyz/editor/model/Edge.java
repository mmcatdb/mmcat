package cz.cuni.matfyz.editor.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Group;

/**
 *
 * @author pavel.koupil
 */
public abstract class Edge {

	private StringProperty type;

	private StringProperty id;

	private StringProperty name;

	private Widget source;

	private Widget target;

	public Edge(String type, String id, String name, Widget source, Widget target) {
		this.type = new SimpleStringProperty(type);
		this.id = new SimpleStringProperty(id);
		this.name = new SimpleStringProperty(name);
		this.source = source;
		this.target = target;
	}

	public String getId() {
		return id.getValue();
	}

	public Widget getSource() {
		return source;
	}

	public Widget getTarget() {
		return target;
	}

//	public double getX() {
//		return x.doubleValue();
//	}
//
//	public double getY() {
//		return y.doubleValue();
//	}
//
//	public double getWidth() {
//		return width.doubleValue();
//	}
//
//	public double getHeight() {
//		return height.doubleValue();
//	}
//
//	public String getName() {
//		return name.getValue();
//	}
}
