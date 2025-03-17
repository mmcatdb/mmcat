package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Neo4jDDLWrapper implements AbstractDDLWrapper {

    @Override public boolean isSchemaless() {
        return true;
    }

    @Override public void clear() {
        kindName = null;
    }

    private String kindName = null;

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public void addProperty(PropertyPath path, boolean isComplex, boolean isRequired) {
        throw InvalidPathException.isSchemaless(DatasourceType.neo4j, path);
    }

    @Override public AbstractStatement createDDLStatement() {
        return AbstractStatement.createEmpty();
    }

    @Override
    public Collection<AbstractStatement> createDDLDeleteStatements(List<String> executionCommands) {
        Collection<AbstractStatement> deleteStatements = new ArrayList<>();
        Set<String> tableNames = extractCreatedTables(executionCommands);

        for (String tableName: tableNames)
            deleteStatements.add(createDDLDeleteStatement(tableName));

        return deleteStatements;
    }

    private Set<String> extractCreatedTables(List<String> executionCommands) {
        Set<String> labels = new HashSet<>();
        for (String command : executionCommands) {
            Matcher matcher = Pattern.compile("CREATE CONSTRAINT ON \\((\\w+):([^)]+)\\)").matcher(command);
            if (matcher.find())
                labels.add(matcher.group(2));
        }
        return labels;
    }

    private StringStatement createDDLDeleteStatement(String tableName) {
        return StringStatement.create("a");
    }

}
