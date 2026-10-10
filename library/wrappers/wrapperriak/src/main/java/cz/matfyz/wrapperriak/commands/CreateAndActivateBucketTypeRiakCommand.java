package cz.matfyz.wrapperriak.commands;

import com.basho.riak.client.api.RiakCommand;
import com.basho.riak.client.api.commands.ListenableFuture;
import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakFuture;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class CreateAndActivateBucketTypeRiakCommand extends RiakCommand<Void, String> {

    private final String bucketType;
    private final String props;

    public CreateAndActivateBucketTypeRiakCommand(Builder builder) {
        this.bucketType = builder.bucketType;
        this.props = builder.props;
    }

    protected RiakFuture<Void, String> executeAsync(RiakCluster cluster) {
        return new ListenableFuture<>() {

            private final CompletableFuture<Void> future;

            {
                future = execCommand(new String[] { "riak-admin", "bucket-type", "create", bucketType, props })
                    .thenCompose(p -> execCommand(new String[] { "riak-admin", "bucket-type", "activate", bucketType }));

                future.whenCompleteAsync((p, e) -> notifyListeners());
            }

            private CompletableFuture<Void> execCommand(String[] command) {
                try {
                    // riak-admin does not use stderr and prints error messages to stdout instead.
                    // Therefore, we need to read stdout in case of an error.
                    // We redirect stderr to stdout just to be sure no potential error message is missed.
                    Process process = new ProcessBuilder(command)
                        .redirectErrorStream(true)
                        .start();
                    return process.onExit()
                        .thenComposeAsync(p -> {
                            if (p.exitValue() != 0) {
                                try (BufferedReader reader = p.inputReader()) {
                                    String message = reader.lines().collect(Collectors.joining("\n"));
                                    return CompletableFuture.failedFuture(new Exception(message));
                                }
                                catch (IOException e) {
                                    return CompletableFuture.failedFuture(e);
                                }
                            }

                            return CompletableFuture.completedFuture(null);
                        });
                }
                catch (IOException e) {
                    return CompletableFuture.failedFuture(e);
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

            @Override public String getQueryInfo() {
                return bucketType;
            }
        };
    }

    public static class Builder {
        private final String bucketType;
        private String props;

        public Builder(String bucketType) {
            if (bucketType == null || bucketType.isBlank())
                throw new IllegalArgumentException("Bucket type cannot be null nor blank");

            this.bucketType = bucketType;
            this.props = "";
        }

        /**
         * Sets bucket type properties.
         * <p>
         * JSON string must include the {@code "props"} top-level object.
         * <pre>{@code {"props":{...}}}</pre>
         *
         * @param props JSON with properties
         * @return this builder
         */
        public Builder withProps(String props) {
            this.props = props;
            return this;
        }

        /**
         * Sets bucket type properties from a JSON file.
         * <p>
         * Content of the JSON file must include the {@code "props"} top-level object.
         * <pre>{@code {"props":{...}}}</pre>
         *
         * @param path path to the JSON file
         * @return this builder
         * @throws IOException I/O error occurred while reading the file
         */
        public Builder withProps(Path path) throws IOException {
            this.props = Files.readString(path);
            return this;
        }

        public CreateAndActivateBucketTypeRiakCommand build() {
            return new CreateAndActivateBucketTypeRiakCommand(this);
        }
    }
}
