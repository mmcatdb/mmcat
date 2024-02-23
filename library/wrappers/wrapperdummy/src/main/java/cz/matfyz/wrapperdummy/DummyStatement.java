package cz.matfyz.wrapperdummy;

import cz.matfyz.abstractwrappers.AbstractStatement;

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
