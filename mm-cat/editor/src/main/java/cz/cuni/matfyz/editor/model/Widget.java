/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.editor.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.beans.property.DoubleProperty;
//import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author pavel.koupil
 */
public abstract class Widget {

	private Widget parent = null;

	private List<Widget> adjacent = new ArrayList<>();

	private List<Widget> adjacentBase = new ArrayList<>();

	private StringProperty type;

	private StringProperty id;

	private StringProperty name;

	private DoubleProperty x;

	private DoubleProperty y;

	private DoubleProperty width;

	private DoubleProperty height;

	public Widget(String type) {
		this(type, "", "", 100, 100, 100, 100);
	}

	public Widget(String type, String id, String name, double x, double y, double width, double height) {
		this.type = new SimpleStringProperty(type);
		this.id = new SimpleStringProperty(id);
		this.name = new SimpleStringProperty(name);
		this.x = new SimpleDoubleProperty(x);
		this.y = new SimpleDoubleProperty(y);
		this.width = new SimpleDoubleProperty(width);
		this.height = new SimpleDoubleProperty(height);
	}

	public List<Widget> getAdjacent() {
		return adjacent;
	}

	public List<Widget> getAdjacentBase() {
		return adjacentBase;
	}

	public String getId() {
		return id.getValue();
	}

	public double getX() {
		return x.doubleValue();
	}

	public double getY() {
		return y.doubleValue();
	}

	public double getWidth() {
		return width.doubleValue();
	}

	public double getHeight() {
		return height.doubleValue();
	}

	public String getName() {
		return name.getValue();
	}

//	public List<Widget> getParents() {
//		System.out.println("TOHLE PATRI DO REPRESENTATION! NEBO DO MODELU? DO REPRESENTATION!!! PROTOZE TAM JE DULEZITA HIERARCHIE!");
//		var result = Collections.EMPTY_LIST;
//		result.add(parent);
//		return result;
////		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//	}
//
//	public void addChild(Widget widget) {
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//	}
//
//	public void removeChild(Widget widget) {
//		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
//	}
}
