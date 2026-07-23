package cz.matfyz.wrapperriak;

import cz.matfyz.abstractwrappers.AbstractStatement;

import com.basho.riak.client.api.RiakCommand;
import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakFuture;
import com.basho.riak.client.core.RiakFutureListener;
import com.basho.riak.client.core.query.Location;

import java.util.concurrent.TimeUnit;

public class RiakCommandStatement implements AbstractStatement {

    private final String content;
    private final RiakCommand<?, Location> command;

    public RiakCommandStatement(String content, RiakCommand<?, Location> command) {
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
    public RiakCommand<?, Location> getCommand() {
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

    private static class EmptyRiakCommand extends RiakCommand<Void, Location> {
        @Override protected RiakFuture<Void, Location> executeAsync(RiakCluster riakCluster) {
            // return RiakFuture that is already completed
            return new RiakFuture<>() {
                @Override public boolean cancel(boolean mayInterruptIfRunning) {
                    return false;
                }

                @Override public Void get() {
                    return null;
                }

                @Override public Void get(long timeout, TimeUnit unit) {
                    return null;
                }

                @Override public boolean isCancelled() {
                    return false;
                }

                @Override public boolean isDone() {
                    return true;
                }

                @Override public void await() {
                    // already done
                }

                @Override public boolean await(long timeout, TimeUnit unit) {
                    return true;
                }

                @Override public Void getNow() {
                    return null;
                }

                @Override public boolean isSuccess() {
                    return true;
                }

                @Override public Throwable cause() {
                    return null;
                }

                @Override public Location getQueryInfo() {
                    return null;
                }

                @Override public void addListener(RiakFutureListener<Void, Location> listener) {
                    // the future has already been completed, so we handle the listener
                    listener.handle(this);
                }

                @Override public void removeListener(RiakFutureListener<Void, Location> listener) {
                    // listeners have already been handled, there is no need to remove them now
                }
            };
        }
    }
}
