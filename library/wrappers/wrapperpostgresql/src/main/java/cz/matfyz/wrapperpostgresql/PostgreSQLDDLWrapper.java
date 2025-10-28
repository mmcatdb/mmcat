package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

        if (segment.isArray() && segment.arrayDimension() != 1)
            throw InvalidPathException.unsupportedArrayDimension(DatasourceType.postgresql, path);

        final String type = segment.isArray() ? "TEXT[]" : "TEXT";

        segment.names.forEach(name -> {
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

        return StringStatement.create(content);
    }

    private record Property(
        String name,
        String command
    ) {}

    @Override
    public Collection<AbstractStatement> createDDLDeleteStatements(List<String> executionCommands) {
        Collection<AbstractStatement> deleteStatements = new ArrayList<>();
        List<String> tableNames = extractCreatedTables(executionCommands);

        // To avoid errors with references among tables.
        Collections.reverse(tableNames);

        for (String tableName: tableNames)
            deleteStatements.add(createDDLDeleteStatement(tableName));

        return deleteStatements;
    }

    private List<String> extractCreatedTables(List<String> executionCommands) {
        List<String> tableNames = new ArrayList<>();
        for (String command : executionCommands) {
            Matcher matcher = Pattern.compile("CREATE TABLE\\s+\"([^\"]+)\"").matcher(command);
            if (matcher.find())
                tableNames.add(matcher.group(1));
        }
        return tableNames;
    }

    private StringStatement createDDLDeleteStatement(String tableName) {
        final String content = String.format("""
            DROP TABLE \"%s\" ;
            """, tableName);
        return StringStatement.create(content);
    }

    @Override
    public AbstractStatement createCreationStatement(String newDBName, String owner) {
        final String content = String.format("""
                CREATE DATABASE \"%s\" OWNER \"%s\";
                """, newDBName, owner);
        return StringStatement.create(content);
    }

}
