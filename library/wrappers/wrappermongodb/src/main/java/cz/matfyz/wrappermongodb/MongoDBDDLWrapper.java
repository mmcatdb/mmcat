package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;

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

    @Override public boolean addSimpleProperty(String path, boolean required) {
        return false;
    }

    @Override public boolean addSimpleArrayProperty(String path, boolean required) {
        return false;
    }

    @Override public boolean addComplexProperty(String path, boolean required) {
        return false;
    }

    @Override public boolean addComplexArrayProperty(String path, boolean required) {
        return false;
    }

    @Override public MongoDBCommandStatement createDDLStatement() {
        return new MongoDBCommandStatement("db.createCollection(" + kindName + ");", new BsonDocument("create", new BsonString(kindName)));
    }

}
