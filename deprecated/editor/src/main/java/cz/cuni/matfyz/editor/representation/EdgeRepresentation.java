package cz.cuni.matfyz.editor.representation;

import cz.cuni.matfyz.editor.model.Edge;
import javafx.scene.Group;

/**
 *
 * @author pavel.koupil
 */
public abstract class EdgeRepresentation extends Group {

	private final Edge model;

	private WidgetRepresentation source;

	private WidgetRepresentation target;

	public EdgeRepresentation(Edge model, WidgetRepresentation source, WidgetRepresentation target) {
		this.model = model;
		this.source = source;
		this.target = target;
	}

	public WidgetRepresentation getSource() {
		return source;
	}

	public WidgetRepresentation getTarget() {
		return target;
	}

}
