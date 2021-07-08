/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fxgraph.cells;

import com.fxgraph.graph.Cell;
import javafx.scene.paint.Color;
import javafx.scene.shape.Arc;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;

/**
 *
 * @author pavel.koupil
 */
public class CircleCell extends Cell {

    public CircleCell(String id) {
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

}
