package cz.matfyz.wrapperriak.commands;

import com.basho.riak.client.api.RiakCommand;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakFuture;
import com.basho.riak.client.core.RiakFutureListener;

import java.util.concurrent.TimeUnit;

public class EmptyRiakCommand extends RiakCommand<Void, Void> {
    @Override protected RiakFuture<Void, Void> executeAsync(RiakCluster riakCluster) {
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

            @Override public Void getNow() {
                return null;
            }

            @Override public boolean isCancelled() {
                return false;
            }

            @Override public boolean isDone() {
                return true;
            }

            @Override public boolean isSuccess() {
                return true;
            }

            @Override public void await() {
                // already done
            }

            @Override public boolean await(long timeout, TimeUnit unit) {
                return true;
            }

            @Override public Throwable cause() {
                return null;
            }

            @Override public Void getQueryInfo() {
                return null;
            }

            @Override public void addListener(RiakFutureListener<Void, Void> listener) {
                // the future has already been completed, so we handle the listener
                listener.handle(this);
            }

            @Override public void removeListener(RiakFutureListener<Void, Void> listener) {
                // listeners have already been handled, there is no need to remove them now
            }
        };
    }
}
