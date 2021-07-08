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
public class EREdge extends Edge {

    private final Line line;

    public EREdge(Cell source, Cell target) {
        super(source, target);

        source.addCellChild(target);
        target.addCellParent(source);

        line = new Line();
        line.setStrokeWidth(2);
//        line.setStrokeType(StrokeType.CENTERED);

//        for (var c : source.getChildren()) {
//            System.out.println(c);
//        }
//        System.out.println("-----");

//        var x = source.layoutXProperty();
//        var y = source.layoutYProperty();
//        var xc = source.getChildren().get(0).layoutXProperty();
//        var xy = source.getChildren().get(0).layoutYProperty();
//        System.out.println(x + ", " + y + "     :::     " + xc + ", " + y);
//        
//        var tx = target.layoutXProperty();
//        var ty = target.layoutYProperty();
        
        line.startXProperty().bind(source.layoutXProperty().add(source.getChildren().get(0).getBoundsInParent().getWidth() / 2.0));
        line.startYProperty().bind(source.layoutYProperty().add(source.getChildren().get(0).getBoundsInParent().getHeight() / 2.0));

        line.endXProperty().bind(target.layoutXProperty().add(target.getChildren().get(0).getBoundsInParent().getWidth() / 2.0));
        line.endYProperty().bind(target.layoutYProperty().add(target.getChildren().get(0).getBoundsInParent().getHeight() / 2.0));

        getChildren().add(line);
    }
}
