package cz.matfyz.core.utils.io;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author jachym.bartik
 */
public class UrlInputStreamProvider implements InputStreamProvider {

    private String url;

    public UrlInputStreamProvider(String url) {
        this.url = url;
    }

    public InputStream getInputStream() throws IOException {
        try {
            return new URI(url).toURL().openStream();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

}
