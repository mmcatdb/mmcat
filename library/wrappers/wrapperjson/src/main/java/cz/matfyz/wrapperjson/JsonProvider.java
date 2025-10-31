package cz.matfyz.wrapperjson;

import cz.matfyz.abstractwrappers.AbstractDatasourceProvider;
import cz.matfyz.core.utils.FileUtils;
import cz.matfyz.core.utils.InputStreamProvider.UrlInputStreamProvider;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A provider class for accessing JSON files based on given settings.
 * This class provides methods to retrieve input streams and JSON file names
 * from both local and remote sources.
 */
public class JsonProvider implements AbstractDatasourceProvider {

    /** The settings used by this provider for accessing JSON files. */
    final JsonSettings settings;

    /**
     * Constructs a new {@code JsonProvider} with the specified settings.
     */
    public JsonProvider(JsonSettings settings) {
        this.settings = settings;
    }

    @Override public boolean isStillValid(Object settings) {
        // We don't cache anything so the provider is always valid.
        return true;
    }

    @Override public void close() {
        // Nothing to close.
    }

    private @Nullable String kindName = null;

    public String getKindName() {
        if (kindName != null)
            kindName = FileUtils.extractBaseName(settings.url);
        return kindName;
    }
    /**
     * Retrieves an input stream for the specified JSON file.
     */
    public InputStream getInputStream() throws IOException {
        return new UrlInputStreamProvider(settings.url).getInputStream();
    }

    /**
     * A record representing JSON settings, including the URL, writability, and queryability of the JSON source.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JsonSettings(
        String url,
        boolean isWritable,
        boolean isQueryable,
        boolean isClonable
    ) {}

}
