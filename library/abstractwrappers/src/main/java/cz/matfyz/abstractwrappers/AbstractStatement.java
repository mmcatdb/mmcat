package cz.matfyz.abstractwrappers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface AbstractStatement {

    String getContent();

    /**
     * Used to divide all statements into groups.
     * All statements with the same priority can be executed in any order.
     * Statements with lower priority should be executed befor statements with higher priority.
     */
    int getPriority();

    static Collection<AbstractStatement> sortByPriority(Collection<AbstractStatement> statements) {
        final List<List<AbstractStatement>> groups = new ArrayList<>();
        for (final AbstractStatement statement : statements) {
            final int priority = statement.getPriority();
            while (groups.size() <= priority)
                groups.add(new ArrayList<>());
            groups.get(priority).add(statement);
        }

        if (groups.isEmpty())
            return new ArrayList<>();
        if (groups.size() == 1)
            return groups.get(0);

        final var output = new ArrayList<AbstractStatement>();
        for (final List<AbstractStatement> group : groups)
            output.addAll(group);

        return output;
    }

    static AbstractStatement createEmpty() {
        return StringStatement.empty;
    }

    class StringStatement implements AbstractStatement {

        private StringStatement(String content, int priority) {
            this.content = content;
            this.priority = priority;
        }

        public static StringStatement create(String content) {
            return "".equals(content) ? empty : new StringStatement(content, 0);
        }

        public static StringStatement create(String content, int priority) {
            return "".equals(content) ? empty : new StringStatement(content, priority);
        }

        private final String content;

        @Override public String getContent() {
            return content;
        }

        private final int priority;

        @Override public int getPriority() {
            return priority;
        }

        private static final StringStatement empty = new StringStatement("", 0);

    }

}
