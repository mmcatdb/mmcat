/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor.view.cell;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

/**
 *
 * @author pavel.koupil
 */
public class CircleCell extends Cell {

    public CircleCell(String id) {
        super(id);

        double size = 25;
        Circle view = new Circle(size, size, size);
        view.setUserData("aaa");

        Color color = Color.color(Math.random(), Math.random(), Math.random());

        view.setStroke(color);
        view.setFill(color);
        view.setStrokeWidth(2);

        setView(view);

    }

}
