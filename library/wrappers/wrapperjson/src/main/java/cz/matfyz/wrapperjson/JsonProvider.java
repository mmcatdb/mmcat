package cz.matfyz.wrapperjson;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import cz.matfyz.core.utils.InputStreamProvider.UrlInputStreamProvider;

/**
 * A provider class for accessing JSON files based on given settings.
 * This class provides methods to retrieve input streams and JSON file names
 * from both local and remote sources.
 */
public class JsonProvider {

    /** The settings used by this provider for accessing JSON files. */
    public final JsonSettings settings;

    /**
     * Constructs a new {@code JsonProvider} with the specified settings.
     */
    public JsonProvider(JsonSettings settings) {
        this.settings = settings;
    }

    public String getUrl() {
        return settings.url;
    }

    /**
     * Retrieves an input stream for the specified JSON file.
     */
    public InputStream getInputStream() throws IOException {
        return new UrlInputStreamProvider(settings.url).getInputStream();
    }

    /**
     * Retrieves a list of JSON file names from the specified URL.
     * Supports both local directories and remote file access.
     */
    public String getJsonFilenames() {
        String url = settings.url;
        return url.substring(url.lastIndexOf('/') + 1);
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
