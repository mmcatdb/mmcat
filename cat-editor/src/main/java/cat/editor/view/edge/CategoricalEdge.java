/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor.view.edge;

import cat.editor.view.cell.Cell;
import javafx.scene.Group;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeType;

public class CategoricalEdge extends Edge {

    private final Line line;

    public CategoricalEdge(Cell source, Cell target) {
        super(source, target);

        source.addCellChild(target);
        target.addCellParent(source);

        line = new Line();
        line.setStrokeWidth(2);
//        line.setStrokeType(StrokeType.CENTERED);

        line.startXProperty().bind(source.layoutXProperty().add(source.getBoundsInParent().getWidth() / 2.0));
        line.startYProperty().bind(source.layoutYProperty().add(source.getBoundsInParent().getHeight() / 2.0));

        line.endXProperty().bind(target.layoutXProperty().add(target.getBoundsInParent().getWidth() / 2.0));
        line.endYProperty().bind(target.layoutYProperty().add(target.getBoundsInParent().getHeight() / 2.0));

        getChildren().add(line);

    }

}
