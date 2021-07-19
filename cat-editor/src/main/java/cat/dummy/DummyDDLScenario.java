/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.dummy;

import javafx.scene.control.TextArea;

/**
 *
 * @author pavel.koupil
 */
public enum DummyDDLScenario {
	INSTANCE;

	public void createMongoKinds(TextArea textArea) {
		textArea.setText("""
                         db.createCollection(\"orders\")
                         """);
	}

}
