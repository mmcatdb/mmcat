package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.exception.UnsupportedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLDDLWrapper implements AbstractDDLWrapper {

    private String kindName = null;
    private final List<Property> properties = new ArrayList<>();
    
    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public boolean isSchemaLess() {
        return false;
    }

    @Override public boolean addSimpleProperty(Set<String> names, boolean required) {
        names.forEach(name -> {
            String command = "\"" + name + "\" TEXT" + (required ? " NOT NULL" : "");
            properties.add(new Property(name, command));
        });
        
        return true;
    }

    @Override public boolean addSimpleArrayProperty(Set<String> names, boolean required) {
        names.forEach(name -> {
            String command = "\"" + name + "\" TEXT[]" + (required ? " NOT NULL" : "");
            properties.add(new Property(name, command));
        });
        
        return true;
    }

    @Override public boolean addComplexProperty(Set<String> names, boolean required) {
        throw UnsupportedException.addComplexProperty(PostgreSQLControlWrapper.TYPE);
        // It is supported in a newer version (see https://www.postgresql.org/docs/10/rowtypes.html) so it could be implemented later.
        // TODO dynamic named properties?
    }

    @Override public boolean addComplexArrayProperty(Set<String> names, boolean required) {
        throw UnsupportedException.addComplexArrayProperty(PostgreSQLControlWrapper.TYPE);
        // It is supported in a newer version (see https://www.postgresql.org/docs/10/rowtypes.html) so it could be implemented later.
    }

    @Override public PostgreSQLStatement createDDLStatement() {
        String commands = String.join(",\n", properties.stream().map(property -> AbstractDDLWrapper.INDENTATION + property.command).toList());
        String content = String.format("""
            CREATE TABLE \"%s\" (
            %s
            );
            """, kindName, commands);
        
        return new PostgreSQLStatement(content);
    }

    private record Property(
        String name,
        String command
    ) {}
}
