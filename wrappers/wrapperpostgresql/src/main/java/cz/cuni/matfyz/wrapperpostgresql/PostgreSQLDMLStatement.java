package cz.cuni.matfyz.wrapperpostgresql;

import cz.cuni.matfyz.statements.DMLStatement;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLDMLStatement implements DMLStatement {

    private final String content;
    
    public PostgreSQLDMLStatement(String content) {
        this.content = content;
    }
    
    @Override
    public String getContent() {
        return content;
    }
}
