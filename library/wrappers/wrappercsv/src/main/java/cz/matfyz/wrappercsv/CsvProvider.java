package cz.matfyz.wrappercsv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import cz.matfyz.core.utils.InputStreamProvider.UrlInputStreamProvider;

public class CsvProvider {

    public final CsvSettings settings;

    public CsvProvider(CsvSettings settings) {
        this.settings = settings;
    }

    public String getUrl() {
        return settings.url;
    }

    public InputStream getInputStream(String kindName) throws IOException {
        return new UrlInputStreamProvider(settings.url + kindName + ".csv").getInputStream();
    }

    public List<String> getCsvFileNames() throws URISyntaxException {
        List<String> csvFileNames = new ArrayList<>();
        URI uri = new URI(settings.url);
        File directory = new File(uri);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
            if (files != null) {
                for (File file : files) {
                    csvFileNames.add(file.getName().replace(".csv", ""));
                }
            }
        } else {
            throw new IllegalArgumentException("The provided URL does not point to a directory.");
        }
        return csvFileNames;
    }

    public record CsvSettings(
        String url,
        boolean isWritable,
        boolean isQueryable
    ) {}
}
