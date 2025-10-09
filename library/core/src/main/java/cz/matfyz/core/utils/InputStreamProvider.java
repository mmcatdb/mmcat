package cz.matfyz.core.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;

public interface InputStreamProvider {

    InputStream getInputStream() throws IOException;

    class FileInputStreamProvider implements InputStreamProvider {

        private String filename;

        public FileInputStreamProvider(String filename) {
            this.filename = filename;
        }

        public InputStream getInputStream() throws IOException {
            try {
                final var fileUrl = ClassLoader.getSystemResource(filename);
                Path pathToDataFile = Paths.get(fileUrl.toURI()).toAbsolutePath();
                File dataFile = pathToDataFile.toFile();

                return new FileInputStream(dataFile);
            }
            catch (Exception e) {
                throw new IOException("Cannot read from local file " + filename + ".", e.getCause());
            }
        }

    }

    class UrlInputStreamProvider implements InputStreamProvider {

        private String url;

        public UrlInputStreamProvider(String url) {
            this.url = url;
        }

        public InputStream getInputStream() throws IOException {
            try {
                return new URI(url).toURL().openStream();
            }
            catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

    }

}
