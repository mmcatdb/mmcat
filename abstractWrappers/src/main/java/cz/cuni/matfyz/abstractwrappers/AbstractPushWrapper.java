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
