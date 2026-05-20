package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
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

    @Override public Collection<AbstractStatement> createDDLDeleteStatements(List<String> executionCommands) {
        Collection<AbstractStatement> deleteStatements = new ArrayList<>();
        List<String> constraintDrops = extractCreatedConstraints(executionCommands);

        for (String dropStatement : constraintDrops)
            deleteStatements.add(StringStatement.create(dropStatement + ";"));

        return deleteStatements;
    }

    private List<String> extractCreatedConstraints(List<String> executionCommands) {
        List<String> constraints = new ArrayList<>();
        for (String command : executionCommands) {
            Matcher matcher = Pattern.compile("CREATE CONSTRAINT ON \\([^)]*\\) ASSERT [^;]+;?").matcher(command);
            if (matcher.find()) {
                String matched = matcher.group();
                // Strip "CREATE " and keep everything else
                String dropPart = matched.replaceFirst("CREATE", "DROP");
                constraints.add(dropPart.trim().replaceAll(";$", ""));
            }
        }
        return constraints;
    }
    // TODO: needs testing
    // Note that only enterprise version of Neo4j supports multiple dbs. This command will fail on the community version.
    @Override public AbstractStatement createCreationStatement(String newDBName, String owner) {
        final String content = String.format("""
            CREATE DATABASE `%s`;
            """, newDBName);

        return StringStatement.create(content);
    }

}
