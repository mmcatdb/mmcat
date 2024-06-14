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
        return new UrlInputStreamProvider(settings.url + kindName + ".json").getInputStream();
    }

    public List<String> getJsonFileNames() throws URISyntaxException {
        List<String> jsonFileNames = new ArrayList<>();
        URI uri = new URI(settings.url);
        File directory = new File(uri);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
            if (files != null) {
                for (File file : files) {
                    jsonFileNames.add(file.getName().replace(".json", ""));
                }
            }
        } else {
            throw new IllegalArgumentException("The provided URL does not point to a directory.");
        }
        return jsonFileNames;
    }

    public record JsonSettings(
        String url,
        boolean isWritable,
        boolean isQueryable
    ) {}

}
