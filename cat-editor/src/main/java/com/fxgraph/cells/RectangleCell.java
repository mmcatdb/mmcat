/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fxgraph.cells;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import cat.editor.view.cell.Cell;
import javafx.scene.text.Text;

public class RectangleCell extends Cell {

    public RectangleCell(String id) {
        super(id);

        double size = 50;
//        double size = (Math.random() * 50.0) + 10.0;
        Rectangle view = new Rectangle(size, size);
        view.setUserData("aaa");

//        this.relocate(Math.random()*200, 100);
        Color color = Color.color(Math.random(), Math.random(), Math.random());

        view.setStroke(color);
//        view.setStroke(Color.DODGERBLUE);
        view.setFill(color);
//        view.setFill(Color.DODGERBLUE);
        view.setStrokeWidth(2);
        Text text = new Text(id);
        setView(view);
        setView(text);

    }

}
