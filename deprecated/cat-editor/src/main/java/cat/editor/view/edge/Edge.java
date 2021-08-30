/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor.view.edge;

import cat.editor.view.cell.Cell;
import javafx.scene.Group;

/**
 *
 * @author pavel.koupil
 */
public abstract class Edge extends Group {

    protected Cell source;
    protected Cell target;

    public Edge(Cell source, Cell target) {
        this.source = source;
        this.target = target;
    }

    public Cell getSource() {
        return source;
    }

    public Cell getTarget() {
        return target;
    }
}
