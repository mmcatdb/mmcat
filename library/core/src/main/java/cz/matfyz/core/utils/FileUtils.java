package cz.matfyz.core.utils;

import java.io.File;
import java.net.URI;

public class FileUtils {

    /**
     * Extracts the base name (file name without path and extension) from a given input string.
     * The input can be a local file path or a URL.
     */
    public static String extractBaseName(String input) {
        String filename;

        try {
            // Try to parse the input as URL. The `getPath` method excludes query & fragment.
            final String path = URI.create(input).getPath();
            filename = path.substring(path.lastIndexOf('/') + 1);
        }
        catch (Exception e) {
            // Otherwise, treat it as a local file path.
            // This should work on different OSes. Obv, a path from one OS wouldn't work on another OS, so this can't be easily tested.
            filename = new File(input).getName();
        }

        // Strip extension if present
        final int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0)
            filename = filename.substring(0, dotIndex);

        return filename;
    }

}
