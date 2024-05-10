package cz.matfyz.wrapperjson;

import java.io.IOException;
import java.io.InputStream;

import cz.matfyz.core.utils.InputStreamProvider.UrlInputStreamProvider;

public class JsonProvider {

    public final JsonSettings settings;

    public JsonProvider(JsonSettings settings) {
        this.settings = settings;
    }

    public String getUrl() {
        return settings.url;
    }

    public InputStream getInputStream() throws IOException {
        return new UrlInputStreamProvider(settings.url).getInputStream();
    }

    public record JsonSettings(
        String url,
        boolean isWritable,
        boolean isQueryable
    ) {}

}
