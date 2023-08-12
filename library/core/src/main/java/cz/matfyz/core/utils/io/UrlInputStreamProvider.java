package cz.matfyz.core.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author jachym.bartik
 */
public class UrlInputStreamProvider implements InputStreamProvider {

    private String url;

    public UrlInputStreamProvider(String url) {
        this.url = url;
    }

    public InputStream getInputStream() throws IOException {
        final var fileUrl = new URL(url);
        return fileUrl.openStream();
    }
    
}
