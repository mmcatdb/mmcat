/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fxgraph.cells;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

import com.fxgraph.graph.Cell;

public class TriangleCell extends Cell {

    public TriangleCell(String id) {
        super(id);

        double width = 50;
        double height = 50;

        Polygon view = new Polygon(width / 2, 0, width, height, 0, height);

        Color color = Color.color(Math.random(), Math.random(), Math.random());

        view.setStroke(color);
//        view.setStroke(Color.RED);
        view.setFill(color);
        view.setStrokeWidth(2);
//        view.setFill(Color.RED);

        setView(view);

    }

}
