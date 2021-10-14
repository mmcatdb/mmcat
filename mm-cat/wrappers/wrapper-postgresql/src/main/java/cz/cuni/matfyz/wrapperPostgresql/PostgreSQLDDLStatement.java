package cz.cuni.matfyz.wrapperPostgresql;

import cz.cuni.matfyz.statements.DDLStatement;

/**
 *
 */
public class PostgreSQLDDLStatement implements DDLStatement
{
    private final String content;
    
    public PostgreSQLDDLStatement(String content) {
        this.content = content;
    }
    
    public String getContent()
    {
        return content;
    }
}