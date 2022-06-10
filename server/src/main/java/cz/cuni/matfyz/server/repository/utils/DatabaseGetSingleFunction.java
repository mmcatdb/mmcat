package cz.cuni.matfyz.server.repository.utils;

import java.sql.Connection;
import java.sql.SQLException;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * 
 * @author jachym.bartik
 */
public interface DatabaseGetSingleFunction<OutputType> {

    void execute(Connection connection, SingleOutput<OutputType> output) throws SQLException, JsonProcessingException;

}
