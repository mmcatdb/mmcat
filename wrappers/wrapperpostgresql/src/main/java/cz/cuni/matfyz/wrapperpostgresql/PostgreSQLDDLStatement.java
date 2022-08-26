package cz.cuni.matfyz.wrapperpostgresql;

import cz.cuni.matfyz.statements.DDLStatement;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLDDLStatement implements DDLStatement {

    private final String content;
    
    public PostgreSQLDDLStatement(String content) {
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }

}