/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.editor.dummy.entity;

import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author pavel.koupil
 */
public class Kind {

	private SimpleStringProperty name;
	private SimpleStringProperty root;
	private SimpleStringProperty modified;
	private SimpleStringProperty materialized;

	public Kind() {
	}

	public Kind(String name, String root, String modified, String materialized) {
		this.name = new SimpleStringProperty(name);
		this.root = new SimpleStringProperty(root);
		this.modified = new SimpleStringProperty(modified);
		this.materialized = new SimpleStringProperty(materialized);
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

	public String getModified() {
		return modified.get();
	}

	public void setModified(String modified) {
		this.modified.set(modified);
	}

	public String getMaterialized() {
		return materialized.get();
	}

	public void setMaterialized(String materialized) {
		this.materialized.set(materialized);
	}

	@Override
	public String toString() {
		return "Kind{" + "name=" + name + ", root=" + root + ", modified=" + modified + ", materialized=" + materialized + '}';
	}

}
