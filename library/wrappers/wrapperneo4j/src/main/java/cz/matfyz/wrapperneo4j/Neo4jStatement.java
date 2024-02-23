package cz.matfyz.wrapperneo4j;

import cz.matfyz.abstractwrappers.AbstractStatement;

/**
 * @author jachymb.bartik
 */
public class Neo4jStatement implements AbstractStatement {

    private String content;

    public Neo4jStatement(String content) {
        this.content = content;
    }

    public String getContent() {
        return this.content;
    }

    private static final Neo4jStatement empty = new Neo4jStatement("");

    public static Neo4jStatement createEmpty() {
        return empty;
    }
}
