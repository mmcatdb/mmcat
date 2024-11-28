package cz.matfyz.abstractwrappers;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.checkerframework.checker.nullness.qual.Nullable;

public interface AbstractDMLWrapper {

    /**
     * Prepares the wrapper for the next kind. Very important - don't underestimate!
     */
    void clear();

    void setKindName(String name);

    void append(String name, @Nullable Object value);

    AbstractStatement createDMLStatement();

    /**
     * The statements are joined depending on the datasource. E.g., JSON statements have to be put into array and joined with commas, while SQL statements don't really care.
     */
    default String joinStatements(Iterable<AbstractStatement> statements) {
        return StreamSupport.stream(statements.spliterator(), false)
            .map(AbstractStatement::getContent)
            .collect(Collectors.joining("\n"));

        // TODO This method can be eventually extended to support streams.
        // The expected behavior is that there would be some PushWrapper, which would be able to collect statements for various outputs (to string, to database driver, to file, to network, etc.).
        // The wrapper itself would then decide how to handle the data.
        // Of course, the whole DML algorithm (and especially the MTC algorithm) would have to be adjusted to support this.
    }

}
