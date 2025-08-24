package cz.matfyz.wrappercsv;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

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
     */
    public CsvProvider(CsvSettings settings) {
        this.settings = settings;
    }

    public String getUrl() {
        return settings.url;
    }

    public char getSeparator() {
        return settings.separator;
    }

    public boolean hasHeader() {
        return settings.hasHeader;
    }

    /**
     * Retrieves an input stream for the specified CSV file.
     */
    public InputStream getInputStream() throws IOException {
        return new UrlInputStreamProvider(settings.url).getInputStream();
    }

    /**
     * Retrieves a list of CSV file names from the specified URL.
     * Supports both local directories and remote file access.
     */
    public String getCsvFilenames() {
        String url = settings.url;
        return url.substring(url.lastIndexOf('/') + 1);
    }

    /**
     * A record representing CSV settings, including the URL, writability, and queryability of the CSV source.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CsvSettings(
        String url,
        char separator,
        boolean hasHeader,
        boolean isWritable,
        boolean isQueryable,
        boolean isClonable
    ) {}

}
