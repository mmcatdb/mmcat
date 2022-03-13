package cz.cuni.matfyz.server.repository.utils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 * @author jachym.bartik
 */
interface DatabaseFunction<ReturnType> {

    ReturnType execute(Connection connection) throws SQLException;

}
