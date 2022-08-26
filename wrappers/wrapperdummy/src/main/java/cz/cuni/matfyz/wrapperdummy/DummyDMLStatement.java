package cz.cuni.matfyz.wrapperdummy;

import cz.cuni.matfyz.statements.DMLStatement;

/**
 * @author jachymb.bartik
 */
public class DummyDMLStatement implements DMLStatement {

    private final String content;
    
    public DummyDMLStatement(String content) {
        this.content = content;
    }
    
    public String getContent() {
        return content;
    }
}