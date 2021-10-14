package cz.cuni.matfyz.wrapperPostgresql;

import java.sql.Connection;

/**
 *
 */
public interface ConnectionProvider
{
    public Connection getConnection();
}