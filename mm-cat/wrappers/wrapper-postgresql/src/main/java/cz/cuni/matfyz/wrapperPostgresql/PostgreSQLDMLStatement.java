package cz.cuni.matfyz.wrapperPostgresql;

import cz.cuni.matfyz.statements.DMLStatement;

/**
 *
 */
public class PostgreSQLDMLStatement implements DMLStatement
{
    private final String content;
    
    public PostgreSQLDMLStatement(String content) {
        this.content = content;
    }
    
    public String getContent()
    {
        return content;
    }
}
