package cz.matfyz.wrappermongodb;

import cz.matfyz.abstractwrappers.AbstractStatement;

import org.bson.BsonDocument;

public class MongoDBCommandStatement implements AbstractStatement {

    private String content;
    private BsonDocument command;

    public MongoDBCommandStatement(String content, BsonDocument command) {
        this.content = content;
        this.command = command;
    }

    @Override public String getContent() {
        return this.content;
    }

    @Override public int getPriority() {
        // There is no priority for MongoDB commands.
        return 0;
    }

    public BsonDocument getCommand() {
        return this.command;
    }

}
