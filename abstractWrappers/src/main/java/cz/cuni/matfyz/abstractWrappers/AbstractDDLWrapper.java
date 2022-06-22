package cz.cuni.matfyz.abstractWrappers;

import cz.cuni.matfyz.statements.DDLStatement;
import java.util.Set;

/**
 *
 * @author pavel.koupil
 */
public interface AbstractDDLWrapper {

	public final static String INTENDATION = "    ";

	public abstract void setKindName(String name);

	public abstract boolean isSchemaLess();

	public abstract boolean addSimpleProperty(Set<String> names, boolean required) throws UnsupportedOperationException;

	public abstract boolean addSimpleArrayProperty(Set<String> names, boolean required) throws UnsupportedOperationException;

	public abstract boolean addComplexProperty(Set<String> names, boolean required) throws UnsupportedOperationException;

	public abstract boolean addComplexArrayProperty(Set<String> names, boolean required) throws UnsupportedOperationException;

	public abstract DDLStatement createDDLStatement();

}
