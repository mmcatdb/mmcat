package cz.cuni.matfyz.wrapperpostgresql;

import java.sql.Connection;

/**
 * @author jachymb.bartik
 */
public interface ConnectionProvider {

    public Connection getConnection();
    
}