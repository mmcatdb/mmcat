/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.algorithms2.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pavel.koupil
 */
public class DocumentKind implements AbstractKind {

	private final String name;
	List<AbstractRecord> records = new ArrayList<>();

	public DocumentKind(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Iterable<String> getPropertyNames() {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public Iterable<AbstractRecord> getRecords() {
		return records;
	}

	@Override
	public AbstractRecord getRecord(int index) {
		return records.get(index);
	}

	@Override
	public AbstractRecord getRecord(AbstractIdentifier identifier) {
		for (AbstractRecord record : records) {
			for (AbstractIdentifier superid : record.getIdentifiers()) {
				if (superid.equals(identifier)) {
					return record;
				}
			}
		}
		return null;
	}

	@Override
	public int size() {
		return records.size();
	}

	@Override
	public void add(AbstractRecord record) {
		records.add(record);
	}

}
