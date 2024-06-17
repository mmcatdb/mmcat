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
        if (settings.url.endsWith(".csv")) {
            return new UrlInputStreamProvider(settings.url).getInputStream();
        } else {
            return new UrlInputStreamProvider(settings.url + kindName + ".csv").getInputStream();
        }
    }

    /*
     * There is not a straight forward way to access filenames in remote folder
     * Therefore, right now it is possible to access local folders or files and remote just files.
     */
    public List<String> getCsvFileNames() throws URISyntaxException, IOException {
        URI uri = new URI(settings.url);
        if ("http".equals(uri.getScheme()) || "https".equals(uri.getScheme())) {
            return getRemoteCsvFileNames(uri);
        } else if ("file".equals(uri.getScheme())) {
            return getLocalCsvFileNames(uri);
        } else {
            throw new IllegalArgumentException("Unsupported URI scheme: " + uri.getScheme());
        }
    }

    private List<String> getRemoteCsvFileNames(URI uri) {
        List<String> csvFileNames = new ArrayList<>();
        if (settings.url.endsWith(".csv")) {
            csvFileNames.add(new File(uri.getPath()).getName().replace(".csv", ""));
        } else {
            throw new IllegalArgumentException("The provided URL does not point to a directory.");
        }
        return csvFileNames;
    }

    private List<String> getLocalCsvFileNames(URI uri) {
        List<String> csvFileNames = new ArrayList<>();
        File file = new File(uri);
        if (settings.url.endsWith(".csv")) {
            if (file.exists()) {
                csvFileNames.add(file.getName().replace(".csv", ""));
            } else {
                throw new IllegalArgumentException("The provided URL does not point to an existing file.");
            }
        } else if (file.isDirectory()) {
            File[] files = file.listFiles((dir, name) -> name.toLowerCase().endsWith(".csv"));
            if (files != null) {
                for (File csvFile : files) {
                    csvFileNames.add(csvFile.getName().replace(".csv", ""));
                }
            }
        } else {
            throw new IllegalArgumentException("The provided URL does not point to a directory or a valid file.");
        }
        return csvFileNames;
    }

    public record CsvSettings(
        String url,
        boolean isWritable,
        boolean isQueryable
    ) {}
}
