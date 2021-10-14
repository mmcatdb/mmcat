package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.statements.ICStatement;

/**
 *
 */
public class MongoDBICStatement implements ICStatement
{
    private String content;
    
    public MongoDBICStatement(String content) {
        this.content = content;
    }
    
    public String getContent()
    {
        return content;
    }
}