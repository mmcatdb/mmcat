package cz.matfyz.wrapperriak;

import cz.matfyz.abstractwrappers.AbstractStatement;
import cz.matfyz.wrapperriak.commands.EmptyRiakCommand;

import com.basho.riak.client.api.RiakCommand;
import com.basho.riak.client.api.RiakClient;

public class RiakCommandStatement implements AbstractStatement {

    private final String content;
    private final RiakCommand<?, ?> command;

    public RiakCommandStatement(String content, RiakCommand<?, ?> command) {
        // the content is not created from the command using the toString method, but is passed as a parameter instead to allow e.g. simple export to sh script
        this.content = content;
        this.command = command;
    }

    /**
     * Returns human-readable statement content that is used only for logging.
     * @return human-readable content
     */
    @Override public String getContent() {
        return this.content;
    }

    @Override public int getPriority() {
        // TODO: should the riak commands have a priority?
        return 0;
    }

    /**
     * Returns {@link RiakCommand} that {@link RiakClient} can execute.
     * @return command to execute
     */
    public RiakCommand<?, ?> getCommand() {
        return this.command;
    }

    /**
     * Returns empty no-op {@link RiakCommandStatement} that does nothing when executed.
     * @return empty RiakCommandStatement
     */
    static RiakCommandStatement createEmpty() {
        return RiakCommandStatement.empty;
    }

    private static final RiakCommandStatement empty = new RiakCommandStatement("", new EmptyRiakCommand());
}
