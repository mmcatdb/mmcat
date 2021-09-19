/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.core;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class RecordProperty {

	protected RecordProperty parent;
	protected List<RecordProperty> children = new ArrayList<>();
	protected List<RecordData> data = new ArrayList<>();

	public RecordProperty(RecordProperty parent, List<RecordProperty> children, List<RecordData> data) {
		this.parent = parent;
		this.children = children;
		this.data = data;
	}

	public RecordProperty(RecordProperty parent) {
		this.parent = parent;
	}

	public RecordProperty parent() {
		return parent;
	}

	public List<RecordProperty> children() {
		return children;
	}

}
