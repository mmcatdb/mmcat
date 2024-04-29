package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractStatement;

public class MongoDBEmptyStatement implements AbstractStatement {

    private MongoDBEmptyStatement() {}

    public String getContent() {
        return "";
    }

    private static MongoDBEmptyStatement instance = new MongoDBEmptyStatement();

    public static MongoDBEmptyStatement getInstance() {
        return instance;
    }

}
