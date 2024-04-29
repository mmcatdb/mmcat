package cz.matfyz.abstractwrappers;

import cz.matfyz.abstractwrappers.exception.UnsupportedException;
import cz.matfyz.core.mapping.StaticName;

import java.util.Set;

public interface AbstractDDLWrapper {

    String PATH_SEPARATOR = "/";
    String EMPTY_NAME = StaticName.createAnonymous().getStringName();
    String INDENTATION = "    ";

    void setKindName(String name);

    boolean isSchemaLess();

    boolean addSimpleProperty(Set<String> names, boolean required) throws UnsupportedException;

    boolean addSimpleArrayProperty(Set<String> names, boolean required) throws UnsupportedException;

    boolean addComplexProperty(Set<String> names, boolean required) throws UnsupportedException;

    boolean addComplexArrayProperty(Set<String> names, boolean required) throws UnsupportedException;

    AbstractStatement createDDLStatement();

}
