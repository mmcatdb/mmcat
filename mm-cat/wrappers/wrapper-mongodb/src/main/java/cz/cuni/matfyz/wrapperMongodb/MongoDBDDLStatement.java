package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.statements.DDLStatement;

/**
 *
 */
public class MongoDBDDLStatement implements DDLStatement {
    
    private String content;
    
    public MongoDBDDLStatement(String content) {
        this.content = content;
    }
    
    public String getContent()
    {
        return content;
    }
}