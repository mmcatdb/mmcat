/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.dummy.entity;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author pavel.koupil
 */
public class MigratedKind {

	private SimpleStringProperty name;
	private SimpleStringProperty root;
	private SimpleBooleanProperty checked;

	public MigratedKind() {
	}

	public MigratedKind(Boolean checked, String name, String root) {
		this.name = new SimpleStringProperty(name);
		this.root = new SimpleStringProperty(root);
		this.checked = new SimpleBooleanProperty(checked);
	}

	public String getName() {
		return name.get();
	}

	public void setName(String name) {
		this.name.set(name);
	}

	public String getRoot() {
		return root.get();
	}

	public void setRoot(String root) {
		this.root.set(root);
	}

	public Boolean getChecked() {
		return checked.get();
	}
	
	public SimpleBooleanProperty getCheckedProperty() {
		return checked;
	}

	public void setChecked(Boolean checked) {
		this.checked.set(checked);
	}

	@Override
	public String toString() {
		return "MigratedKind{" + "name=" + name + ", root=" + root + ", checked=" + checked + '}';
	}

}
