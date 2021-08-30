/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.editor;

import cat.editor.view.cell.Cell;
import javafx.scene.layout.Pane;

/**
 *
 * @author pavel.koupil
 */
public class Palette extends Pane {

	public Palette() {

	}

	public void addElement(Cell element) {
		getChildren().add(element);
	}

}
