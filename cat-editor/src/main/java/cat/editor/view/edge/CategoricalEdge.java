/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor.view.edge;

import cat.editor.view.cell.Cell;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author pavel.koupil
 */
public class CategoricalEdge extends Edge {

	private final Line line;

	public CategoricalEdge(Cell source, Cell target) {
		super(source, target);

		source.addCellChild(target);
		target.addCellParent(source);

		line = new Line();
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

		double xOrientation = startX.get() > endX.get() ? 1.0 : -1.0;
		double yOrientation = startY.get() > endY.get() ? 1.0 : -1.0;

		Text sourceIdText = new Text(source.getCellId());
		sourceIdText.setFont(Font.font("DejaVu Sans Mono", 12));
		sourceIdText.relocate(line.getBoundsInLocal().getMinX() + 30 * xOrientation, line.getBoundsInLocal().getMinY() + 30 * yOrientation);
		getChildren().add(sourceIdText);

	}

}
