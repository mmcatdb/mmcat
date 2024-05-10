package cz.matfyz.wrappercsv;

import java.io.IOException;
import java.io.InputStream;

import cz.matfyz.core.utils.InputStreamProvider.UrlInputStreamProvider;

public class CsvProvider {

    public final CsvSettings settings;

    public CsvProvider(CsvSettings settings) {
        this.settings = settings;
    }

    public InputStream getInputStream() throws IOException {
        return new UrlInputStreamProvider(settings.url).getInputStream();
    }

    public record CsvSettings(
        String url,
        boolean isWritable,
        boolean isQueryable
    ) {}

}
