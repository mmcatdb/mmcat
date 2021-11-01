package cz.cuni.matfyz.wrapperMongodb;

import cz.cuni.matfyz.statements.DDLStatement;

/**
 *
 * @author jachymb.bartik
 */
public class MongoDBDDLStatement implements DDLStatement {
    
    private final String content;
    
    public MongoDBDDLStatement(String content) {
        this.content = content;
    }
    
    public String getContent()
    {
        return content;
    }
}