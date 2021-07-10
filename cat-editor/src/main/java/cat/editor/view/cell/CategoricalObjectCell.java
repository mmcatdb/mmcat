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
public class CategoricalObjectCell extends Cell {

    private static final double SIZE = 10;

    public CategoricalObjectCell(String id) {
        super(id);

        double size = 25;
//        double size = (Math.random() * 50.0) + 10.0;
//        Arc view = new Arc(size, size, size, size, size, size);
        Circle view = new Circle(size, size, size);
        view.setUserData("aaa");

//        this.relocate(Math.random()*200, 100);
        Color color = Color.color(Math.random(), Math.random(), Math.random());

        
        
        view.setStroke(color);
//        view.setStroke(Color.DODGERBLUE);
        view.setFill(color);
        view.setStrokeWidth(2);
//        view.setFill(Color.DODGERBLUE);

        setView(view);

    }

    public CategoricalObjectCell(String id, String name, double x, double y) {
        super(id);

        Text text = new Text(name);
        text.setFont(Font.font("DejaVu Sans Mono", 20));
        double height = text.getBoundsInLocal().getHeight();
//        System.out.println(height + " ::: height");
        text.relocate(25, -(height / 2 - SIZE));

        Circle shape = new Circle(SIZE, SIZE, SIZE);
        shape.setUserData("aaa");
        shape.setStroke(Color.BLACK);
        shape.setFill(Color.WHITE);
        shape.setStrokeWidth(2);

        setView(shape);
        setView(text);
        relocate(x, y);
    }

}
