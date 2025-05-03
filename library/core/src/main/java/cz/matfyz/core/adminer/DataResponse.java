package cz.matfyz.core.adminer;

import java.util.List;

/**
 * Represents a generic data response.
 */
public abstract class DataResponse {
    private Metadata metadata;

    protected DataResponse(long itemCount, List<String> propertyNames) {
        this.metadata = new Metadata(itemCount, propertyNames);
    }

    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * Represents metadata for the response.
     */
    public record Metadata (long itemCount, List<String> propertyNames) {}

    public abstract String getType();

}
