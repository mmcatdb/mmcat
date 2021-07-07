/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fxgraph.cells;

import javafx.scene.image.ImageView;

import com.fxgraph.graph.Cell;

public class ImageCell extends Cell {

    public ImageCell(String id) {
        super(id);

        ImageView view = new ImageView("http://upload.wikimedia.org/wikipedia/commons/thumb/4/41/Siberischer_tiger_de_edit02.jpg/800px-Siberischer_tiger_de_edit02.jpg");
        view.setFitWidth(100);
        view.setFitHeight(80);

        setView(view);

    }

}
