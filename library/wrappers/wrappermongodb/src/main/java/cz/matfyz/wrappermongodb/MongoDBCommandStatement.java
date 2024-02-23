package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractStatement;

import org.bson.BsonDocument;

/**
 * @author jachymb.bartik
 */
public class MongoDBCommandStatement implements AbstractStatement {

    private String content;
    private BsonDocument command;

    public MongoDBCommandStatement(String content, BsonDocument command) {
        this.content = content;
        this.command = command;
    }

    public String getContent() {
        return this.content;
    }

    public BsonDocument getCommand() {
        return this.command;
    }

}
