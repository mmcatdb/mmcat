/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.editor.view.edge;

import cz.cuni.matfyz.editor.view.cell.Cell;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author pavel.koupil
 */
public class EREdge extends Edge {

	private final Line line;

	private static final double DIFF = 20;
	private static final double EPSILON = 5;

	public EREdge(String cardinality, Cell source, Cell target) {
		super(source, target);

		source.addCellChild(target);
		target.addCellParent(source);

		line = new Line();
		line.setStrokeWidth(2);

		var sourceX = source.layoutXProperty().add(source.getChildren().get(0).getBoundsInParent().getWidth() / 2.0);
		var sourceY = source.layoutYProperty().add(source.getChildren().get(0).getBoundsInParent().getHeight() / 2.0);

		var targetX = target.layoutXProperty().add(target.getChildren().get(0).getBoundsInParent().getWidth() / 2.0);
		var targetY = target.layoutYProperty().add(target.getChildren().get(0).getBoundsInParent().getHeight() / 2.0);

		line.startXProperty().bind(sourceX);
		line.startYProperty().bind(sourceY);

		line.endXProperty().bind(targetX);
		line.endYProperty().bind(targetY);

		getChildren().add(line);

		Text edgeId = new Text(cardinality);
		edgeId.setFont(Font.font("DejaVu Sans Mono", 12));
		double textWidth = edgeId.getBoundsInLocal().getWidth();
		double textHeight = edgeId.getBoundsInLocal().getHeight();

		double xOrientation = sourceX.get() <= targetX.get() ? 1.0 : -1.0;
		double yOrientation = sourceY.get() <= targetY.get() ? 1.0 : -1.0;

		double px = xOrientation > 0 ? DIFF : DIFF + textWidth;
		double py = yOrientation > 0 ? DIFF : DIFF + textHeight;

		double x, y;

		double diffX = (targetX.get() - sourceX.get());// * xOrientation;
		double diffY = (targetY.get() - sourceY.get());// * yOrientation;

		x = sourceX.get() + (diffX / 2.00);
		y = sourceY.get() + (diffY / 2.00);

		diffX *= xOrientation;
		diffY *= yOrientation;

		if (diffX < EPSILON) {
			x += 10;
			y -= (textHeight / 2);
//			xOrientation = 1;
//			px = 2;
//			x = sourceX.get() + px * xOrientation;
//			y = sourceY.get() + py * yOrientation;
		} else if (diffY < EPSILON) {
			x -= (textWidth / 2);
			y += 10;
//			yOrientation = 1;
//			py = 2;
//			x = sourceX.get() + px * xOrientation;
//			y = sourceY.get() + py * yOrientation;
		} else {
//			px += DIFF;
//			x = sourceX.get() + px * xOrientation;
//			y = sourceY.get() + py * yOrientation;
		}

		StringBuilder builder = new StringBuilder();
		builder.append("X:[");
		builder.append(sourceX.get());
		builder.append(",");
		builder.append(sourceY.get());
		builder.append("] Y:[");
		builder.append(targetX.get());
		builder.append(",");
		builder.append(targetY.get());
		builder.append("] MID:[");
		builder.append(x);
		builder.append(",");
		builder.append(y);
		builder.append("] DIFF:[");
		builder.append(diffX);
		builder.append(",");
		builder.append(diffY);
		builder.append("]");
		System.out.println(builder);

		edgeId.relocate(x, y);
		getChildren().add(edgeId);
	}
}
