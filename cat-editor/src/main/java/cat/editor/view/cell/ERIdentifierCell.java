/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor.view.cell;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author pavel.koupil
 */
public class ERIdentifierCell extends Cell {

    private static final double SIZE = 10;

    public ERIdentifierCell(String id, String name, double x, double y) {
        super(id);

        Text text = new Text(name);
        text.setFont(Font.font("DejaVu Sans Mono", 20));
        double height = text.getBoundsInLocal().getHeight();
        System.out.println(height + " ::: height");
        text.relocate(25, -(height / 2 - SIZE));

        Circle shape = new Circle(SIZE, SIZE, SIZE);
        shape.setUserData("aaa");
        shape.setStroke(Color.BLACK);
        shape.setFill(Color.BLACK);
        shape.setStrokeWidth(2);

        setView(shape);
        setView(text);
        relocate(x, y);
    }
}
