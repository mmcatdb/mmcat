/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.editor.model.widgets;

import cz.cuni.matfyz.editor.model.Edge;
import cz.cuni.matfyz.editor.model.Widget;

/**
 *
 * @author pavel.koupil
 */
public class CategoricalMorphismWidget extends Edge {

	public CategoricalMorphismWidget(String id, Widget source, Widget target) {
		super("", id, "", source, target);
	}

	public double getStrokeWidth() {
		return 2;
	}

}
