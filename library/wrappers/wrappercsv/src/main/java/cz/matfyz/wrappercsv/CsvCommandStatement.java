package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractStatement;

public class CsvCommandStatement implements AbstractStatement {
    private final String content;

    public CsvCommandStatement(String content) {
        this.content = content;
    }

    @Override
    public String getContent() {
        return content;
    }
}
