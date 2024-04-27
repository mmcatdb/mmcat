package cz.matfyz.wrapperjsonld;

/**
 * @author jachymb.bartik
 */
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
