package cz.matfyz.wrappercsv;

import cz.matfyz.abstractwrappers.AbstractDatasourceProvider;
import cz.matfyz.core.utils.FileUtils;
import cz.matfyz.core.utils.InputStreamProvider.UrlInputStreamProvider;

import java.io.IOException;
import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * A provider class for accessing CSV files based on given settings.
 * This class provides methods to retrieve input streams and CSV file names
 * from both local and remote sources.
 */
public class CsvProvider implements AbstractDatasourceProvider {

    /** The settings used by this provider for accessing CSV files. */
    final CsvSettings settings;

    /**
     * Constructs a new {@code CsvProvider} with the specified settings.
     */
    public CsvProvider(CsvSettings settings) {
        this.settings = settings;
    }

    @Override public boolean isStillValid(Object settings) {
        // We don't cache anything so the provider is always valid.
        return true;
    }

    @Override public void close() {
        // Nothing to close.
    }

    public String getKindName() {
        return FileUtils.extractBaseName(settings.url);
    }

    /**
     * Retrieves an input stream for the specified CSV file.
     */
    public InputStream getInputStream() throws IOException {
        return new UrlInputStreamProvider(settings.url).getInputStream();
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
