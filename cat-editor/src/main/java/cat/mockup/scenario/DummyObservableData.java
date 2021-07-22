/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.mockup.scenario;

import cat.dummy.entity.Contact;
import cat.dummy.entity.Kind;
import cat.tutorial.Person;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

/**
 *
 * @author pavel.koupil
 */
public enum DummyObservableData {
	INSTANCE;

	private static final ObservableList<Kind> postgreSQLBKinds
			= FXCollections.observableArrayList(
					new Kind("Contact", "113", "No", "No"),
					new Kind("Customer", "100", "No", "No"),
					new Kind("Orders", "111", "No", "No"),
					new Kind("Type", "114", "No", "No")
			);

	public void setItemsPostgreSQLKinds(TableView table) {
		table.setItems(postgreSQLBKinds);
	}

	private static ObservableList<Contact> observableContacts;

}
