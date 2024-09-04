package cz.matfyz.wrappercsv;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import cz.matfyz.core.utils.InputStreamProvider.UrlInputStreamProvider;

/**
 * A provider class for accessing CSV files based on given settings.
 * This class provides methods to retrieve input streams and CSV file names
 * from both local and remote sources.
 */
public class CsvProvider {

    /** The settings used by this provider for accessing CSV files. */
    public final CsvSettings settings;

    /**
     * Constructs a new {@code CsvProvider} with the specified settings.
     *
     * @param settings the settings used to configure this provider.
     */
    public CsvProvider(CsvSettings settings) {
        this.settings = settings;
    }

    public String getUrl() {
        return settings.url;
    }

    /**
     * Retrieves an input stream for the specified CSV file.
     * If the URL points directly to a CSV file, that file is used.
     * Otherwise, a CSV file corresponding to the kind name is accessed.
     *
     * @param kindName the name of the CSV kind or type.
     * @return an input stream for the CSV file.
     * @throws IOException if an I/O error occurs while retrieving the input stream.
     */
    public InputStream getInputStream(String kindName) throws IOException {
        if (settings.url.endsWith(".csv")) {
            return new UrlInputStreamProvider(settings.url).getInputStream();
        } else {
            return new UrlInputStreamProvider(settings.url + kindName + ".csv").getInputStream();
        }
    }

    /**
     * There is not a straightforward way to access filenames in remote folder
     * Therefore, right now it is possible to access local folders or files and remote just files.
    */

    /**
     * Retrieves a list of CSV file names from the specified URL.
     * Supports both local directories and remote file access.
     *
     * @return a list of CSV file names (without the ".csv" extension).
     * @throws URISyntaxException if the URL is not formatted correctly.
     * @throws IOException if an I/O error occurs while accessing local or remote files.
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

    /**
     * Retrieves a list of CSV file names from a remote location.
     * Currently only supports URLs pointing directly to a CSV file.
     *
     * @param uri the URI pointing to the remote location.
     * @return a list of CSV file names (without the ".csv" extension).
     */
    private List<String> getRemoteCsvFileNames(URI uri) {
        List<String> csvFileNames = new ArrayList<>();
        if (settings.url.endsWith(".csv")) {
            csvFileNames.add(new File(uri.getPath()).getName().replace(".csv", ""));
        } else {
            throw new IllegalArgumentException("The provided URL does not point to a directory.");
        }
        return csvFileNames;
    }

    /**
     * Retrieves a list of CSV file names from a local directory or file.
     *
     * @param uri the URI pointing to the local directory or file.
     * @return a list of CSV file names (without the ".csv" extension).
     */
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

    /**
     * A record representing CSV settings, including the URL, writability, and queryability of the CSV source.
     */
    public record CsvSettings(
        String url,
        boolean isWritable,
        boolean isQueryable
    ) {}
}
