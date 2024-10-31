package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractStatement;

/**
 * A concrete implementation of {@link AbstractStatement} representing a command statement
 * for CSV data. This class encapsulates a CSV command as a string content.
 */
public class CsvCommandStatement implements AbstractStatement {

    private final String content;

    /**
     * Constructs a new {@code CsvCommandStatement} with the specified content.
     *
     * @param content the content of the CSV command statement.
     */
    public CsvCommandStatement(String content) {
        this.content = content;
    }

    @Override public String getContent() {
        return content;
    }
}
