package cz.matfyz.wrapperjson;

public class JsonProvider {

    public final JsonSettings settings;

    public JsonProvider(JsonSettings settings) {
        this.settings = settings;
    }

    public String getUrl() {
        return settings.url;
    }

    public record JsonSettings(
        String url,
        boolean isWritable,
        boolean isQueryable
    ) {}

}
