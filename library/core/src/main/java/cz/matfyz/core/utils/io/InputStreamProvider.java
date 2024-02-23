package cz.matfyz.core.utils.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jachym.bartik
 */
public interface InputStreamProvider {

    InputStream getInputStream() throws IOException;

}
