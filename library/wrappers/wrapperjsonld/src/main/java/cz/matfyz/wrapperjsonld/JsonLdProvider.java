package cz.matfyz.wrapperjsonld;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class JsonLdProvider {

    public final JsonLdSettings settings;

    public JsonLdProvider(JsonLdSettings settings) {
        this.settings = settings;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record JsonLdSettings(
        String url,
        boolean isWritable,
        boolean isQueryable,
        boolean isClonable
    ) {}

}
