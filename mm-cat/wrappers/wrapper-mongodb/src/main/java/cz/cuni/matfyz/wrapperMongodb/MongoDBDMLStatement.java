package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.statements.DMLStatement;

/**
 *
 */
public class MongoDBDMLStatement implements DMLStatement
{
    private String content;
    
    public MongoDBDMLStatement(String content) {
        this.content = content;
    }
    
    public String getContent()
    {
        return content;
    }
}