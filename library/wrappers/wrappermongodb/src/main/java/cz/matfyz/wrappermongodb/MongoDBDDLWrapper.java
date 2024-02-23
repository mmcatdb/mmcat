package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractDDLWrapper;

import java.util.Set;

import org.bson.BsonDocument;
import org.bson.BsonString;

/**
 * @author jachymb.bartik
 */
public class MongoDBDDLWrapper implements AbstractDDLWrapper {

    private String kindName = null;

    @Override public void setKindName(String name) {
        kindName = name;
    }

    @Override public boolean isSchemaLess() {
        return true;
    }

    @Override public boolean addSimpleProperty(Set<String> names, boolean required) {
        return false;
    }

    @Override public boolean addSimpleArrayProperty(Set<String> names, boolean required) {
        return false;
    }

    @Override public boolean addComplexProperty(Set<String> names, boolean required) {
        return false;
    }

    @Override public boolean addComplexArrayProperty(Set<String> names, boolean required) {
        return false;
    }

    @Override public MongoDBCommandStatement createDDLStatement() {
        return new MongoDBCommandStatement("db.createCollection(" + kindName + ");", new BsonDocument("create", new BsonString(kindName)));
    }

}
