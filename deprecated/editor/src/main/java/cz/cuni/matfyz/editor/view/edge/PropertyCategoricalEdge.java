package cz.cuni.matfyz.editor.view.edge;

import cz.cuni.matfyz.editor.view.cell.Cell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author pavel.koupil
 */
public class PropertyCategoricalEdge extends Edge {

	private final Line line;

	private static final double DIFF = 20;
	private static final double EPSILON = 5;

	public PropertyCategoricalEdge(String id, Cell source, Cell target) {
		super(source, target);

		source.addCellChild(target);
		target.addCellParent(source);

		line = new Line();
		line.setStroke(EdgeColors.PROPERTY_STROKE_COLOR);
		line.setFill(EdgeColors.PROPERTY_STROKE_COLOR);
		line.setStrokeWidth(2);

		var startX = source.layoutXProperty().add(source.getChildren().get(0).getBoundsInParent().getWidth() / 2.0);
		var startY = source.layoutYProperty().add(source.getChildren().get(0).getBoundsInParent().getHeight() / 2.0);

		var endX = target.layoutXProperty().add(target.getChildren().get(0).getBoundsInParent().getWidth() / 2.0);
		var endY = target.layoutYProperty().add(target.getChildren().get(0).getBoundsInParent().getHeight() / 2.0);

		line.startXProperty().bind(startX);
		line.startYProperty().bind(startY);

		line.endXProperty().bind(endX);
		line.endYProperty().bind(endY);

		getChildren().add(line);

		Text edgeId = new Text(id);
		edgeId.setFont(Font.font("DejaVu Sans Mono", 12));
		double textWidth = edgeId.getBoundsInLocal().getWidth();
		double textHeight = edgeId.getBoundsInLocal().getHeight();

		double xOrientation = startX.get() <= endX.get() ? 1.0 : -1.0;
		double yOrientation = startY.get() <= endY.get() ? 1.0 : -1.0;

		double px = xOrientation > 0 ? DIFF : DIFF + textWidth;
		double py = yOrientation > 0 ? DIFF : DIFF + textHeight;

		double x, y;

		double diffX = (endX.get() - startX.get()) * xOrientation;
		double diffY = (endY.get() - startY.get()) * yOrientation;

		if (diffX < EPSILON) {
			xOrientation = 1;
			px = 2;
			x = startX.get() + px * xOrientation;
			y = startY.get() + py * yOrientation;
		} else if (diffY < EPSILON) {
			yOrientation = 1;
			py = 2;
			x = startX.get() + px * xOrientation;
			y = startY.get() + py * yOrientation;
		} else {
			px += DIFF;
			x = startX.get() + px * xOrientation;
			y = startY.get() + py * yOrientation;
		}

		edgeId.relocate(x, y);
		getChildren().add(edgeId);

	}

}
