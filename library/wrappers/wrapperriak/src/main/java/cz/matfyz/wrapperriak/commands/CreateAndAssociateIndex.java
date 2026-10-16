package cz.matfyz.wrapperriak.commands;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.RiakCommand;
import com.basho.riak.client.api.commands.ListenableFuture;
import com.basho.riak.client.api.commands.buckets.StoreBucketProperties;
import com.basho.riak.client.api.commands.search.StoreIndex;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakFuture;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.search.YokozunaIndex;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Command used to create Yokozuna index and associate it with a bucket.
 * This command internally executes {@link StoreIndex} and {@link StoreBucketProperties} commands.
 */
public class CreateAndAssociateIndex extends RiakCommand<Void, YokozunaIndex> {

    YokozunaIndex index;
    Namespace namespace;

    public CreateAndAssociateIndex(Builder builder) {
        this.index = builder.index;
        this.namespace = builder.namespace;
    }

    @Override protected RiakFuture<Void, YokozunaIndex> executeAsync(RiakCluster cluster) {
        return new ListenableFuture<>() {

            private final CompletableFuture<Void> future;

            {
                var client = new RiakClient(cluster);

                var storeIndex = new StoreIndex.Builder(index).build();

                var storeIndexFuture = new CompletableFuture<Void>();
                client.executeAsync(storeIndex).addListener(riakFuture -> {
                    if (riakFuture.isSuccess())
                        storeIndexFuture.complete(null);
                    else
                        storeIndexFuture.completeExceptionally(riakFuture.cause());
                });

                future = storeIndexFuture.thenCompose(f -> {
                    var storeBucketProps = new StoreBucketProperties.Builder(namespace)
                        .withSearchIndex(index.getName())
                        .build();

                    var storeBucketPropsFuture = new CompletableFuture<Void>();
                    client.executeAsync(storeBucketProps).addListener(riakFuture -> {
                        if (riakFuture.isSuccess())
                            storeBucketPropsFuture.complete(null);
                        else
                            storeBucketPropsFuture.completeExceptionally(riakFuture.cause());
                    });

                    return storeBucketPropsFuture;
                });
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
                }
                catch (ExecutionException e) {
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

            @Override public YokozunaIndex getQueryInfo() {
                return index;
            }
        };
    }

    public static class Builder {
        YokozunaIndex index;
        Namespace namespace;

        public Builder(YokozunaIndex index, Namespace namespace) {
            this.index = index;
            this.namespace = namespace;
        }

        public CreateAndAssociateIndex build() {
            return new CreateAndAssociateIndex(this);
        }
    }
}
