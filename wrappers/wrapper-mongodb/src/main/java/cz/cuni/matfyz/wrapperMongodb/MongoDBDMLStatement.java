package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.statements.DMLStatement;

/**
 *
 * @author jachymb.bartik
 */
public class MongoDBDMLStatement implements DMLStatement
{
    private final String content;
    
    public MongoDBDMLStatement(String content) {
        this.content = content;
    }
    
    public String getContent()
    {
        return content;
    }
}