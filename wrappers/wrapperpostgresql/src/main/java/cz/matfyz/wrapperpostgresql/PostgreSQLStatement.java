package cz.matfyz.wrapperpostgresql;

import cz.matfyz.abstractwrappers.AbstractStatement;

/**
 * @author jachymb.bartik
 */
public class PostgreSQLStatement implements AbstractStatement {

    private String content;

    public PostgreSQLStatement(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }
    
}