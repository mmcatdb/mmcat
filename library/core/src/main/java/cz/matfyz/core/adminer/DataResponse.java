package cz.matfyz.core.adminer;

import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Represents a generic data response.
 */
public abstract class DataResponse {
    private Metadata metadata;

    protected DataResponse(int itemCount, Set<String> propertyNames) {
        this.metadata = new Metadata(itemCount, propertyNames);
    }

    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Represents metadata for the response.
     */
    public record Metadata (int itemCount, @Nullable Set<String> propertyNames) {}
}
