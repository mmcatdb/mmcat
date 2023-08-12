package cz.matfyz.server.repository.utils;

import java.sql.Connection;
import java.sql.SQLException;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * @author jachym.bartik
 */
public interface DatabaseGetSingleFunction<T> {

    void execute(Connection connection, SingleOutput<T> output) throws SQLException, JsonProcessingException;

}
