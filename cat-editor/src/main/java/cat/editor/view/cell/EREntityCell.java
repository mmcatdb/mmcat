/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor.view.cell;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

/**
 *
 * @author pavel.koupil
 */
public class EREntityCell extends Cell {

    public EREntityCell(String id, String name, double x, double y) {
        super(id);

        double size = 25;
        Text text = new Text(name);
        text.setFont(Font.font("DejaVu Sans Mono", 20));
        text.relocate(10, 10);
        Rectangle shape = new Rectangle(text.getBoundsInLocal().getWidth() + 20, text.getBoundsInLocal().getHeight() + 20);
        shape.setUserData("aaa");
        Color color = Color.color(Math.random(), Math.random(), Math.random());
        shape.setStroke(Color.BLACK);
        shape.setFill(Color.WHITE);
        shape.setStrokeWidth(2);
        setView(shape);

        setView(text);
        relocate(x, y);
    }
}
