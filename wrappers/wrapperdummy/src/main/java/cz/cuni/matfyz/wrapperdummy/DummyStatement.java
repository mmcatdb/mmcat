package cz.cuni.matfyz.wrapperdummy;

import cz.cuni.matfyz.statements.AbstractStatement;

/**
 * @author jachymb.bartik
 */
public class DummyStatement implements AbstractStatement {

    private String content;

    public DummyStatement(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }
    
}