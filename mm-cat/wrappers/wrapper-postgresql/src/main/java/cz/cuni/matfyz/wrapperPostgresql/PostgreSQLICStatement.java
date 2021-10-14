package cz.cuni.matfyz.wrapperPostgresql;

import cz.cuni.matfyz.statements.ICStatement;

/**
 *
 */
public class PostgreSQLICStatement implements ICStatement
{
    private final String content;
    
    public PostgreSQLICStatement(String content) {
        this.content = content;
    }
    
    public String getContent()
    {
        return content;
    }
}