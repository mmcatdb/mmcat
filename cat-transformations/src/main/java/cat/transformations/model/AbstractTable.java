/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.transformations.model;

import java.util.List;

/**
 *
 * @author pavel.contos
 */
public abstract class AbstractTable {

	public abstract Schema getSchema();

	public abstract Object[] getRecord(int id);

	public abstract int size();

	public abstract void addRecord(Object[] record);

	//	public abstract Object getColumn(String columnName);
	//	public abstract void alterTable();	// Data migration?
}
