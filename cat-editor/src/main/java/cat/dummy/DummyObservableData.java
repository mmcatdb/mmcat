/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.dummy;

import cat.dummy.entity.Contact;
import cat.tutorial.Person;
import javafx.collections.ObservableList;

/**
 *
 * @author pavel.koupil
 */
public enum DummyObservableData {
	INSTANCE;

	public ObservableList<Contact> observableContacts;

}
