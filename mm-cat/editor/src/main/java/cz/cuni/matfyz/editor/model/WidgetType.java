/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.editor.model;

import javafx.scene.paint.Color;

/**
 *
 * @author pavel.koupil
 */
public enum WidgetType {
	//	public static final Color AVAILABLE_STROKE_COLOR = Color.web("82B366");
//	public static final Color SELECTED_STROKE_COLOR = Color.web("FFFF00");
//	public static final Color KIND_STROKE_COLOR = Color.web("B85450");
//	public static final Color PROPERTY_STROKE_COLOR = Color.web("D79B00");
//	public static final Color NAME_STROKE_COLOR = Color.web("6C8EBF");
//
//	public static final Color KIND_FILL_COLOR = Color.web("F8CECC");
//	public static final Color PROPERTY_FILL_COLOR = Color.web("FFE6CC");
//	public static final Color NAME_FILL_COLOR = Color.web("DAE8FC");
//	public static final Color OBJECT_FILL_COLOR = Color.web("FFFFFF");
	CATEGORICAL_OBJECT(null, null),
	MAPPING_KIND(null, null),
	MAPPING_PROPERTY(null, null),
	MAPPING_SELECTED_KIND(null, null),
	MAPPING_SELECTED_PROPERTY(null, null),
	MAPPING_NAME(null, null),
	MAPPING_AVAILABLE(null, null),
	MAPPING_AVAILABLE_PROPERTY(null, null),
	MAPPING_AVAILABLE_KIND(null, null),
	MAPPING_AVAILABLE_NAME(null, null),
	MAPPING_SELECTED_NAME(null, null),
	ER_ENTITY(null, null),
	ER_RELATIONSHIP(null, null),
	ER_ATTRIBUTE(null, null),
	ER_IDENTIFIER(null, null),
	ER_WEAK_IDENTIFIER(null, null),
	SELECTED(null, null),
	POSTGRESQL_KIND(Color.web("9673A6"), Color.web("E1D5E7")),
	MONGODB_KIND(Color.web("82B366"), Color.web("D5E8D4"));

	private final Color stroke;
	private final Color fill;

	private WidgetType(Color stroke, Color fill) {
		this.stroke = stroke;
		this.fill = fill;
	}

	public Color getStroke() {
		return stroke;
	}

	public Color getFill() {
		return fill;
	}

}
