package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractStatement;

public class JsonCommandStatement implements AbstractStatement {
    private final String schema;

    public JsonCommandStatement(String schema) {
        this.schema = schema;
    }

    public String getSchema() {
        return schema;
    }

    @Override
    public String getContent() {
        // return json schema as content of the statement.
        return schema;
    }
}
