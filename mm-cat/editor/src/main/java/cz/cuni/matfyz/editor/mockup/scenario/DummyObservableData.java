/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.editor.mockup.scenario;

import cz.cuni.matfyz.editor.dummy.entity.Contact;
import cz.cuni.matfyz.editor.dummy.entity.Kind;
import cat.tutorial.Person;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ChoiceBox;
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

	private static final ObservableList<String> zoomItems = FXCollections.observableArrayList("50%", "75%", "100%", "150%", "200%", "300%", "400%");

	private static final ObservableList<Contact> contacts
			= FXCollections.observableArrayList(
					new Contact("1", "1", "email"),
					new Contact("1", "1", "cellphone"),
					new Contact("2", "1", "email"),
					new Contact("2", "1", "phone"),
					new Contact("3", "1", "phone"),
					new Contact("3", "1", "cellphone"),
					new Contact("4", "1", "email"),
					new Contact("5", "1", "cellphone"),
					new Contact("6", "1", "email"),
					new Contact("6", "1", "phone"),
					new Contact("6", "1", "cellphone"),
					new Contact("7", "1", "email"),
					new Contact("7", "1", "phone"),
					new Contact("8", "1", "phone"),
					new Contact("9", "1", "email"),
					new Contact("9", "1", "cellphone"),
					new Contact("10", "1", "email")
			);

	public void setItemsPostgreSQLKinds(TableView table) {
		table.setItems(postgreSQLBKinds);
	}

	public void setZoomItems(ChoiceBox<String> zoom) {
		zoom.setItems(zoomItems);
	}

	public void setItemsContactInstance(TableView table) {
		table.setItems(contacts);
	}

}
