package cz.matfyz.wrapperjsonld;

public class JsonLdProvider {

    public final JsonLdSettings settings;

    public JsonLdProvider(JsonLdSettings settings) {
        this.settings = settings;
    }

    public record JsonLdSettings(
        String url,
        boolean isWritable,
        boolean isQueryable
    ) {}

}
