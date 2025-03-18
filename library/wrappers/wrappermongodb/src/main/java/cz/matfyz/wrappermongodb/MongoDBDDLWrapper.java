package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.abstractwrappers.AbstractStatement.StringStatement;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bson.BsonDocument;
import org.bson.BsonString;

public class MongoDBDDLWrapper implements AbstractDDLWrapper {

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
        throw InvalidPathException.isSchemaless(DatasourceType.mongodb, path);
    }

    @Override public MongoDBCommandStatement createDDLStatement() {
        return new MongoDBCommandStatement("db.createCollection(" + kindName + ");", new BsonDocument("create", new BsonString(kindName)));
    }

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
        List<String> collectionNames = new ArrayList<>();
        for (String command : executionCommands) {
            Matcher matcher = Pattern.compile("db\\.createCollection\\(\"([^\"]+)\"").matcher(command);
            if (matcher.find())
                collectionNames.add(matcher.group(1));
        }
        return collectionNames;
    }

    private MongoDBCommandStatement createDDLDeleteStatement(String tableName) {
        return new MongoDBCommandStatement("db." + tableName + ".drop();", new BsonDocument("delete", new BsonString(tableName)));
    }

}
