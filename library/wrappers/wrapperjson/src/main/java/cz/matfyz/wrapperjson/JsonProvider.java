package cz.matfyz.wrapperjson;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import cz.matfyz.core.utils.InputStreamProvider.UrlInputStreamProvider;

public class JsonProvider {

    public final JsonSettings settings;

    public JsonProvider(JsonSettings settings) {
        this.settings = settings;
    }

    public String getUrl() {
        return settings.url;
    }

    public InputStream getInputStream(String kindName) throws IOException {
        if (settings.url.endsWith(".json")) {
            return new UrlInputStreamProvider(settings.url).getInputStream();
        } else {
            return new UrlInputStreamProvider(settings.url + kindName + ".json").getInputStream();
        }
    }

    /*
     * There is not a straight forward way to access filenames in remote folder
     * Therefore, right now it is possible to access local folders or files and remote just files.
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

    private List<String> getRemoteJsonFileNames(URI uri) {
        List<String> jsonFileNames = new ArrayList<>();
        if (settings.url.endsWith(".json")) {
            jsonFileNames.add(new File(uri.getPath()).getName().replace(".json", ""));
        } else {
            throw new IllegalArgumentException("The provided URL does not point to a directory.");
        }
        return jsonFileNames;
    }

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

    public record JsonSettings(
        String url,
        boolean isWritable,
        boolean isQueryable
    ) {}

}
