package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractStatement;

/**
 * A concrete implementation of {@link AbstractStatement} representing a command statement
 * for JSON data. This class encapsulates a JSON schema as its content.
 */
public class JsonCommandStatement implements AbstractStatement {

    private final String schema;

    /**
     * Constructs a new {@code JsonCommandStatement} with the specified schema.
     *
     * @param schema the JSON schema content of the command statement.
     */
    public JsonCommandStatement(String schema) {
        this.schema = schema;
    }

    /**
     * Returns the JSON schema associated with this command statement.
     *
     * @return the JSON schema as a {@code String}.
     */
    public String getSchema() {
        return schema;
    }

    /**
     * Returns the content of the JSON command statement.
     *
     * @return the JSON schema as the content of this statement.
     */
    @Override public String getContent() {
        return schema;
    }
}
