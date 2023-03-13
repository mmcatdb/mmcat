package cz.cuni.matfyz.wrapperneo4j;

import org.neo4j.driver.Session;

/**
 * @author jachymb.bartik
 */
public interface SessionProvider {
    
    public Session getSession();
    
}