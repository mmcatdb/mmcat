package cz.matfyz.abstractwrappers;

public interface AbstractDMLWrapper {

    void setKindName(String name);

    void append(String name, Object value);

    AbstractStatement createDMLStatement();

    void clear();

}
