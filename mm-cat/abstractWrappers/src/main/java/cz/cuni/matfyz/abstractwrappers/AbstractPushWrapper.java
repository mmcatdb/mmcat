/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.statements.DMLStatement;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractPushWrapper {

	public abstract void setKindName(String name);

	public abstract void append(String name, Object value);

	public abstract DMLStatement createDMLStatement();

	public abstract void clear();

}
