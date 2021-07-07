/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fxgraph.cells;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import com.fxgraph.graph.Cell;

public class RectangleCell extends Cell {

    public RectangleCell( String id) {
        super( id);

        
        double size = (Math.random() * 50.0)+10.0;
        Rectangle view = new Rectangle(size,size);

//        this.relocate(Math.random()*200, 100);
        view.setStroke(Color.CHOCOLATE);
        view.setFill(Color.DODGERBLUE);

        setView( view);

    }

}
