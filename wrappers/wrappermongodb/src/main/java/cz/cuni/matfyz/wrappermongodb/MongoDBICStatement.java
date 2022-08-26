package cz.cuni.matfyz.wrappermongodb;

import cz.cuni.matfyz.statements.ICStatement;

/**
 * @author jachymb.bartik
 */
public class MongoDBICStatement implements ICStatement {

    private final String content;
    
    public MongoDBICStatement(String content) {
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }
}