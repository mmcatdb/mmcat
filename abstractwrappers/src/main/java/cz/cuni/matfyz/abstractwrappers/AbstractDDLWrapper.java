package cz.cuni.matfyz.abstractwrappers;

import cz.cuni.matfyz.abstractwrappers.exception.UnsupportedException;
import cz.cuni.matfyz.core.mapping.StaticName;

import java.util.Set;

/**
 * @author pavel.koupil
 */
public interface AbstractDDLWrapper {

    public static final String PATH_SEPARATOR = "/";
    public static final String EMPTY_NAME = StaticName.createAnonymous().getStringName();

    public static final String INDENTATION = "    ";

    public abstract void setKindName(String name);

    public abstract boolean isSchemaLess();

    public abstract boolean addSimpleProperty(Set<String> names, boolean required) throws UnsupportedException;

    public abstract boolean addSimpleArrayProperty(Set<String> names, boolean required) throws UnsupportedException;

    public abstract boolean addComplexProperty(Set<String> names, boolean required) throws UnsupportedException;

    public abstract boolean addComplexArrayProperty(Set<String> names, boolean required) throws UnsupportedException;

    public abstract AbstractStatement createDDLStatement();

}
