package cz.cuni.matfyz.wrappermongodb;

import cz.cuni.matfyz.statements.AbstractStatement;

/**
 * @author jachymb.bartik
 */
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