package cz.cuni.matfyz.server.repository.utils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 * @author jachym.bartik
 */
public interface DatabaseGetBooleanFunction {

    void execute(Connection connection, BooleanOutput output) throws SQLException;

}
