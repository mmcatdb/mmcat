/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor.view.edge;

import cat.editor.view.cell.Cell;
import javafx.scene.shape.Line;

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

		line.startXProperty().bind(source.layoutXProperty().add(source.getChildren().get(0).getBoundsInParent().getWidth() / 2.0));
		line.startYProperty().bind(source.layoutYProperty().add(source.getChildren().get(0).getBoundsInParent().getHeight() / 2.0));

		line.endXProperty().bind(target.layoutXProperty().add(target.getChildren().get(0).getBoundsInParent().getWidth() / 2.0));
		line.endYProperty().bind(target.layoutYProperty().add(target.getChildren().get(0).getBoundsInParent().getHeight() / 2.0));

		getChildren().add(line);

	}

}
