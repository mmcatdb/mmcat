/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor.view.cell;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author pavel.koupil
 */
public class ERRelationshipCell extends Cell {

    public ERRelationshipCell(String id, String name, double x, double y) {
        super(id);

        Text text = new Text(name);
        text.setFont(Font.font("DejaVu Sans Mono", 20));
        text.relocate(15, 15);

        double width = text.getBoundsInLocal().getWidth() + 30;
        double height = text.getBoundsInLocal().getHeight() + 30;

        Polygon shape = new Polygon(
                width / 2, 0,
                width, height / 2,
                width / 2, height,
                0, height / 2);
        shape.setUserData("aaa");
        shape.setStroke(Color.BLACK);
        shape.setFill(Color.WHITE);
        shape.setStrokeWidth(2);
        setView(shape);

        setView(text);
        relocate(x, y);
    }
}
