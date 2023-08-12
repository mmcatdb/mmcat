package cz.matfyz.abstractwrappers;

/**
 * @author pavel.koupil
 */
public interface AbstractDMLWrapper {

    void setKindName(String name);

    void append(String name, Object value);

    AbstractStatement createDMLStatement();

    void clear();

}
