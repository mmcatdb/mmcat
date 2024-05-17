package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.abstractwrappers.datasource.Datasource.DatasourceType;
import cz.matfyz.abstractwrappers.exception.UnsupportedException;

import java.util.ArrayList;
import java.util.List;

public class PostgreSQLDDLWrapper implements AbstractDDLWrapper {

    private String kindName = null;
    private final List<Property> properties = new ArrayList<>();

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public boolean isSchemaless() {
        return false;
    }

    @Override public boolean addSimpleProperty(String path, boolean required) {
        // The postgres structure is flat, therefore, the path should be equal to the simple name of the property.
        String command = "\"" + path + "\" TEXT" + (required ? " NOT NULL" : "");
        properties.add(new Property(path, command));

        return true;
    }

    @Override public boolean addSimpleArrayProperty(String path, boolean required) {
        String command = "\"" + path + "\" TEXT[]" + (required ? " NOT NULL" : "");
        properties.add(new Property(path, command));

        return true;
    }

    @Override public boolean addComplexProperty(String path, boolean required) {
        throw UnsupportedException.addComplexProperty(DatasourceType.postgresql);
        // It is supported in a newer version (see https://www.postgresql.org/docs/10/rowtypes.html) so it could be implemented later.
        // TODO dynamic named properties?
    }

    @Override public boolean addComplexArrayProperty(String path, boolean required) {
        throw UnsupportedException.addComplexArrayProperty(DatasourceType.postgresql);
        // It is supported in a newer version (see https://www.postgresql.org/docs/10/rowtypes.html) so it could be implemented later.
    }

    @Override public StringStatement createDDLStatement() {
        String commands = String.join(",\n", properties.stream().map(property -> AbstractDDLWrapper.INDENTATION + property.command).toList());
        String content = String.format("""
            CREATE TABLE \"%s\" (
            %s
            );
            """, kindName, commands);

        return new StringStatement(content);
    }

    private record Property(
        String name,
        String command
    ) {}
}
