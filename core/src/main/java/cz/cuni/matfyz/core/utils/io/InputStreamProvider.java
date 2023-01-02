package cz.cuni.matfyz.core.utils.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author jachym.bartik
 */
public interface InputStreamProvider {

    public InputStream getInputStream() throws IOException;
    
}
