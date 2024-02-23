package cz.matfyz.core.utils.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author jachym.bartik
 */
public class FileInputStreamProvider implements InputStreamProvider {

    private String fileName;

    public FileInputStreamProvider(String fileName) {
        this.fileName = fileName;
    }

    public InputStream getInputStream() throws IOException {
        try {
            final var fileUrl = ClassLoader.getSystemResource(fileName);
            Path pathToDataFile = Paths.get(fileUrl.toURI()).toAbsolutePath();
            File dataFile = pathToDataFile.toFile();

            return new FileInputStream(dataFile);
        }
        catch (Exception e) {
            throw new IOException("Cannot read from local file " + fileName + ".", e.getCause());
        }
    }

}
