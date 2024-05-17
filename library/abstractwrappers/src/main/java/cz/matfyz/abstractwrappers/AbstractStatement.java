package cz.matfyz.abstractwrappers;

public interface AbstractStatement {

    String getContent();

    public static AbstractStatement createEmpty() {
        return StringStatement.empty;
    }

    class StringStatement implements AbstractStatement {

        private final String content;

        public StringStatement(String content) {
            this.content = content;
        }

        public static StringStatement create(String content) {
            return "".equals(content) ? empty : new StringStatement(content);
        }

        @Override public String getContent() {
            return content;
        }

        private static final StringStatement empty = new StringStatement("");

    }

}
