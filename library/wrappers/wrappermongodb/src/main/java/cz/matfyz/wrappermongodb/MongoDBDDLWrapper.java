package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;
import cz.matfyz.abstractwrappers.exception.InvalidPathException;
import cz.matfyz.core.datasource.Datasource.DatasourceType;

import org.bson.BsonDocument;
import org.bson.BsonString;

public class MongoDBDDLWrapper implements AbstractDDLWrapper {

    private String kindName = null;

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public boolean isSchemaless() {
        return true;
    }

    @Override public void addProperty(PropertyPath path, boolean isComplex, boolean isRequired) {
        throw InvalidPathException.isSchemaless(DatasourceType.mongodb, path);
    }

    @Override public MongoDBCommandStatement createDDLStatement() {
        return new MongoDBCommandStatement("db.createCollection(" + kindName + ");", new BsonDocument("create", new BsonString(kindName)));
    }

}
