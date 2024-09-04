package cz.matfyz.wrapperjson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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
     *
     * @param settings the settings used to configure this provider.
     */
    public JsonProvider(JsonSettings settings) {
        this.settings = settings;
    }

    public String getUrl() {
        return settings.url;
    }

    /**
     * Retrieves an input stream for the specified JSON file.
     * If the URL points directly to a JSON file, that file is used.
     * Otherwise, a JSON file corresponding to the kind name is accessed.
     *
     * @param kindName the name of the JSON kind or type.
     * @return an input stream for the JSON file.
     * @throws IOException if an I/O error occurs while retrieving the input stream.
     */
    public InputStream getInputStream(String kindName) throws IOException {
        if (settings.url.endsWith(".json")) {
            return new UrlInputStreamProvider(settings.url).getInputStream();
        } else {
            return new UrlInputStreamProvider(settings.url + kindName + ".json").getInputStream();
        }
    }

    /*
     * There is not a straightforward way to access filenames in remote folder.
     * Therefore, right now it is possible to access local folders or files and remote just files.
     */
    /**
     * Retrieves a list of JSON file names from the specified URL.
     * Supports both local directories and remote file access.
     *
     * @return a list of JSON file names (without the ".json" extension).
     * @throws URISyntaxException if the URL is not formatted correctly.
     * @throws IOException if an I/O error occurs while accessing local or remote files.
     */
    public List<String> getJsonFileNames() throws URISyntaxException, IOException {
        URI uri = new URI(settings.url);
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
            return getRemoteJsonFileNames(uri);
        } else if ("file".equals(uri.getScheme())) {
            return getLocalJsonFileNames(uri);
        } else {
            throw new IllegalArgumentException("Unsupported URI scheme: " + uri.getScheme());
        }
    }

    /**
     * Retrieves a list of JSON file names from a remote location.
     * Currently only supports URLs pointing directly to a JSON file.
     *
     * @param uri the URI pointing to the remote location.
     * @return a list of JSON file names (without the ".json" extension).
     */
    private List<String> getRemoteJsonFileNames(URI uri) {
        List<String> jsonFileNames = new ArrayList<>();
        if (settings.url.endsWith(".json")) {
            jsonFileNames.add(new File(uri.getPath()).getName().replace(".json", ""));
        } else {
            throw new IllegalArgumentException("The provided URL does not point to a directory.");
        }
        return jsonFileNames;
    }

    /**
     * Retrieves a list of JSON file names from a local directory or file.
     *
     * @param uri the URI pointing to the local directory or file.
     * @return a list of JSON file names (without the ".json" extension).
     */
    private List<String> getLocalJsonFileNames(URI uri) {
        List<String> jsonFileNames = new ArrayList<>();
        File file = new File(uri);
        if (settings.url.endsWith(".json")) {
            if (file.exists()) {
                jsonFileNames.add(file.getName().replace(".json", ""));
            } else {
                throw new IllegalArgumentException("The provided URL does not point to an existing file.");
            }
        } else if (file.isDirectory()) {
            File[] files = file.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
            if (files != null) {
                for (File jsonFile : files) {
                    jsonFileNames.add(jsonFile.getName().replace(".json", ""));
                }
            }
        } else {
            throw new IllegalArgumentException("The provided URL does not point to a directory or a valid file.");
        }
        return jsonFileNames;
    }

    /**
     * A record representing JSON settings, including the URL, writability, and queryability of the JSON source.
     */
    public record JsonSettings(
        String url,
        boolean isWritable,
        boolean isQueryable
    ) {}

}
