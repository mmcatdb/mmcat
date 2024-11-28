package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.util.ArrayList;
import java.util.List;

public class PostgreSQLDDLWrapper implements AbstractDDLWrapper {

    @Override public boolean isSchemaless() {
        return false;
    }

    @Override public void clear() {
        kindName = null;
        properties.clear();
    }

    private String kindName = null;
    private final List<Property> properties = new ArrayList<>();

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public void addProperty(PropertyPath path, boolean isComplex, boolean isRequired) {
        if (path.segments().size() != 1)
            throw InvalidPathException.wrongLength(DatasourceType.postgresql, path);

        final PathSegment segment = path.segments().get(0);
        // The postgres structure is flat.
        if (isComplex)
            throw InvalidPathException.isComplex(DatasourceType.postgresql, path);

        final String type = segment.isArray() ? "TEXT[]" : "TEXT";
        segment.names().forEach(name -> {
            final String command = "\"" + name + "\" " + type + (isRequired ? " NOT NULL" : "");
            properties.add(new Property(name, command));
        });
    }

    @Override public StringStatement createDDLStatement() {
        final String commands = String.join(",\n", properties.stream().map(property -> AbstractDDLWrapper.INDENTATION + property.command).toList());
        final String content = String.format("""
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
