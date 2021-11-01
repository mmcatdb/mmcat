package cz.cuni.matfyz.wrapperPostgresql;

import java.sql.Connection;

/**
 *
 * @author jachymb.bartik
 */
public interface ConnectionProvider
{
    public Connection getConnection();
}