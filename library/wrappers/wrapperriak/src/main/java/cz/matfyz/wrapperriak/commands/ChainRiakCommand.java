package cz.matfyz.wrapperriak.commands;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.RiakCommand;
import com.basho.riak.client.api.commands.ListenableFuture;
import com.basho.riak.client.core.*;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * {@link RiakCommand} consisting of a {@link RiakCommand}s chain.
 */
public class ChainRiakCommand extends RiakCommand<Void, Void> {

    private final RiakCommand<?, ?>[] commands;

    /**
     * Creates a new {@link ChainRiakCommand}.
     * @param commands Sequence of commands, commands will be executed in the given order.
     */
    public ChainRiakCommand(RiakCommand<?, ?>... commands) {
        if (Arrays.stream(commands).anyMatch(Objects::isNull))
            throw new IllegalArgumentException("Commands cannot be null");
        this.commands = commands;
    }

    @Override protected RiakFuture<Void, Void> executeAsync(RiakCluster cluster) {
        return new ListenableFuture<>() {

            private CompletableFuture<Void> future;

            {
                var client = new RiakClient(cluster);
                future = CompletableFuture.completedFuture(null);

                for (var command : commands) {
                    future = future.thenCompose(res -> {
                        var completableFuture = new CompletableFuture<Void>();

                        client.executeAsync(command).addListener(riakFuture -> {
                            if (riakFuture.isSuccess())
                                completableFuture.complete(null);
                            else
                                completableFuture.completeExceptionally(riakFuture.cause());
                        });

                        return completableFuture;
                    });
                }
            }

            @Override public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override public Void get() throws InterruptedException, ExecutionException {
                return future.get();
            }

            @Override public Void get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                return future.get(timeout, unit);
            }

            @Override public Void getNow() {
                return null;
            }

            @Override public boolean isCancelled() {
                return false;
            }

            @Override public boolean isDone() {
                return future.isDone();
            }

            @Override public boolean isSuccess() {
                return future.isDone() && !future.isCompletedExceptionally();
            }

            @Override public void await() throws InterruptedException {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    return;
                }
            }

            @Override public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
                try {
                    future.get(timeout, unit);
                    return true;
                }
                catch (ExecutionException e) {
                    return true;
                }
                catch (TimeoutException e) {
                    return false;
                }
            }

            @Override public Throwable cause() {
                return future.exceptionNow();
            }

            @Override public Void getQueryInfo() {
                return null;
            }
        };
    }
}
