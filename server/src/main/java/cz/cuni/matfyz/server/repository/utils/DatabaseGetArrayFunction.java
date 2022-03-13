package cz.cuni.matfyz.server.repository.utils;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 * @author jachym.bartik
 */
public interface DatabaseGetArrayFunction<OutputType> {

    void execute(Connection connection, ArrayOutput<OutputType> output) throws SQLException;

}
